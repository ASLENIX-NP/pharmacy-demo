package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import ASLENIX.pharmacy.demo.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder , Long> {

    List<PurchaseOrder> findByPaymentStatus(PaymentStatus paymentStatus);

}
