package ASLENIX.pharmacy.demo.model;

import ASLENIX.pharmacy.demo.Enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "supplierInfo_tbl")
@Setter
@Getter
@ToString
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String companyName;
    private String panVatNumber;
    private String supplierName;
    private String phone;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private SupplierStatus supplierStatus;

}
