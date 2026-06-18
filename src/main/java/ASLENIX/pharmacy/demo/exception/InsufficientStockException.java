package ASLENIX.pharmacy.demo.exception;

public class InsufficientStockException extends PharmacyBusinessException{
    // Default no-argument constructor
    public InsufficientStockException() {
        super();
    }

    // 🎯 The constructor you are using to pass custom error strings from the service layer
    public InsufficientStockException(String message) {
        super(message);
    }
}
