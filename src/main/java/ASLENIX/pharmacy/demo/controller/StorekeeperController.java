package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.exception.InventoryBatchNotFoundException;
import ASLENIX.pharmacy.demo.model.*;
import ASLENIX.pharmacy.demo.services.StorekeeperServices;
import ASLENIX.pharmacy.demo.Enums.UserRole;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StorekeeperController {

    @Autowired
    private StorekeeperServices storekeeperServices;
    private boolean isNotStoreKeeper(HttpSession session) {
        User activeUser = (User) session.getAttribute("activeUser");

        return activeUser == null ||
                activeUser.getRole() != UserRole.STOREKEEPER;
    }


    //======= mapping for dashboard =======

    @GetMapping("/storekeeper/dashboard")
    public String storekeeperDashboard(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        model.addAttribute("stats", storekeeperServices.getDashboardStats());
        return "storekeeperDashboard";
    }


    //======= mapping for inventory =======

    @GetMapping("/storekeeper/inventory")
    public String storekeeperInventory(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        model.addAttribute("inventoryBatches", storekeeperServices.getAllInventoryBatch());
        return "storekeeperInventory";
    }


    @GetMapping("/storekeeper/inventory/add")
    public String storekeeperAddStockLedgerGet(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }


        List<Product> products = storekeeperServices.getAllProduct();
        List<PurchaseOrder> purchaseOrders = storekeeperServices.getALlPurchaseOrder();

        model.addAttribute("products", products);
        model.addAttribute("purchaseOrder",purchaseOrders);


        return "addStorekeeperStockLedgerForm";
    }

    @PostMapping("/storekeeper/inventory/add")
    public String storekeeperAddStockLedgerPost(
            @ModelAttribute InventoryBatch inventoryBatch,
            HttpSession session,Model model,RedirectAttributes redirectAttributes) {

        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }

        try {

            storekeeperServices.addInventoryBatch(
                    inventoryBatch.getProduct(),
                    inventoryBatch.getExpiryDate(),
                    inventoryBatch.getQuantityReceived(),
                    inventoryBatch.getPurchaseOrder());


            redirectAttributes.addFlashAttribute("success",
                    "new batch of '" + inventoryBatch.getProduct().getName() +"' created successfully!");


            return "redirect:/storekeeper/inventory";


        } catch (Exception e) {
            model.addAttribute("error", "Failed to create user: " + e.getMessage());

            return "redirect:/storekeeper/inventory";
        }
    }


    //======= mapping for productsRacks =======

    @GetMapping("/storekeeper/productsRacks")
    public String storekeeperProductsRacks(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        model.addAttribute("products", storekeeperServices.getAllProduct());
        return "storekeeperProductsRacks";
    }


    @PostMapping("/storekeeper/productsRacks/edit")
    public String updateRackLocation(
            @RequestParam("productId") Long productId,
            @RequestParam("rackLocation") String rackLocation,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        try {
            storekeeperServices.updateProductLocation(productId, rackLocation);
            redirectAttributes.addFlashAttribute("successMessage", "product id"+productId+" Rack location updated " +
                    "successfully! to "+rackLocation);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating rack location: " + e.getMessage());
        }
        return "redirect:/storekeeper/productsRacks";
    }


    //======= mapping for pullRequests =======

    @GetMapping("/storekeeper/pullRequests")
    public String storekeeperPullRequests(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        model.addAttribute("pullRequests", storekeeperServices.getAllInventoryBatch().stream()
                .filter(batch -> batch.getBatchApprovalStatus().toString().equals("PENDING_REMOVAL"))
                .toList());
        return "storekeeperPullRequests";
    }

    @PostMapping("/storekeeper/pullRequests/confirm")
    public String confirmRemoval(
            @RequestParam("batchId") Long batchId,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        try {
            storekeeperServices.pullBatchOffRack(batchId);
            redirectAttributes.addFlashAttribute("successMessage", "Batch "+batchId+" removal confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error confirming removal: " + e.getMessage());
        }

        return "redirect:/storekeeper/pullRequests";
    }


    //======= mapping for orderDeliver =======

    @GetMapping("/storekeeper/orderDeliver")
    public String storekeeperDeliverGet(Model model, HttpSession session) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }

        model.addAttribute("purchaseOrder", storekeeperServices.getCompletedPurchaseOrder());

        return "storekeeperDelivery";
    }

    @PostMapping("/storekeeper/orderDeliver")
    public String storekeeperDeliverPost(
            @RequestParam("orderId") Long purchaseId,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (isNotStoreKeeper(session)) {
            return "redirect:/login";
        }
        try {
            storekeeperServices.conformDeliverOfOrder(purchaseId);
            redirectAttributes.addFlashAttribute("successMessage", "Order "+purchaseId+"delivery confirmed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error confirming delivery: " + e.getMessage());
        }
        return "redirect:/storekeeper/orderDeliver";
    }
}
