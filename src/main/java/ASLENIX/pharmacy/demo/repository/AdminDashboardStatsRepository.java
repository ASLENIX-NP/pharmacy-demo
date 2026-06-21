package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.AdminDashboardStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminDashboardStatsRepository extends JpaRepository<AdminDashboardStats , UUID> {

    // Finds the top (first) record ordered by Year DESC, then Month DESC
    Optional<AdminDashboardStats> findTopByOrderByYearDescMonthDesc();

    // Bonus: If you ever need to find stats for a specific month/year
    AdminDashboardStats findByYearAndMonth(Integer year, Integer month);
}
