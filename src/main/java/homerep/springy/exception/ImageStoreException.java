package homerep.springy.exception;

public class ImageStoreException extends Exception {
    public ImageStoreException(Throwable cause) {
        super("Failed to store image.", cause);
    }
}
