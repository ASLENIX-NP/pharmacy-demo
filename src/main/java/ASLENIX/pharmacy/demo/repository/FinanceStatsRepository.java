package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.FinanceStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public interface FinanceStatsRepository extends JpaRepository<FinanceStats , UUID> {

    Optional<FinanceStats> findByYearAndMonth(int year, int month);

    // This fetches ONLY the totalRevenue field for a specific year and month
    @Query("SELECT f.totalRevenue FROM FinanceStats f WHERE f.year = :year AND f.month = :month")
    Optional<Double> findTotalRevenueByYearAndMonth(
            @Param("year") int year,
            @Param("month") int month
    );


    // This fetches ONLY the purchaseOrderTotal field for a specific year and month
    @Query("SELECT f.purchaseOrderTotal FROM FinanceStats f WHERE f.year = :year AND f.month = :month")
    Optional<Double> findPurchaseOrderTotalByYearAndMonth(
            @Param("year") int year,
            @Param("month") int month
    );

    // This fetches ONLY the totalUnitsSold field for a specific year and month
    @Query("SELECT f.totalUnitsSold FROM FinanceStats f WHERE f.year = :year AND f.month = :month")
    Optional<Long> findTotalUnitsSoldByYearAndMonth(
            @Param("year") int year,
            @Param("month") int month
    );
}
