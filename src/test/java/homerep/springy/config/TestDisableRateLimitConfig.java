package homerep.springy.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestDisableRateLimitConfig {
    @Bean
    @Primary
    public RateLimitConfiguration.RateLimitConfig configure(RateLimitConfiguration.RateLimitConfig config) {
        config.setEnabled(false);
        return config;
    }
}
