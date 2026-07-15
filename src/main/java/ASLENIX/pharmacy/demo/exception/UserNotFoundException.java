package ASLENIX.pharmacy.demo.exception;

public class UserNotFoundException extends  PharmacyBusinessException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
