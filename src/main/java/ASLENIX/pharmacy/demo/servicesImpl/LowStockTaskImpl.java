package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.JobType;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.LowStockNotification;
import ASLENIX.pharmacy.demo.model.Product;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.ProductRepository;
import ASLENIX.pharmacy.demo.services.SchedulableTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
public class LowStockTaskImpl implements SchedulableTask {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;


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
            Long id = inventoryBatch.getId();
            Long newStocks = inventoryBatch.getCurrentStock();

            if(productIdToCurrentStocks.containsKey(id)){
                Long prevStocks= productIdToCurrentStocks.get(id);
                productIdToCurrentStocks.put(inventoryBatch.getId() , prevStocks+newStocks );
            }else {
                productIdToCurrentStocks.put(id,newStocks);
            }
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
    public void execute() {

        HashMap<Long,Product> productIdToProduct = mapProductIdToProduct();

        HashMap<Long,Long> productIdToCurrentStocks = mapProductIdToCurrentStocks();
        HashMap<Long,Long> productIdToBackRoomStocks = mapProductIdToStorageStocks();





        HashMap<Long , Boolean> productIdToForAdmin = new HashMap<>();

        List<LowStockNotification> saveNotification = new LinkedList<>();

        for (Long productId : productIdToCurrentStocks.keySet()){
            Long minStock  = productIdToProduct.get(productId).getMinStockLevel();

            Long activeQty = productIdToCurrentStocks.get(productId);
            if( activeQty < minStock){
                productIdToForAdmin.put(productId,true);
            }
        }

        for (Long productId : productIdToBackRoomStocks.keySet()){
            Long minStock  = productIdToProduct.get(productId).getMinStockLevel();

        }













    }
}
