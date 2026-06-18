package ASLENIX.pharmacy.demo.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "customerInfo_tbl")
@Setter
@Getter
@ToString
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String phone;


    private String email;

    private String panNumber;

    @Column(columnDefinition = "longtext")
    private String medicalHistoryNotes;

}
