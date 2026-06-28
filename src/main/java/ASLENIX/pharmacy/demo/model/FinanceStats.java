package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Finance_stats_tbl")
@Getter
@Setter
public class FinanceStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private  int year ;
    private int month;

    private Double totalRevenue;

    private Double totalRevenueGrowthRate;

    private Double purchaseOrderTotal;
    private Double purchaseOrderTotalGrowthRate;


    private Double netProfit;
    private Double netProfitGrowthRate;


    private Double profitMargin ;
    private Double profitMarginGrowthRate ;

    private LocalDateTime lastUpdated;
}
