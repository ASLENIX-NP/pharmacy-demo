package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Active","bg-success"),
    PENDING("Pending","bg-warning text-dark"),
    SUSPENDED("Suspended"," bg-danger");

    private final String value;
    private final String colour;

    UserStatus(String value, String colour) {
        this.value = value;
        this.colour = colour;
    }

}
