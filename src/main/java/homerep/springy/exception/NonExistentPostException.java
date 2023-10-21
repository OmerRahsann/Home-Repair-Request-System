package homerep.springy.exception;

public class NonExistentPostException extends ApiException {
    public NonExistentPostException() {
        super("non_existent_post", "Post not found.");
    }
}
