package ASLENIX.pharmacy.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private Double roundOffOne(Double value) {
        if (value == null) {
            return 0.0; // Or return null depending on your business logic
        }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void roundOff() {
        this.totalRevenue = roundOffOne(this.totalRevenue);
        this.totalRevenueGrowthRate = roundOffOne(this.totalRevenueGrowthRate);

        this.purchaseOrderTotal = roundOffOne(this.purchaseOrderTotal);
        this.purchaseOrderTotalGrowthRate = roundOffOne(this.purchaseOrderTotalGrowthRate);

        this.netProfit = roundOffOne(this.netProfit);
        this.netProfitGrowthRate = roundOffOne(this.netProfitGrowthRate);

        this.profitMargin = roundOffOne(this.profitMargin);
        this.profitMarginGrowthRate = roundOffOne(this.profitMarginGrowthRate);
    }


}
