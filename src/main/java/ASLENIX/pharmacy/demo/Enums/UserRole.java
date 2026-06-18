package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ADMIN"," bg-danger"),
    PHARMACIST("PHARMACIST"," bg-primary"),
    CASHIER("CASHIER"," bg-success"),
    STOREKEEPER("STOREKEEPER"," bg-secondary");


    private final String value;
    private final String colour;

    UserRole(String value,String colour) {
        this.value =value;
        this.colour = colour;
    }

};
