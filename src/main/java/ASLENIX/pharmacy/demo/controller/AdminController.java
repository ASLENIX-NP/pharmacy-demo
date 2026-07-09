package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.*;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.servicesImpl.AdminServicesImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminServicesImpl adminServices;

    // ========================================================================
    // mapping for Dashboard
    // ========================================================================

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "overview");
        return "adminDashboard";
    }

    // ========================================================================
    // mapping for inventory
    // ========================================================================

    @GetMapping("/admin/inventory")
    public String adminInventory(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("inventoryBatch", adminServices.getAllInventoryBatch());

        model.addAttribute("products", adminServices.getAllProduct());

        model.addAttribute("category", adminServices.getAllCategory());

        model.addAttribute("currentPage", "inventory");
        return "adminInventory";
    }

    // ========================================================================
    // mapping for inventory batch
    // ========================================================================

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

    // ========================================================================
    // mapping for product
    // ========================================================================

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
            return "redirect:/admin/inventory";
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
            return "redirect:/login";
        }

        try {

            adminServices.updateProduct(product);

            redirectAttributes.addFlashAttribute("success",
                    "product  '" + product.getName() + "' updated successfully!");

            return "redirect:/admin/inventory";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("currentPage", "users");
            return "redirect:/admin/inventory";
        }
    }

    // ========================================================================
    // mapping for Category
    // ========================================================================

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

    // ========================================================================
    // mapping for logistics ;, supplier , purchase
    // ========================================================================

    @GetMapping("/admin/logistics")
    public String adminLogistics(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        List<Supplier> suppliers = adminServices.getAllSupplier();
        List<PurchaseOrder> purchaseOrders = adminServices.getAllPurchaseOrder();

        model.addAttribute("supplier", suppliers);
        model.addAttribute("purchaseOrders", purchaseOrders);
        model.addAttribute("currentPage", "logistics");
        return "adminLogistics";
    }

    // ========================================================================
    // mapping for supplier
    // ========================================================================

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

    // ========================================================================
    // mapping for purchaseOrder
    // ========================================================================

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

    // ========================================================================
    // mapping for users
    // ========================================================================

    @GetMapping("/admin/users")
    public String adminUsers(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        List<User> users = adminServices.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentPage", "users");
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
            user.setCreatedAt(java.time.LocalDate.now());
            user.setPassword("123");
            adminServices.addUser(user);
            redirectAttributes.addFlashAttribute("success",
                    "User '" + user.getFirstName() + " " + user.getLastName() + "' created successfully!");
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
            HttpSession session) {

        if (session.getAttribute("activeUser") == null) {

            return "loginForm";
        }

        String un = String.format("%s%d", user.getFirstName(), user.getId());
        String initials = String.valueOf(user.getFirstName().charAt(0)) + user.getLastName().charAt(0);

        user.setUsername(un);

        user.setInitials(initials);

        adminServices.updateUser(user);

        return "redirect:/admin/users";

    }

    // ========================================================================
    // mapping for financials
    // ========================================================================
    @GetMapping("/admin/financials")
    public String adminFinancials(Model model, HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentPage", "financials");

        List<PurchaseOrder> purchaseOrders = adminServices.getAllPurchaseOrder();
        int completedCount = 0;
        int receivedCount = 0;
        int dueCount = 0;
        if (purchaseOrders != null) {
            for (PurchaseOrder po : purchaseOrders) {
                if (po.getPaymentStatus() == PaymentStatus.COMPLETE)
                    completedCount++;
                else if (po.getPaymentStatus() == PaymentStatus.RECEIVED)
                    receivedCount++;
                else if (po.getPaymentStatus() == PaymentStatus.DUE)
                    dueCount++;
            }
        }
        model.addAttribute("poStatusData", java.util.Arrays.asList(completedCount, receivedCount, dueCount));

        return "adminFinancials";
    }
}
