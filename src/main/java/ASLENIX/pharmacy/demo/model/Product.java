package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name = "product_tbl")
@Setter
@Getter
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String genericName;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String rackLocation;
    private  int minStockLevel;

    private Double discountPercentage;

}
