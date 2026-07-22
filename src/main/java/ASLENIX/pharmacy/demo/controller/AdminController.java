package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.*;
import ASLENIX.pharmacy.demo.exception.ExpiryDateNotificationNotFound;
import ASLENIX.pharmacy.demo.exception.InventoryBatchNotFoundException;
import ASLENIX.pharmacy.demo.exception.UserNotFoundException;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.services.EmailService;
import ASLENIX.pharmacy.demo.services.TokenService;
import ASLENIX.pharmacy.demo.servicesImpl.AdminServicesImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminServicesImpl adminServices;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;

    //======= mapping for Dashboard =======

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
         if (session.getAttribute("activeUser") == null) {
             return "redirect:/login";
         }

         Double thisMonthSales= adminServices.getTotalSalesThisMonth();
         List<LowStockNotification> lowStockNotificationList  =adminServices.getLowStockNotification();
         List<ExpiryDateNotification> expiryDateNotificationList = adminServices.getExpiryDateNotification();

         Integer countPendingOrder = adminServices.countPendingOrder();

         model.addAttribute("thisMonthSales" , thisMonthSales);
         model.addAttribute("countLowStock", lowStockNotificationList.size());
         model.addAttribute("countExpiryDate", expiryDateNotificationList.size());
         model.addAttribute("countPendingOrder", countPendingOrder);

         model.addAttribute("lowStockNotificationList" , lowStockNotificationList);
        model.addAttribute("expiryDateNotificationList" ,expiryDateNotificationList);


        return "adminDashboard";
    }

    @PostMapping("admin/dashboard/notifications/expiry/dispose")
    public String disposeExpiredInventoryPost(
            @RequestParam("expiredNotificationId") Long expiredNotificationId,
            HttpSession session, RedirectAttributes redirectAttributes){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            adminServices.disposeExpiredInventory(expiredNotificationId);
            redirectAttributes.addFlashAttribute("dashboardSuccess", "Batch set to removed from main rack");

        }
        catch (InventoryBatchNotFoundException | ExpiryDateNotificationNotFound e){
            redirectAttributes.addFlashAttribute("dashboardError", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    //======= mapping for inventory =======

    @GetMapping("/admin/inventory")
    public String adminInventory(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }


        model.addAttribute("inventoryBatch", adminServices.getInventoryBatchesByStatus(BatchApprovalStatus.APPROVED));

        List<InventoryBatch> pendingList =
                adminServices.getInventoryBatchesByStatus(BatchApprovalStatus.PENDING_APPROVAL);

        model.addAttribute("pendingCount",pendingList.size() );
        model.addAttribute("pendingInventoryBatch", pendingList);

        return "adminInventory";
    }

    @GetMapping("/admin/inventory/add")
    public String addInventoryGet(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        List<Product> products = adminServices.getAllProduct();
        List<PurchaseOrder> purchaseOrders = adminServices.getAllPurchaseOrder();

        model.addAttribute("products", products);
        model.addAttribute("purchaseOrder", purchaseOrders);
        model.addAttribute("currentPage", "logistics");
        return "addInventoryForm";
    }

    @PostMapping("/admin/inventory/add")
    public String addInventoryPost(
            @ModelAttribute InventoryBatch inventoryBatch,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            inventoryBatch.setBatchApprovalStatus(BatchApprovalStatus.PENDING_APPROVAL);

            inventoryBatch.setStorageZone(StorageZone.BACKROOM_STOCK);

            adminServices.addInventoryBatch(inventoryBatch);
            redirectAttributes.addFlashAttribute("success",
                    "batch no  '" + inventoryBatch.getBatchNumber() + "' created successfully!");


            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/inventory";
        }
    }

    @GetMapping("/admin/inventory/edit")
    public String editInventoryGet(
            @RequestParam("id") Long id,
            HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        InventoryBatch inventoryBatch = adminServices.getInventoryBatchById(id);
        List<Product> products = adminServices.getAllProduct();
        List<PurchaseOrder> purchaseOrders = adminServices.getAllPurchaseOrder();

        model.addAttribute("inventoryBatch", inventoryBatch);
        model.addAttribute("products", products);
        model.addAttribute("purchaseOrder", purchaseOrders);

        model.addAttribute("batchStatus", BatchApprovalStatus.values());

        model.addAttribute("storageZone", StorageZone.values());

        return "editInventoryBatchForm";
    }

    @PostMapping("/admin/inventory/edit")
    public String editInventoryPost(
            @ModelAttribute InventoryBatch newinventoryBatch,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            InventoryBatch currentIB = adminServices.getInventoryBatchById(newinventoryBatch.getId());

            long currentQuantityReceived = currentIB.getQuantityReceived();
            long newQuantityReceived = newinventoryBatch.getQuantityReceived();

            if (currentQuantityReceived != newQuantityReceived) {
                long soldStock = currentQuantityReceived - currentIB.getCurrentStock();
                newinventoryBatch.setCurrentStock(newQuantityReceived - soldStock);
            }

            System.out.println(newinventoryBatch);

            adminServices.updateInventoryBatch(newinventoryBatch);

            redirectAttributes.addFlashAttribute("success",
                    "inventory batch '" + newinventoryBatch.getBatchNumber() + "' updated successfully!");

            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/inventory";
        }
    }

    @PatchMapping("/admin/inventory/approve")
    public String approveBatches(@RequestParam(value = "batchIds", required = false) List<Long> batchIds,
                                 RedirectAttributes redirectAttributes){

        if (batchIds == null || batchIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one batch to approve.");
            return "redirect:/admin/inventory";
        }

        adminServices.approveBatches(batchIds);

        redirectAttributes.addFlashAttribute("success", batchIds.size()+" batches were approved");

        return "redirect:/admin/inventory";
    }

    @GetMapping("/admin/inventory/export")
    Object exportInventoryBatch(
            HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        try {
            User user = (User) session.getAttribute("activeUser");
            byte[] inventoryBatchesInExcelByte = adminServices.exportInventoryBatchInExcel(user.getUsername());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=inventory_batches_" + user.getUsername()+ "_"+ LocalDateTime.now() + ".xlsx")
                    .body(inventoryBatchesInExcelByte);
        }
        catch (IOException e){
            redirectAttributes.addFlashAttribute("error", "Error occurred while generating Excel file");
            return "redirect:/admin/inventory";

        }

    }

     //======= mapping for product =======

    @GetMapping("/admin/product")
    public String adminProduct(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("products", adminServices.getAllProduct());

        model.addAttribute("category", adminServices.getAllCategory());

        return "adminProduct";
    }

    @GetMapping("/admin/product/add")
    public String addProductGet(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("categories", adminServices.getAllCategory());
        model.addAttribute("currentPage", "logistics");
        return "addProductForm";
    }

    @PostMapping("/admin/product/add")
    public String addProductPost(
            @ModelAttribute Product product,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            adminServices.addProduct(product);
            redirectAttributes.addFlashAttribute("success",
                    "product " + product.getName() + "' created successfully!");

            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/product";
        }
    }

    @GetMapping("/admin/product/edit")
    public String editProductGet(
            @RequestParam("id") Long id,
            HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        Product product = adminServices.getProductById(id);
        List<Category> categories = adminServices.getAllCategory();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "editProductForm";
    }

    @PostMapping("/admin/product/edit")
    public String editProductPost(
            @ModelAttribute Product product,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/admin/product";

        }

        try {

            adminServices.updateProduct(product);

            redirectAttributes.addFlashAttribute("success",
                    "product  '" + product.getName() + "' updated successfully!");

            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/product";

        }
    }

    @GetMapping("/admin/product/export")
    Object exportProducts(
            HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        try {
            User user = (User) session.getAttribute("activeUser");
            byte[] productsInExcelByte = adminServices.exportProductsInExcel(user.getUsername());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=products_" + user.getUsername()+ "_"+ LocalDateTime.now() + ".xlsx")
                    .body(productsInExcelByte);
        }
        catch (IOException e){
            redirectAttributes.addFlashAttribute("error", "Error occurred while generating Excel file");
            return "redirect:/admin/product";

        }

    }

    //======= mapping for Category =======

    @GetMapping("/admin/category/add")
    public String addCategoryGet(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentPage", "logistics");
        return "addCategoryForm";
    }

    @PostMapping("/admin/category/add")
    public String addCategoryPost(
            @ModelAttribute Category category,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            adminServices.addCategory(category);

            redirectAttributes.addFlashAttribute("success",
                    "category " + category.getCategoryName() + "' created successfully!");


            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/inventory";
        }
    }

    @GetMapping("/admin/category/edit")
    public String editCategoryGet(
            @RequestParam("id") Long id,
            HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        Category category = adminServices.getCategoryById(id);

        model.addAttribute("category", category);

        return "editCategoryForm";
    }

    @PostMapping("/admin/category/edit")
    public String editCategoryPost(
            @ModelAttribute Category category,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            adminServices.updateCategory(category);

            redirectAttributes.addFlashAttribute("success",
                    "category   '" + category.getCategoryName() + "' updated successfully!");

            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/inventory";
        }
    }


    //======= mapping for logistics =======
    @GetMapping("/admin/logistics")
    public String adminLogistics(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        List<Supplier> suppliers = adminServices.getAllSupplier();
        List<PurchaseOrder> purchaseOrders = adminServices.getAllPurchaseOrder();

        model.addAttribute("supplier", suppliers);
        model.addAttribute("purchaseOrders", purchaseOrders);
        return "adminLogistics";
    }


     //======= mapping for supplier =======

    @GetMapping("/admin/supplier/add")
    public String addSupplierGet(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("supplierStatus", SupplierStatus.values());
        model.addAttribute("currentPage", "logistics");
        return "addSupplierForm.html";
    }

    @PostMapping("/admin/supplier/add")
    public String addSupplierPost(
            @ModelAttribute Supplier supplier,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            adminServices.addSupplier(supplier);

            redirectAttributes.addFlashAttribute("success",
                    "Supplier '" + supplier.getSupplierName() + "' created successfully!");

            return "redirect:/admin/logistics";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/logistics";
        }
    }

    @GetMapping("/admin/supplier/edit")
    public String editSupplierGet(
            @RequestParam("id") Integer id,
            HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        Supplier supplier = adminServices.getSupplierById(id);

        model.addAttribute("status", SupplierStatus.values());
        model.addAttribute("supplierModel", supplier);

        return "editSupplierForm";
    }

    @PostMapping("/admin/supplier/edit")
    public String editSupplierPost(
            @ModelAttribute Supplier supplier,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            adminServices.updateSupplier(supplier);

            redirectAttributes.addFlashAttribute("success",
                    "Supplier '" + supplier.getSupplierName() + "' updated successfully!");

            return "redirect:/admin/logistics";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/logistics";
        }
    }

    //======= mapping for purchaseOrder =======

    @GetMapping("/admin/purchaseOrder/add")
    public String addPurchaseOrderGet(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        List<Supplier> suppliers = adminServices.getAllSupplier();

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("payment_status", PaymentStatus.values());
        model.addAttribute("currentPage", "logistics");
        return "addPurchaseStatusForm.html";
    }

    @PostMapping("/admin/purchaseOrder/add")
    public String addPurchaseOrderPost(
            @ModelAttribute PurchaseOrder purchaseOrder,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            purchaseOrder.setReceivedDate(java.time.LocalDate.now());
            purchaseOrder.setPurchaseNumber("");
            adminServices.addPurchaseOrder(purchaseOrder);

            redirectAttributes.addFlashAttribute("success",
                    "bill no :'" + purchaseOrder.getBillNumber() + "' created successfully!");
            return "redirect:/admin/logistics";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create purchase order: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/users";
        }

    }

    @GetMapping("/admin/purchaseOrder/edit")
    public String editPurchaseOrderGet(
            @RequestParam("id") Long id,
            Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        List<Supplier> suppliers = adminServices.getAllSupplier();

        model.addAttribute("payment_status", PaymentStatus.values());

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("purchaseOrder", adminServices.getPurchaseOrderById(id));

        return "editPurchaseOrderForm";
    }

    @PostMapping("/admin/purchaseOrder/edit")
    public String editPurchaseOrderPost(
            @ModelAttribute PurchaseOrder purchaseOrder,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            adminServices.updatePurchaseOrder(purchaseOrder);

            redirectAttributes.addFlashAttribute("success",
                    "Purchase Order '" + purchaseOrder.getPurchaseNumber() + "' updated successfully!");

        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/logistics";
        }

        return "redirect:/admin/logistics";
    }


    //======= mapping for users =======
    @GetMapping("/admin/users")
    public String adminUsers(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        List<User> users = adminServices.getAllUsers();
        model.addAttribute("users", users);
        return "adminUser";
    }


    @GetMapping("/admin/users/add")
    public String addUserPage(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("role", UserRole.values());
        model.addAttribute("status", UserStatus.values());
        model.addAttribute("currentPage", "users");
        return "addUserForm";
    }

    @PostMapping("/admin/users/add")
    public String addUser(
            @ModelAttribute User user,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            if (adminServices.isEmailTaken(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email is already registered.");
                return "redirect:/admin/users/add";
            }

            user.setCreatedAt(java.time.LocalDate.now());
            user.setPassword(java.util.UUID.randomUUID().toString());
            user.setStatus(UserStatus.PENDING);
            adminServices.addUser(user);

            PasswordResetToken token = tokenService.createToken(user);
            emailService.sendPasswordSetupEmail(user.getEmail(), user.getUsername(), token.getToken());

            redirectAttributes.addFlashAttribute("success",
                    "User '" + user.getFirstName() + " " + user.getLastName() + "' created successfully! An invitation email has been sent.");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/admin/user/edit")
    public String editUserGet(
            @RequestParam("id") Long id,
            Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }
        model.addAttribute("role", UserRole.values());
        model.addAttribute("status", UserStatus.values());
        model.addAttribute("userModel", adminServices.getUserById(id));

        return "editUserForm";
    }

    @PostMapping("/admin/users/edit")
    public String editUserPost(
            @ModelAttribute User user,
            HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        try {
            adminServices.updateUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "User '" + user.getFirstName() + " " + user.getLastName() + "' updated successfully");
            return "redirect:/admin/users";

        }catch (UserNotFoundException e){
            redirectAttributes.addFlashAttribute("error",  e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/admin/user/export")
    Object userExportInExcel(
            HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        try {
            User user = (User) session.getAttribute("activeUser");
            byte[] usersInExcelByte = adminServices.exportUsersInExcel(user.getUsername());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=users_" + user.getUsername()+ "_"+ LocalDateTime.now() + ".xlsx")
                    .body(usersInExcelByte);
        }
        catch (IOException e){
            redirectAttributes.addFlashAttribute("error", "Error occurred while generating Excel file");
            return "redirect:/admin/users";

        }

    }

    //======= mapping for financials =======

    @GetMapping("/admin/financials")
    public String adminFinancials(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        HashMap<String, Integer> billCountByStatus = adminServices.mapPaymentStatusThisMonth();

        LinkedHashMap<String, Double> monthToIncome = adminServices.mapMonthToIncome();

        LinkedHashMap<String,Double> monthToExpense = adminServices.mapMonthToExpense();

        LinkedHashMap<String,Long > monthToQuantity = adminServices.mapMonthToQuantity();

        FinanceStats LiveFinanceStat = adminServices.thisMonthsFinanceStats();

        model.addAttribute("purchaseOrderStatusMap",billCountByStatus );

        model.addAttribute("monthToIncomeMap", monthToIncome);

        model.addAttribute("monthToExpenseMap", monthToExpense);

        model.addAttribute("monthToQuantityMap", monthToQuantity);

        model.addAttribute("financeStat" , LiveFinanceStat);

        return "adminFinancials";
    }
}
