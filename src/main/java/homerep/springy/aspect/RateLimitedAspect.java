package homerep.springy.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import homerep.springy.aspect.annotation.RateLimited;
import homerep.springy.config.RateLimitConfiguration;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.VerboseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Aspect
@Component
public class RateLimitedAspect {

    private final RateLimitConfiguration.RateLimitConfig config;

    private static final Cache<String, Bucket> BUCKET_CACHE = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Bucket>() {
                private long getExpireTime(Bucket bucket) {
                    VerboseResult<Boolean> result = bucket.asVerbose().tryConsume(1);
                    if (result.getValue()) {
                        // There's probably a race condition here :shrug:
                        bucket.addTokens(1);
                    }
                    return result.getDiagnostics().calculateFullRefillingTime();
                }

                @Override
                public long expireAfterCreate(String key, Bucket value, long currentTime) {
                    return getExpireTime(value);
                }

                @Override
                public long expireAfterUpdate(String key, Bucket value, long currentTime, @NonNegative long currentDuration) {
                    return getExpireTime(value);
                }

                @Override
                public long expireAfterRead(String key, Bucket value, long currentTime, @NonNegative long currentDuration) {
                    return getExpireTime(value);
                }
            })
            .build();

    public RateLimitedAspect(RateLimitConfiguration.RateLimitConfig config) {
        this.config = config;
    }

    @Around(value = "@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint pjp, RateLimited rateLimited) throws Throwable {
        RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new IllegalStateException("Received non ServletRequestAttributes when rate-limiting: " + pjp.getSignature());
        }
        if (!config.isEnabled()) {
            return pjp.proceed();
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String bucketKey = request.getRemoteAddr() + ";" + pjp.getSignature();
        Bucket bucket = BUCKET_CACHE.get(bucketKey, key -> Bucket.builder()
                .addLimit(BandwidthBuilder.builder()
                        .capacity(rateLimited.capacity())
                        .refillIntervally(rateLimited.refillAmount(), Duration.ofSeconds(rateLimited.refillDuration()))
                        .build())
                .build());
        System.out.println(bucketKey + " " + System.identityHashCode(bucket));
        if (bucket.tryConsume(1)) {
            return pjp.proceed();
        } else {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
