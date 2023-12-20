package homerep.springy.service.impl.geocoding;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import homerep.springy.exception.GeocodingException;
import homerep.springy.service.GeocodingService;
import homerep.springy.type.LatLong;

import java.io.IOException;

public class GoogleGeocodingService implements GeocodingService {
    private final GeoApiContext context;

    public GoogleGeocodingService(String googleMapsAPIKey) {
        this.context = new GeoApiContext.Builder()
                .apiKey(googleMapsAPIKey)
                .build();
    }

    @Override
    public LatLong geocode(String address) throws GeocodingException {
        GeocodingApiRequest req = GeocodingApi.geocode(context, address);
        try {
            GeocodingResult[] results = req.await();
            for (GeocodingResult result : results) {
                // TODO can we reject unspecific results with result.type?
                LatLng location = result.geometry.location;
                return new LatLong(location.lat, location.lng);
            }
            return null;
        } catch (ApiException | InterruptedException | IOException e) {
            throw new GeocodingException(e);
        }
    }
}
