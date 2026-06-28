package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.FinanceStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinanceStatsRepository extends JpaRepository<FinanceStats , UUID> {

    Optional<FinanceStats> findByYearAndMonth(int year, int month);

}
