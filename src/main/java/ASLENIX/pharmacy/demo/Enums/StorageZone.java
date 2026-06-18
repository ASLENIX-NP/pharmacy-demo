package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum StorageZone {
    MAIN_RACK("MAIN_RACK","bg-success"),
    BACKROOM_STOCK("BACKROOM_STOCK","bg-warning"),
    DISPOSAL_ZONE("DISPOSAL_ZONE","bg-danger");


    private final String value;
    private final String colour;

    StorageZone(String value, String colour) {
        this.value = value;
        this.colour = colour;
    }
}
