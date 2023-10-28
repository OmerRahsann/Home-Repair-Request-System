package homerep.springy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@Configuration
public class UriBuilderConfig {
    @Value("${server.base-url:#{null}}")
    private String baseUrl;

    @Bean
    public UriBuilderFactory uriBuilderFactory(ServerProperties serverProperties) {
        if (baseUrl == null) {
            String protocol = "http";
            if (serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled()) {
                protocol = "https";
            }
            baseUrl = protocol + "://localhost:" + serverProperties.getPort();
        }
        return new DefaultUriBuilderFactory(baseUrl);
    }
}
