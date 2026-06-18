package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;


@Getter
public enum SupplierStatus {
    ACTIVE("ACTIVE","bg-success"),
    SUSPENDED("SUSPENDED","bg-danger");


    private final String value;
    private final String colour;

    SupplierStatus(String value, String colour) {
        this.value = value;
        this.colour = colour;
    }

}
