package ASLENIX.pharmacy.demo.model;


import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "purchaseOrder_tbl")
@Setter
@Getter
@ToString
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String purchaseNumber;

    @ManyToOne
    private Supplier supplier;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate receivedDate;

    private String billNumber;

    private Double grandTotal;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;


    @PostPersist
    public void generateInvoiceNumber() {
        // This runs automatically right after the entity is saved and gets its ID
        this.purchaseNumber = String.format("PUR-%06d", this.id);
    }




}
