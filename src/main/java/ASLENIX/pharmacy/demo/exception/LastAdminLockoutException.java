package ASLENIX.pharmacy.demo.exception;

public class LastAdminLockoutException extends PharmacyBusinessException {
    public LastAdminLockoutException(String message) {
        super(message);
    }
}
