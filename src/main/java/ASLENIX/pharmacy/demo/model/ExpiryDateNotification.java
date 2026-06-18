package ASLENIX.pharmacy.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "expiryDate_notification_tbl")
@Setter
@Getter
public class ExpiryDateNotification {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private  Long id;

    @OneToOne(orphanRemoval = true)
    private InventoryBatch inventoryBatch;

    private Integer daysLeft;

    private boolean actionTaken = false;

}
