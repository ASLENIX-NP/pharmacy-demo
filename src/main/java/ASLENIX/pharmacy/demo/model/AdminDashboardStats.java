package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "admin_dashboard_stats_tbl")
@Getter
@Setter
public class AdminDashboardStats {
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
    private  UUID id;

     private Integer year;
     private Integer Month;

     private LocalDate LastUpdated;

     private Long thisMonthSales;

}
