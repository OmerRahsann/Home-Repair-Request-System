package homerep.springy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EmailTemplateConfig {
    @Bean
    @ConfigurationProperties("homerep.email-templates")
    public Map<String, String> emailTemplates() {
        // Default email templates
        Map<String, String> map = new HashMap<>();
        map.put("email-verification", "classpath:email-templates/email-verification.eml");
        map.put("reset-password", "classpath:email-templates/reset-password.eml");

        map.put("email-request", "classpath:email-templates/email-request.eml");
        map.put("email-request-accepted", "classpath:email-templates/email-request-accepted.eml");

        map.put("appointment-created", "classpath:email-templates/appointment-created.eml");
        map.put("appointment-confirmed", "classpath:email-templates/appointment-confirmed.eml");
        map.put("appointment-cancelled-by-customer", "classpath:email-templates/appointment-cancelled-by-customer.eml");
        map.put("appointment-cancelled-by-provider", "classpath:email-templates/appointment-cancelled-by-provider.eml");
        return map;
    }
}
