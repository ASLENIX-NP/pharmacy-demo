package ASLENIX.pharmacy.demo.exception;

public class EmailAlreadyExistsException extends PharmacyBusinessException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
