package homerep.springy.service.impl.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import homerep.springy.exception.GeocodingException;
import homerep.springy.service.GeocodingService;
import homerep.springy.type.LatLong;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class NominatimGeocodingService implements GeocodingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NominatimGeocodingService.class);

    private final UriBuilderFactory nominatimUriBuilder;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    /**
     * Cache all geocoding results for 1 week to avoid sending the same request repeatedly
     */
    private static final Cache<String, LatLong> GEOCODING_CACHE = Caffeine.newBuilder()
            .maximumSize(1024 * 1024 / 16) // Roughly 1 MiB of coordinates
            .expireAfterWrite(Duration.ofDays(7))
            .build();

    /**
     * Sentinel value that represents a null result in the GEOCODING_CACHE
     */
    private static final LatLong NULL_CACHE_RESULT = new LatLong(Double.MAX_VALUE, Double.MAX_VALUE);

    public NominatimGeocodingService(String nominatimURL, ObjectMapper mapper) {
        this.nominatimUriBuilder = new DefaultUriBuilderFactory(nominatimURL);
        this.client = new OkHttpClient(); // TODO app wide http client or service?
        this.mapper = mapper;
    }

    @Override
    public LatLong geocode(String address) throws GeocodingException {
        LatLong location = GEOCODING_CACHE.getIfPresent(address);
        if (location != null) {
            return NULL_CACHE_RESULT.equals(location) ? null : location;
        }
        location = geocode0(address);
        GEOCODING_CACHE.put(address, location == null ? NULL_CACHE_RESULT : location);
        return location;
    }

    private LatLong geocode0(String address) throws GeocodingException {
        try {
            URL url = nominatimUriBuilder.uriString("/search")
                    .queryParam("q", address)
                    .build().toURL();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            // TODO rate limits? use public api?
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    if (response.body() != null) {
                        LOGGER.error("Unsuccessful search call to Nominatim! Status: {} Content: {}", response.code(), response.body().string());
                    } else {
                        LOGGER.error("Unsuccessful search call to Nominatim! Status: {}", response.code());
                    }
                    return null;
                }
                if (response.body() == null) {
                    return null; // No results?
                }
                PlaceResult[] results = mapper.readValue(response.body().charStream(), PlaceResult[].class);
                if (results.length > 0) {
                    return new LatLong(results[0].latitude, results[0].longitude);
                }
            }
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Base Nominatim URL is malformed.", e);
        } catch (IOException e) {
            throw new GeocodingException(e);
        }
    }

    private record PlaceResult(
            @JsonProperty("place_id") int placeId,
            String license,
            @JsonProperty("osm_type") String osmType,
            @JsonProperty("boundingbox") double[] boundingBox,
            @JsonProperty("lat") double latitude,
            @JsonProperty("lon") double longitude,
            @JsonProperty("display_name") String displayName,
            @JsonProperty("place_rank") int placeRank,
            String category,
            String type,
            double importance
    ) {
    }

}
