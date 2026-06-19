package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.LowStockNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LowStockNotificationRepository extends JpaRepository<LowStockNotification , Long >
{
    // Used by dashboards to display active tasks
    List<LowStockNotification> findByIsInternalLowStockTrueAndActionTakenFalse();  // Storekeeper items
    List<LowStockNotification> findByIsInternalLowStockFalseAndActionTakenFalse(); // Admin items

    List<LowStockNotification> findByActionTakenFalse();
}
