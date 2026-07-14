package ASLENIX.pharmacy.demo.repository;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.StorageZone;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    @Query("""
                SELECT b
                FROM InventoryBatch b
                JOIN b.product p
                WHERE b.storageZone = 'MAIN_RACK'
                  AND b.batchApprovalStatus = 'APPROVED'
                  AND b.currentStock > 0
                  AND (
                        LOWER(p.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                     OR LOWER(p.genericName) LIKE CONCAT('%', LOWER(:keyword), '%')
                  )
                ORDER BY b.expiryDate ASC, b.currentStock ASC
            """)
    List<InventoryBatch> searchBatches(@Param("keyword") String keyword);

    @Query("SELECT ib FROM InventoryBatch ib " +
            "WHERE ib.batchApprovalStatus = :status " +
            "AND ib.storageZone = :zone")
    List<InventoryBatch> findBatchesByStatusAndZone(
            @Param("status") BatchApprovalStatus status,
            @Param("zone") StorageZone zone
    );

}
