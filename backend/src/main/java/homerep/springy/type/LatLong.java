package homerep.springy.type;

/**
 * Represents a point on Earth by a latitude and longitude
 * @param latitude degrees North/South of the equator
 * @param longitude degrees West/East of the prime meridian
 */
public record LatLong(double latitude, double longitude) {
}
