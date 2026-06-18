package ASLENIX.pharmacy.demo.exception;

public class InvoiceNotFoundException extends PharmacyBusinessException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
