package homerep.springy.exception;

public class GeocodingException extends Exception {
    public GeocodingException(Throwable cause) {
        super("Failed to geocode address.", cause);
    }
}
