package ASLENIX.pharmacy.demo.exception;

import ASLENIX.pharmacy.demo.servicesImpl.PharmacistServicesImpl;

public class InvoiceItemListEmpty extends PharmacyBusinessException {
    public InvoiceItemListEmpty(String message) {
        super(message);
    }
}
