package homerep.springy.exception;

public class ApiException extends RuntimeException {
    private final String type;

    public ApiException(String type, String message) {
        super(message);
        this.type = type;
    }

    public ApiException(String type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
