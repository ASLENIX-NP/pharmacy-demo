package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.model.*;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public interface AdminServices {

    //==============  users =======================

    void addUser(User user);

    void updateUser( User user);

    User getUserById(Long id);

    List<User> getAllUsers();

    boolean isEmailTaken(String email);

    //==============  dashboard =======================

    Double getTotalSalesThisMonth();

    List<ExpiryDateNotification> getExpiryDateNotification();

    List<LowStockNotification> getLowStockNotification();

    Integer countPendingOrder ();

    void disposeExpiredInventory(Long id);


    //==============  supplier =======================

    void addSupplier(Supplier supplier);

    void updateSupplier(Supplier supplier);

    Supplier getSupplierById(Integer id);

    List<Supplier> getAllSupplier();



    //==============  purchaseOrder =======================

    void addPurchaseOrder(PurchaseOrder purchaseOrder);

    void updatePurchaseOrder(PurchaseOrder purchaseOrder);

    PurchaseOrder getPurchaseOrderById(Long id);

    List<PurchaseOrder> getAllPurchaseOrder();

    //==============  Inventory Batch =======================

    void addInventoryBatch (InventoryBatch inventoryBatch);

    void updateInventoryBatch (InventoryBatch inventoryBatch);

    InventoryBatch getInventoryBatchById(Long id);

    List<InventoryBatch> getAllInventoryBatch();

    List<InventoryBatch> getInventoryBatchesByStatus(BatchApprovalStatus status);

    void approveBatches(List<Long> batchIds);

    //==============  Product =======================

    void addProduct(Product product);

    void updateProduct(Product product);

    Product getProductById(Long id);

    List<Product> getAllProduct();

    byte[] exportProductsInExcel( String username) throws IOException;


    //==============  Category =======================

    void addCategory (Category category);

    void updateCategory (Category category);

    Category getCategoryById(Long id);

    List<Category> getAllCategory();

//==============  finance =======================

    HashMap<String,Integer> mapPaymentStatusThisMonth ();

    LinkedHashMap<String, Double> mapMonthToIncome();

    LinkedHashMap<String,Double> mapMonthToExpense();

    LinkedHashMap<String,Long> mapMonthToQuantity();

    FinanceStats thisMonthsFinanceStats();







}
