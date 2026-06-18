package ASLENIX.pharmacy.demo.exception;

public class InvalidInvoiceStatusException extends PharmacyBusinessException {
    public InvalidInvoiceStatusException(String message) {
        super(message);
    }
}
