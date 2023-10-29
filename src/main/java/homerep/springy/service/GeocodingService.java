package homerep.springy.service;

import homerep.springy.exception.GeocodingException;
import homerep.springy.type.LatLong;

public interface GeocodingService {
    LatLong geocode(String address) throws GeocodingException;
}
