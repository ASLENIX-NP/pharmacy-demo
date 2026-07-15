package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.BatchApprovalStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentStatus;
import ASLENIX.pharmacy.demo.exception.ExpiryDateNotificationNotFound;
import ASLENIX.pharmacy.demo.exception.InventoryBatchNotFoundException;
import ASLENIX.pharmacy.demo.exception.ReportNotUpdatedExcpetion;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.repository.*;
import ASLENIX.pharmacy.demo.services.AdminServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

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

    @Autowired
    private FinanceStatsRepository financeStatsRepository;

    @Autowired
    private FinanceStatsImpl financeStatsImpl ;


//  ===================  users  =================


    @Override
    public void addUser(User user) {
        userRepository.save(user);

    }

    @Override
    public void updateUser(User user) {
        String un = String.format("%s%d", user.getFirstName(), user.getId());
        String initials = String.valueOf(user.getFirstName().charAt(0)) + user.getLastName().charAt(0);

        user.setUsername(un);

        user.setInitials(initials);
        userRepository.save(user);
    }


    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC,"status"));
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

//  ===================  dashboard  =================


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
        ExpiryDateNotification expiryDateNotification =
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


    //  ===================  supplier  =================


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


    //  ===================  purchaseOrder  =================
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

    //  ===================  Inventory batch  =================
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

    @Override
    public List<InventoryBatch> getInventoryBatchesByStatus(BatchApprovalStatus status) {
        return inventoryBatchRepository.findByBatchApprovalStatus(status);
    }

    @Override
    public void approveBatches(List<Long> batchIds) {

        inventoryBatchRepository.approveMultipleBatches(batchIds,BatchApprovalStatus.APPROVED,BatchApprovalStatus.PENDING_APPROVAL);

    }


    //  ===================  product  =================
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

    //  ===================  Category  =================
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

    //  ===================  finance  =================
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

    @Override
    public LinkedHashMap<String, Double> mapMonthToIncome() {

        LinkedHashMap<String, Double> monthToIncome = new LinkedHashMap<>();


        LocalDate localDate = LocalDate.now().minusYears(1).plusMonths(1);

        for (int i = 0; i < 12; i++) {
            String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Optional<Double> totalRevenueOfAMonthOpt =
                    financeStatsRepository.findTotalRevenueByYearAndMonth(localDate.getYear(),
                    localDate.getMonth().getValue());

            if(totalRevenueOfAMonthOpt.isPresent()){
                monthToIncome.put(month,totalRevenueOfAMonthOpt.get());
            }else {
                monthToIncome.put(month,0.0);
            }

            localDate =  localDate.plusMonths(1);

        }

        return monthToIncome;
    }

    @Override
    public LinkedHashMap<String, Double> mapMonthToExpense() {

        LinkedHashMap<String, Double> monthToExpense = new LinkedHashMap<>();


        LocalDate localDate = LocalDate.now().minusYears(1).plusMonths(1);

        for (int i = 0; i < 12; i++) {
            String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Optional<Double> purchaseOrderTotalOfAMonthOpt =
                    financeStatsRepository.findPurchaseOrderTotalByYearAndMonth(localDate.getYear(),
                            localDate.getMonth().getValue());

            if(purchaseOrderTotalOfAMonthOpt.isPresent()){
                monthToExpense.put(month, purchaseOrderTotalOfAMonthOpt.get());
            }else {
                monthToExpense.put(month,0.0);
            }

            localDate =  localDate.plusMonths(1);

        }

        return monthToExpense;
    }

    @Override
    public LinkedHashMap<String, Long> mapMonthToQuantity() {
        LinkedHashMap<String, Long> monthToQuantity = new LinkedHashMap<>();


        LocalDate localDate = LocalDate.now().minusYears(1).plusMonths(1);

        for (int i = 0; i < 12; i++) {
            String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Optional<Long> totalUnitsSoldOfAMonthOpt =
                    financeStatsRepository.findTotalUnitsSoldByYearAndMonth(localDate.getYear(),
                            localDate.getMonth().getValue());

            if(totalUnitsSoldOfAMonthOpt.isPresent()){
                monthToQuantity.put(month, totalUnitsSoldOfAMonthOpt.get());
            }else {
                monthToQuantity.put(month,0L);
            }

            localDate =  localDate.plusMonths(1);

        }

        return monthToQuantity;
    }

    @Override
    public FinanceStats thisMonthsFinanceStats() {

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        Optional<FinanceStats> financeStatsOptional = financeStatsRepository.findByYearAndMonth(currentYear,currentMonth);

        if(financeStatsOptional.isPresent()){
            return financeStatsOptional.get();
        }


        financeStatsImpl.execute();

        return financeStatsRepository.findByYearAndMonth(currentYear,currentMonth).orElseThrow(()->
                new ReportNotUpdatedExcpetion("The report was not updated"));
    }


}
