package homerep.springy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.service.GeocodingService;
import homerep.springy.service.impl.geocoding.GoogleGeocodingService;
import homerep.springy.service.impl.geocoding.NominatimGeocodingService;
import homerep.springy.service.impl.geocoding.NoopGeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeocodingServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMailConfig.class);

    @Value("${homerep.geocoding.google-maps-api-key:#{null}}")
    private String googleMapsAPIKey;

    @Value("${homerep.geocoding.nominatim-url:#{null}}")
    private String nominatimURL;

    @Bean
    @ConditionalOnExpression("'${homerep.geocoding.google-maps-api-key:}' == '' and '${homerep.geocoding.nominatim-url:}' == ''")
    public GeocodingService noopGeocodingService() {
        LOGGER.warn("No geocoding service was configured. Noop geocoder will be used.");
        return new NoopGeocodingService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "homerep.geocoding", name = "google-maps-api-key")
    public GeocodingService googleGeocodingService() {
        LOGGER.info("Google Maps API key was supplied. Google geocoder will be used.");
        return new GoogleGeocodingService(googleMapsAPIKey);
    }

    @Bean
    @ConditionalOnProperty(prefix = "homerep.geocoding", name = "nominatim-url")
    public GeocodingService nominatimGeocodingService(ObjectMapper mapper) {
        LOGGER.info("Nomination URL was supplied. Nominatim geocoder will be used.");
        return new NominatimGeocodingService(nominatimURL, mapper);
    }
}
