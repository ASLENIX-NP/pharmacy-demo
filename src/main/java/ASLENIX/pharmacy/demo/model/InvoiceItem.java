package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name = "InvoiceItem_tbl")
@Setter
@Getter
@ToString
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;

    @ManyToOne
    private Product product ;

    @ManyToOne
    private InventoryBatch batch;

    private  Long quantity;

    private Double unitPrice;

    private  Double discountPercentage;

    private Double discountAmount;

    private Double vatPercentage;

    private Double vatAmount;

    private Double subTotal;

    private  Double lineTotal;


}
