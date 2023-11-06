package homerep.springy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.service.GeocodingService;
import homerep.springy.service.impl.geocoding.GoogleGeocodingService;
import homerep.springy.service.impl.geocoding.NominatimGeocodingService;
import homerep.springy.service.impl.geocoding.NoopGeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "homerep.geocoding")
public class GeocodingServiceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackMailConfig.class);

    private String googleMapsApiKey;

    private String nominatimUrl;

    @Bean
    public GeocodingService geocodingService(ObjectMapper mapper) {
        if (googleMapsApiKey != null) {
            LOGGER.info("Google Maps Api key was supplied. Google geocoder will be used.");
            return new GoogleGeocodingService(googleMapsApiKey);
        } else if (nominatimUrl != null) {
            LOGGER.info("Nomination Url was supplied. Nominatim geocoder will be used.");
            return new NominatimGeocodingService(nominatimUrl, mapper);
        } else {
            LOGGER.warn("No geocoding service was configured. Noop geocoder will be used.");
            return new NoopGeocodingService();
        }
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public void setGoogleMapsApiKey(String googleMapsApiKey) {
        this.googleMapsApiKey = googleMapsApiKey;
    }

    public String getNominatimUrl() {
        return nominatimUrl;
    }

    public void setNominatimUrl(String nominatimUrl) {
        this.nominatimUrl = nominatimUrl;
    }
}
