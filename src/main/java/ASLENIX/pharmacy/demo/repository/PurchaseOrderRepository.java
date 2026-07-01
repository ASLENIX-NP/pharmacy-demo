package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import ASLENIX.pharmacy.demo.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder , Long> {

    List<PurchaseOrder> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT SUM(p.grandTotal) FROM PurchaseOrder p WHERE p.receivedDate BETWEEN :start AND :end")
    Double sumPurchasesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    Integer countByPaymentStatus(PaymentStatus paymentStatus);


}
