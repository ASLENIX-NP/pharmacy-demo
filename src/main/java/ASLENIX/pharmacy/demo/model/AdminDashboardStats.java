package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
     private Integer month;

     private LocalDate lastUpdated;

     private Double thisMonthSales;


     public void  roundOff(){

         BigDecimal tempRoundOff = new BigDecimal(Double.toString(this.thisMonthSales));

         this.thisMonthSales = tempRoundOff.setScale(2, RoundingMode.HALF_UP).doubleValue();

     }

}
