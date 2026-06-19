package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.repository.*;
import ASLENIX.pharmacy.demo.services.AdminServices;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.List;

@Service
public class AdminServicesImpl implements AdminServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private  CategoryRepository categoryRepository;


    /*
==============================================================
                users
====================================================

 */

    @Override
    public void addUser(User user) {
        userRepository.save(user);

    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }


    @Override
    public User getUserById(Long id) {
        return userRepository.findById(String.valueOf(id)).get();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /*
==============================================================
                supplier
====================================================

 */

    @Override
    public void addSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    @Override
    public Supplier getSupplierById(Integer id) {
        return supplierRepository.findById(id).get();
    }

    @Override
    public List<Supplier> getAllSupplier() {
        return supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "supplierStatus"));    }


    /*
==============================================================
                purchaseOrder
====================================================

 */

    @Override
    public void addPurchaseOrder(PurchaseOrder purchaseOrder) {
        purchaseOrderRepository.saveAndFlush(purchaseOrder);
    }

    @Override
    public void updatePurchaseOrder(PurchaseOrder purchaseOrder) {
        purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id).get();
    }

    @Override
    public List<PurchaseOrder> getAllPurchaseOrder() {
        return purchaseOrderRepository.findAll();
    }

    /*
==============================================================
                Inventory batch
====================================================

 */

    @Override
    public void addInventoryBatch(InventoryBatch inventoryBatch) {
        inventoryBatchRepository.save(inventoryBatch);
    }

    @Override
    public void updateInventoryBatch(InventoryBatch inventoryBatch) {
        inventoryBatchRepository.save(inventoryBatch);

    }

    @Override
    public InventoryBatch getInventoryBatchById(Long id) {
        return inventoryBatchRepository.findById(id).get();
    }

    @Override
    public List<InventoryBatch> getAllInventoryBatch() {
        return inventoryBatchRepository.findAll();
    }

     /*
==============================================================
                product
====================================================

 */


    @Override
    public void addProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public void updateProduct(Product product) {
        productRepository.save(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).get();
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

     /*
  ==============================================================
                  Category
  ====================================================

   */

    @Override
    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).get();
    }

    @Override
    public List<Category> getAllCategory() {
        return  categoryRepository.findAll();
    }





}
