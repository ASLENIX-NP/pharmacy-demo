package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import ASLENIX.pharmacy.demo.Enums.StorageZone;
import ASLENIX.pharmacy.demo.exception.InventoryBatchNotFoundException;
import ASLENIX.pharmacy.demo.exception.InvoiceNotFoundException;
import ASLENIX.pharmacy.demo.exception.ProductNotFoundException;
import ASLENIX.pharmacy.demo.exception.PurchaseOrderNotFoundException;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.ProductRepository;
import ASLENIX.pharmacy.demo.repository.PurchaseOrderRepository;
import ASLENIX.pharmacy.demo.services.StorekeeperServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StorekeeperServicesImpl implements StorekeeperServices {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public void updateProductLocation(Long productId, String rackLocation) {
            Product product =
                    productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId +
                            " do not exists"));
            product.setRackLocation(rackLocation);
            productRepository.save(product);
    }

    @Override
    public List<Product> getAllProduct() {
            return productRepository.findAll();
    }

    @Override
    public void addInventoryBatch(Product product, LocalDate expiryDate, long quantityReceived, PurchaseOrder purchaseOrder) {

        InventoryBatch inventoryBatch = new InventoryBatch();
        inventoryBatch.setProduct(product);
        inventoryBatch.setExpiryDate(expiryDate);
        inventoryBatch.setQuantityReceived(quantityReceived);
        inventoryBatch.setPurchaseOrder(purchaseOrder);
        inventoryBatch.setBatchApprovalStatus(BatchApprovalStatus.PENDING_APPROVAL);
        inventoryBatch.setStorageZone(StorageZone.BACKROOM_STOCK);
        inventoryBatch.setCostPrice(0.0);
        inventoryBatch.setSellingPrice(0.0);

        inventoryBatchRepository.save(inventoryBatch);

    }

    @Override
    public void pullBatchOffRack(Long batchId) {

        InventoryBatch inventoryBatch =
                inventoryBatchRepository.findById(batchId).orElseThrow(()-> new InventoryBatchNotFoundException( batchId+" do ont exits "));

        inventoryBatch.setBatchApprovalStatus(BatchApprovalStatus.REMOVED);
        inventoryBatch.setStorageZone(StorageZone.DISPOSAL_ZONE);

        inventoryBatchRepository.save(inventoryBatch);
    }

    @Override
    public List<InventoryBatch> getAllInventoryBatch() {

        List<InventoryBatch> batches  =inventoryBatchRepository.findAll();

        return  batches.stream()
                .sorted(Comparator.comparing(InventoryBatch::getStorageZone)) // Java naturally enums by their ordinal order!
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalBatches", inventoryBatchRepository.count());

        stats.put("pendingTasks",
                inventoryBatchRepository.findAll().stream().filter(b -> b.getBatchApprovalStatus().equals(BatchApprovalStatus.PENDING_REMOVAL)).count());

        stats.put("remainingDelivery" ,
                purchaseOrderRepository.findAll().stream().filter(purchaseOrder -> purchaseOrder.getPaymentStatus().equals(PaymentStatus.COMPLETE)).count());
        return stats;
    }

    @Override
    public List<PurchaseOrder> getCompletedPurchaseOrder() {
        return purchaseOrderRepository.findByPaymentStatus(PaymentStatus.COMPLETE);
    }

    @Override
    public void conformDeliverOfOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder =
                purchaseOrderRepository.findById(purchaseOrderId).orElseThrow(() -> new PurchaseOrderNotFoundException(purchaseOrderId + " do not exists"));
        purchaseOrder.setPaymentStatus(PaymentStatus.RECEIVED);
        purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    public List<PurchaseOrder> getALlPurchaseOrder() {
        return purchaseOrderRepository.findAll();
    }
}
