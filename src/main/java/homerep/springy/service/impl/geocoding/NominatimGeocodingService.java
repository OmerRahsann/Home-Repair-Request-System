package homerep.springy.service.impl.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.exception.GeocodingException;
import homerep.springy.service.GeocodingService;
import homerep.springy.service.impl.ImageStorageServiceImpl;
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

public class NominatimGeocodingService implements GeocodingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageStorageServiceImpl.class);

    private final UriBuilderFactory nominatimUriBuilder;

    private final OkHttpClient client;

    private final ObjectMapper mapper;

    public NominatimGeocodingService(String nominatimURL, ObjectMapper mapper) {
        this.nominatimUriBuilder = new DefaultUriBuilderFactory(nominatimURL);
        this.client = new OkHttpClient(); // TODO app wide http client or service?
        this.mapper = mapper;
    }

    @Override
    public LatLong geocode(String address) throws GeocodingException {
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
