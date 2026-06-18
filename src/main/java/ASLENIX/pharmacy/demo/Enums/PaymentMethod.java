package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("CASH"),
    E_WALLET("E_WALLET"),
    CREDIT_CARD("CREDIT_CARD");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

}
