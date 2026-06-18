package ASLENIX.pharmacy.demo.exception;

public class InventoryBatchNotFoundException extends PharmacyBusinessException {
    public InventoryBatchNotFoundException(String message) {
        super(message);
    }
}
