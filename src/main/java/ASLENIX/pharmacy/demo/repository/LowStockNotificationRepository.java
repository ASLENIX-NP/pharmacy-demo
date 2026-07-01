package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.LowStockNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LowStockNotificationRepository extends JpaRepository<LowStockNotification , Long >
{
    // Used by dashboards to display active tasks

    /** For StoreKeeper
        return List of low stock notifications for store keeper
        i.e. products that is low in main rack.
         */
    List<LowStockNotification> findByIsInternalLowStockTrueAndActionTakenFalse();


    /** For Admin
    return List of low stock notifications for admin
    i.e. products that is low in main rack and in storage zone
     */
    List<LowStockNotification> findByIsInternalLowStockFalseAndActionTakenFalse();

    List<LowStockNotification> findByActionTakenFalse();
}
