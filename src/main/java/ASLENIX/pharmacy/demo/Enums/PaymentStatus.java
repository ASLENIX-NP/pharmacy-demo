package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    RECEIVED("RECEIVED","bg-success text-dark"),
    COMPLETE("COMPLETE","bg-success"),
    PENDING("PENDING","bg-warning text-dark"),
    DUE("DUE"," bg-danger");

    private final String value;
    private final String colour;

    PaymentStatus(String value, String colour) {
        this.value = value;
        this.colour = colour;
    }

   };
