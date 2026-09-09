package ASLENIX.pharmacy.demo.exception;

public class SelfModificationException extends PharmacyBusinessException{
    public SelfModificationException(String message) {
        super(message);
    }
}
