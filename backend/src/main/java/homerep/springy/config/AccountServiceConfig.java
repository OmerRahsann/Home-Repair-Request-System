package homerep.springy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "homerep")
public class AccountServiceConfig {
    private boolean requireVerification = false;

    public boolean isRequireVerification() {
        return requireVerification;
    }

    public void setRequireVerification(boolean requireVerification) {
        this.requireVerification = requireVerification;
    }
}
