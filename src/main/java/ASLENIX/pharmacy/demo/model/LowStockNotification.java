package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lowStock_notification_tbl")
@Getter
@Setter
public class LowStockNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @OneToOne
    private Product product;

    private Long totalStocks;

    private boolean isInternalLowStock;

    private boolean actionTaken = false;

}
