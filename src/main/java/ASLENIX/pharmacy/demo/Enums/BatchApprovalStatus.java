package ASLENIX.pharmacy.demo.Enums;


import lombok.Getter;

@Getter
public enum BatchApprovalStatus {
    PENDING_APPROVAL("PENDING_APPROVAL","bg-warning"),
    APPROVED("APPROVED","bg-success"),
    PENDING_REMOVAL ("PENDING_REMOVAL","bg-warning text-dark"),
    REMOVED("REMOVED","bg-secondary");


    private final String value;
    private final String colour;

    BatchApprovalStatus(String value, String colour) {
        this.value = value;
        this.colour = colour;
    }

};