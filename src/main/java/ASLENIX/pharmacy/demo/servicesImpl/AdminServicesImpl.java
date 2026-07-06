package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import ASLENIX.pharmacy.demo.exception.ExpiryDateNotificationNotFound;
import ASLENIX.pharmacy.demo.exception.InventoryBatchNotFoundException;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.repository.*;
import ASLENIX.pharmacy.demo.services.AdminServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private  AdminDashboardStatsRepository adminDashboardStatsRepository;

    @Autowired
    private  ExpiryDateNotificationRepository expiryDateNotificationRepository;

    @Autowired
    private LowStockNotificationRepository lowStockNotificationRepository;


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
        return userRepository.findAll(Sort.by(Sort.Direction.ASC,"status"));
    }

     /*
==============================================================
                dashboard
====================================================

 */


    @Override
    public Double getTotalSalesThisMonth() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonth().getValue();

        Optional<AdminDashboardStats> adminDashboardStatsOptional = adminDashboardStatsRepository.findByYearAndMonth(year,
                month);

        if (adminDashboardStatsOptional.isEmpty()){
            return 0.0;
        }

        return adminDashboardStatsOptional.get().getThisMonthSales();

    }

    @Override
    public List<ExpiryDateNotification> getExpiryDateNotification() {
        return expiryDateNotificationRepository.findByActionTakenFalse();
    }

    @Override
    public List<LowStockNotification> getLowStockNotification() {
        return lowStockNotificationRepository.findByIsInternalLowStockFalseAndActionTakenFalse();
    }

    @Override
    public Integer countPendingOrder() {
        return purchaseOrderRepository.countByPaymentStatus(PaymentStatus.PENDING);
    }

    @Override
    public void disposeExpiredInventory(Long id) {
        ExpiryDateNotification  expiryDateNotification=
                expiryDateNotificationRepository.findById(id).orElseThrow(
                        ()-> new ExpiryDateNotificationNotFound("This notification do not exists")
                        );

        Optional<InventoryBatch> inventoryBatchOptional =
                inventoryBatchRepository.findById(expiryDateNotification.getInventoryBatch().getId());

        InventoryBatch inventoryBatch;

        if(inventoryBatchOptional.isEmpty()){
            throw  new InventoryBatchNotFoundException("This batch do not exits");
        }else {
            inventoryBatch = inventoryBatchOptional.get();
        }

        inventoryBatch.setBatchApprovalStatus(BatchApprovalStatus.PENDING_REMOVAL);

        expiryDateNotification.setActionTaken(true);

        expiryDateNotificationRepository.save(expiryDateNotification);
        inventoryBatchRepository.save(inventoryBatch);
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
        return purchaseOrderRepository.findAll(Sort.by(Sort.Direction.ASC,"paymentStatus"));
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
        return inventoryBatchRepository.findAll(Sort.by(Sort.Direction.ASC,"batchNumber"));
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
        return productRepository.findAll(Sort.by(Sort.Direction.ASC,"name"));
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

    /*
==============================================================
                finance
====================================================

 */


    @Override
    public HashMap<String, Integer> mapPaymentStatusThisMonth() {

        HashMap<String, Integer> billCountByStatus = new HashMap<>();

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();

        for (PurchaseOrder purchaseOrder : purchaseOrders){
            String currentStatus = purchaseOrder.getPaymentStatus().getValue();
            if(!billCountByStatus.containsKey(currentStatus)){
                billCountByStatus.put(currentStatus,1);
            }
            int count = billCountByStatus.get(currentStatus);
            count++;
            billCountByStatus.put(currentStatus,count);
        }


        return billCountByStatus;
    }


}
