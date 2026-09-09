package ASLENIX.pharmacy.demo.model;

import ASLENIX.pharmacy.demo.Enums.UserRole;
import ASLENIX.pharmacy.demo.Enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "user_tbl")
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private   Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String initials;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAt;

    @PostPersist
    @PreUpdate
    public void initializeProfileMetadata() {
        this.username = String.format("%s%d", firstName, this.id) ;
        this.initials = String.valueOf(firstName.charAt(0)) + lastName.charAt(0);
    }



}
