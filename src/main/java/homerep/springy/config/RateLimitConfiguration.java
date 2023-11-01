package homerep.springy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "homerep.rate-limit")
    public RateLimitConfig rateLimitConfig() {
        return new RateLimitConfig();
    }

    public static class RateLimitConfig {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
