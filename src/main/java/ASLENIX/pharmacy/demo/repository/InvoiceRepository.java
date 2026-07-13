package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.model.Invoice;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice , Long> {

    @Query("SELECT i FROM Invoice i WHERE i.transactionDate BETWEEN :start AND :end AND i.invoiceStatus != InvoiceStatus.CANCELED")
    List<Invoice> findActiveInvoicesByDateRange(@Param("start") LocalDate startDate, @Param("end") LocalDate endDate);


    @Query("SELECT i FROM Invoice i WHERE i.transactionDate BETWEEN :start AND :end AND i.invoiceStatus !=  InvoiceStatus.COMPLETE")
    List<Invoice> findCompleteInvoicesByDateRange(@Param("start") LocalDate startDate, @Param("end") LocalDate endDate);


    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE i.pharmacist.id = :pharmacistId " +
            "AND i.transactionDate = :transactionDate " +
            "AND i.invoiceStatus IN (InvoiceStatus.COMPLETE, InvoiceStatus.PAYMENTPENDING)")
    int countActivePrescriptions(Long pharmacistId, LocalDate transactionDate);

    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE i.cashier.id = :cashierId " +
            "AND i.transactionDate = :transactionDate " +
            "AND i.invoiceStatus = InvoiceStatus.COMPLETE")
    int countCashierCompletePrescriptions(Long cashierId, LocalDate transactionDate);

    @Query("SELECT i FROM Invoice i " +
            "WHERE i.transactionDate = :transactionDate " +
            "AND i.invoiceStatus = InvoiceStatus.PAYMENTPENDING")
    List<Invoice> findPaymentPendingInvoicesByDate(LocalDate transactionDate);

    @Query("SELECT i FROM Invoice i " +
            "WHERE i.transactionDate = :transactionDate " +
            "AND i.invoiceStatus = InvoiceStatus.COMPLETE " +
            "AND i.cashier.id = :cashierId")
    List<Invoice> findCompleteInvoicesByDateAndCashier(
            @Param("transactionDate") LocalDate transactionDate,
            @Param("cashierId") Long cashierId
    );
    @Query("SELECT SUM(i.grandTotal) FROM Invoice i WHERE i.transactionDate BETWEEN :start AND :end")
    Double sumRevenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(item.quantity), 0) " +
            "FROM Invoice inv " +
            "JOIN inv.invoiceItemList item " +
            "WHERE inv.transactionDate BETWEEN :startDate AND :endDate")
    Long getTotalItemQuantityByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
