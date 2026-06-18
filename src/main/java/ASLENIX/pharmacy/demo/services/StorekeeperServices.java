package ASLENIX.pharmacy.demo.services;


import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.Product;
import ASLENIX.pharmacy.demo.model.PurchaseOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StorekeeperServices {

    void updateProductLocation(Long productId, String rackLocation);
    List<Product>  getAllProduct();

    void addInventoryBatch (Product product , LocalDate expiryDate , long quantityReceived, PurchaseOrder purchaseOrder);


    void pullBatchOffRack(Long batchId);
    List<InventoryBatch> getAllInventoryBatch();

    Map<String, Long> getDashboardStats();

    List<PurchaseOrder> getCompletedPurchaseOrder ();


    void conformDeliverOfOrder(Long purchaseOrderId);


    List<PurchaseOrder> getALlPurchaseOrder();



}
