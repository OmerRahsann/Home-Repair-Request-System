package homerep.springy;

import homerep.springy.exception.GeocodingException;
import homerep.springy.service.GeocodingService;
import homerep.springy.service.impl.geocoding.NoopGeocodingService;
import homerep.springy.type.LatLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@SpringBootTest
public class GeocodingServiceTest {
    @Autowired
    private GeocodingService geocodingService;

    private static final String KUNG_FU_TEA_ADDRESS = "224 Rowan Blvd, Glassboro, NJ 08028";
    private static final LatLong KUNG_FU_TEA_LOCATION = new LatLong(39.705402,-75.113789);

    private static final String INVALID_ADDRESS = "600 Rowan Blvd, Glassboro, NJ 08028";

    private static final double ONE_SECOND = 1.0/360.0; // Degrees

    @BeforeEach
    void checkGeocodingServiceType() {
        assumeFalse(geocodingService instanceof NoopGeocodingService);
    }

    @Test
    void validAddressTest() throws GeocodingException {
        LatLong location = geocodingService.geocode(KUNG_FU_TEA_ADDRESS);
        assertNotNull(location);
        assertEquals(KUNG_FU_TEA_LOCATION.latitude(), location.latitude(), ONE_SECOND);
        assertEquals(KUNG_FU_TEA_LOCATION.longitude(), location.longitude(), ONE_SECOND);
    }

    @Test
    @Disabled // Both Google and Nominatim provide the location of the Boulevard even with an invalid house number.
    void invalidAddressTest() throws GeocodingException {
        LatLong location = geocodingService.geocode(INVALID_ADDRESS);
        assertNull(location);
    }
}
