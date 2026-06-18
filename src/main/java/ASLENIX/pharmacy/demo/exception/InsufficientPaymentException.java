package ASLENIX.pharmacy.demo.exception;

public class InsufficientPaymentException extends PharmacyBusinessException {
    public InsufficientPaymentException(String message) {
        super(message);
    }
}
