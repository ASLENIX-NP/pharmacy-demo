package ASLENIX.pharmacy.demo.exception;

public class CustomerNotFoundException extends PharmacyBusinessException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
