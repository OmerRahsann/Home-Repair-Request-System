package homerep.springy.service.impl;

import homerep.springy.service.GeocodingService;
import homerep.springy.type.LatLong;

public class NoopGeocodingService implements GeocodingService {
    @Override
    public LatLong geocode(String address) {
        return new LatLong(0, 0);
    }
}
