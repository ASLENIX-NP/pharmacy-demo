package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.LowStockNotification;
import ASLENIX.pharmacy.demo.model.Product;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.LowStockNotificationRepository;
import ASLENIX.pharmacy.demo.repository.ProductRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LowStockTaskImpl implements SchedulableTask {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private LowStockNotificationRepository lowStockNotificationRepository;


    @Override
    public JobType getJobName() {
        return JobType.LOW_STOCK_CHECKER;
    }


    HashMap<Long,Product> mapProductIdToProduct(){

        HashMap<Long,Product> productIdToProduct = new HashMap<>();

        List<Product> productList = productRepository.findAll();

        for(Product product : productList){
            productIdToProduct.put(product.getId(),product);
        }
        return productIdToProduct;
    }

    HashMap<Long,Long> mapProductIdToCurrentStocks(){
        HashMap<Long,Long> productIdToCurrentStocks = new HashMap<>();

        List<InventoryBatch> inventoryBatchList = inventoryBatchRepository.findApprovedMainRackBatches();

        for(InventoryBatch inventoryBatch : inventoryBatchList){
            Long productId = inventoryBatch.getProduct().getId();
            Long newStocks = inventoryBatch.getCurrentStock();

            if(productIdToCurrentStocks.containsKey(productId)){
                newStocks = newStocks + productIdToCurrentStocks.get(productId);
            }

            productIdToCurrentStocks.put(productId,newStocks);

        }

        return productIdToCurrentStocks;

    }

    HashMap<Long,Long> mapProductIdToStorageStocks(){
        HashMap<Long,Long> productIdToBackRoomStocks = new HashMap<>();

        List<InventoryBatch> inventoryBatchList = inventoryBatchRepository.findApprovedBackRoomStockBatches();

        for(InventoryBatch inventoryBatch : inventoryBatchList){
            Long id = inventoryBatch.getId();
            Long newStocks = inventoryBatch.getCurrentStock();

            if(productIdToBackRoomStocks.containsKey(id)){
                Long prevStocks= productIdToBackRoomStocks.get(id);
                productIdToBackRoomStocks.put(inventoryBatch.getId() , prevStocks+newStocks );
            }else {
                productIdToBackRoomStocks.put(id,newStocks);
            }
        }

        return productIdToBackRoomStocks;

    }



    @Override
    public void execute()   {HashMap<Long, Product> productIdToProduct = mapProductIdToProduct();
        HashMap<Long, Long> productIdToCurrentStocks = mapProductIdToCurrentStocks();
        HashMap<Long, Long> productIdToBackRoomStocks = mapProductIdToStorageStocks();

// 1. Fetch ALL active notifications in the system with a single query
        List<LowStockNotification> existingNotifications = lowStockNotificationRepository.findByActionTakenFalse();

// 2. Map them by Product ID in memory for O(1) lookups
        Map<Long, LowStockNotification> productToNotificationMap = existingNotifications.stream()
                .filter(lsn -> lsn.getProduct() != null) // Safety check to avoid NullPointerException
                .collect(Collectors.toMap(
                        lsn -> lsn.getProduct().getId(),
                        lsn -> lsn,
                        (existing, replacement) -> existing // Merge function: keep the first one if duplicates somehow exist
                ));

        List<LowStockNotification> saveNotification = new LinkedList<>();

// 3. Iterate through your master product list
        for (Map.Entry<Long, Product> entry : productIdToProduct.entrySet()) {
            Long productId = entry.getKey();
            Product product = entry.getValue();
            if (product == null) continue;

            long minStock = product.getMinStockLevel();
            long currentStock = productIdToCurrentStocks.getOrDefault(productId, 0L);
            long backRoomStock = productIdToBackRoomStocks.getOrDefault(productId, 0L);
            long totalStock = currentStock + backRoomStock;

            boolean isCurrentLow = currentStock < minStock;
            boolean isTotalLow = totalStock < minStock;

            if (isCurrentLow || isTotalLow) {

                // 4. O(1) memory lookup from our pre-fetched map
                LowStockNotification lsn = productToNotificationMap.get(productId);

                if (lsn == null) {
                    // No active notification exists yet; instantiate a new record
                    lsn = new LowStockNotification();
                    lsn.setProduct(product);
                    lsn.setActionTaken(false);
                }

                // 5. Apply business logic updates
                if (isTotalLow) {
                    lsn.setTotalStocks(totalStock);
                    lsn.setInternalLowStock(false); // Global alert takes precedence
                } else {
                    lsn.setTotalStocks(currentStock);
                    lsn.setInternalLowStock(true);  // Storekeeper specific alert
                }

                saveNotification.add(lsn);
            }
        }

// 6. Perform a highly efficient transactional batch save/update
        lowStockNotificationRepository.saveAll(saveNotification);

    }
}
