package ASLENIX.pharmacy.demo.exception;

public class MismatchPasswordException extends RuntimeException {
    public MismatchPasswordException(String message) {
        super(message);
    }
}
