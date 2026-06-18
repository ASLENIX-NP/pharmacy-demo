package ASLENIX.pharmacy.demo.Enums;

import lombok.Getter;

@Getter
public enum JobType {
    EXPIRY_CHECKER("EXPIRY_CHECKER"),
    LOW_STOCK_CHECKER("LOW_STOCK_CHECKER"),
    ADMIN_DASHBOARD_UPDATER("ADMIN_DASHBOARD_UPDATER"),
    FINANCE_UPDATED("FINANCE_UPDATED");

    private final String value;

    JobType(String value) {
        this.value =value;
    }
}
