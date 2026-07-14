package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.model.*;

import java.util.List;

public interface AdminServices {
/*
==============================================================
                users
====================================================

 */
    void addUser(User user);

    void updateUser( User user);


    User getUserById(Long id);

    List<User> getAllUsers();

    boolean isEmailTaken(String email);

    /*
==============================================================
                supplier
====================================================

 */

    void addSupplier(Supplier supplier);

    void updateSupplier(Supplier supplier);

    Supplier getSupplierById(Integer id);

    List<Supplier> getAllSupplier();

    /*
==============================================================
                purchaseOrder
====================================================

 */

    void addPurchaseOrder(PurchaseOrder purchaseOrder);

    void updatePurchaseOrder(PurchaseOrder purchaseOrder);

    PurchaseOrder getPurchaseOrderById(Long id);

    List<PurchaseOrder> getAllPurchaseOrder();

/*
==============================================================
                Inventory Batch
====================================================

 */

    void addInventoryBatch (InventoryBatch inventoryBatch);

    void updateInventoryBatch (InventoryBatch inventoryBatch);

    InventoryBatch getInventoryBatchById(Long id);

    List<InventoryBatch> getAllInventoryBatch();


/*
==============================================================
                Product
====================================================

 */

    void addProduct(Product product);

    void updateProduct(Product product);

    Product getProductById(Long id);

    List<Product> getAllProduct();


/*
==============================================================
                Category
====================================================

 */

    void addCategory (Category category);

    void updateCategory (Category category);

    Category getCategoryById(Long id);

    List<Category> getAllCategory();


}
