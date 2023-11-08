package homerep.springy.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rate limit a REST endpoint by request IP.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    /**
     * @return the max number of tokens
     */
    long capacity();

    /**
     * @return the number of tokens to refill over the refillDuration()
     */
    long refillAmount();

    /**
     * @return the number of seconds over which to refill the bucket with refillAmount() tokens
     */
    long refillDuration();
}
