package ASLENIX.pharmacy.demo.model;


import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.StorageZone;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name = "inventoryBatch_tbl")
@Setter
@Getter
@ToString
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String batchNumber;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn( nullable = false)
    private PurchaseOrder purchaseOrder;



    private long quantityReceived;
    private long currentStock;

    private Double costPrice;
    private Double sellingPrice;

    @Enumerated(EnumType.STRING)
    private BatchApprovalStatus batchApprovalStatus;

    @Enumerated(EnumType.STRING)
    private StorageZone storageZone;

@PostPersist
    public void generateInvoiceNumber() {
        // This runs automatically right after the entity is saved and gets its ID
        this.batchNumber = String.format("BACH-%06d", this.id);
        this.currentStock = this.quantityReceived;
    }


}
