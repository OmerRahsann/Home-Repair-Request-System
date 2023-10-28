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
        return map;
    }
}
