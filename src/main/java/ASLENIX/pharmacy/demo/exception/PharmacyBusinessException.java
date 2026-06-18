package ASLENIX.pharmacy.demo.exception;

public class PharmacyBusinessException extends RuntimeException{
    // Default no-argument constructor
    public PharmacyBusinessException() {
        super();
    }

    // 🎯 The constructor you are using to pass custom error strings from the service layer
    public PharmacyBusinessException(String message) {
        super(message);
    }
}
