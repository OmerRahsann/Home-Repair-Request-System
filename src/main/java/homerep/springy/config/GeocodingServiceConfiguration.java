package homerep.springy.config;

import homerep.springy.service.GeocodingService;
import homerep.springy.service.impl.GoogleGeocodingService;
import homerep.springy.service.impl.NoopGeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeocodingServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMailConfig.class);

    @Value("${homerep.geocoding.google-maps-api-key:#{null}}")
    private String googleMapsAPIKey;

    @Bean
    @ConditionalOnProperty(prefix = "homerep.geocoding", name = "google-maps-api-key", havingValue="\0", matchIfMissing = true)
    public GeocodingService noopGeocodingService() {
        LOGGER.warn("Missing Google Maps API key. Noop geocoder will be used.");
        return new NoopGeocodingService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "homerep.geocoding", name = "google-maps-api-key")
    public GeocodingService googleGeocodingService() {
        LOGGER.info("Google Maps API key was supplied. Google geocoder will be used.");
        return new GoogleGeocodingService(googleMapsAPIKey);
    }
}
