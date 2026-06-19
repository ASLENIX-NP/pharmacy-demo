package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum InvoiceStatus {

    COMPLETE("COMPLETE","bg-success"),
    PAYMENTPENDING("PAYMENT PENDING","bg-warning text-dark"),
    INVOICEPENDING("INVOICE PENDING","bg-warning"),

    CANCELED("CANCELED"," bg-danger");

    private final String value;
    private final String colour;

    InvoiceStatus(String value, String colour) {
        this.value = value;
        this.colour = colour;


    }

}
