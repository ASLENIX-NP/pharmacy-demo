package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name = "product_tbl")
@Setter
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String genericName;

    @ManyToOne(fetch = FetchType.LAZY) // Explicitly set to LAZY
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String rackLocation;
    private  Long minStockLevel;

    private Double discountPercentage;

}
