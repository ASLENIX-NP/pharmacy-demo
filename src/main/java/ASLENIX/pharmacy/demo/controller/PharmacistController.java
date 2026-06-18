package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.InvoiceStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import ASLENIX.pharmacy.demo.exception.*;
import ASLENIX.pharmacy.demo.model.Customer;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.servicesImpl.PharmacistServicesImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class PharmacistController {

    @Autowired
    PharmacistServicesImpl pharmacistServices;

//========================================================================
    // mapping for Dashboard
//========================================================================



    @GetMapping("/pharmacist/dashboard")
    String pharmacistDashboard(
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            Model model, HttpSession session){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("activeUser");
        model.addAttribute("invoiceCount", pharmacistServices.countPharmacistsInvoiceOfToday(user.getId()));


        if(invoiceId == null){
            model.addAttribute("activeInvoice", null);
            model.addAttribute("invoice", null);
            model.addAttribute("isAllowedPrinting", false);

        }else {

            try {
                Invoice invoice = pharmacistServices.findInvoiceById(invoiceId);
                model.addAttribute("activeInvoice", true);

                if(invoice.getInvoiceItemList().isEmpty()){
                    model.addAttribute("isCartEmpty", true);
                }else {
                    model.addAttribute("isCartEmpty", false );

                }

                if(invoice.getInvoiceStatus() != InvoiceStatus.PAYMENTPENDING){
                    model.addAttribute("isAllowedPrinting", false);
                }else{
                    model.addAttribute("isAllowedPrinting", true);
                }

                model.addAttribute("invoice", invoice);

            }catch (InvoiceNotFoundException | IllegalStateException e){
                model.addAttribute("invoiceError", e.getMessage());
            }



        }


        return "pharmacistDashboard";
    }

    @GetMapping("/pharmacist/dashboard/invoice/print")
    Object pharmacistDashboard(
            @RequestParam(value = "invoiceId") Long invoiceId,
            Model model,HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        try {
            byte[] invoicePDFByte = pharmacistServices.exportInvoicePDF(invoiceId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=invoice_" + invoiceId + "-"+LocalDateTime.now() + ".pdf")
                    .body(invoicePDFByte);
        }
        catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/dashboard";
        } catch (IOException e) {
            throw new RuntimeException("Fatal engine error during PDF rendering", e);        }

    }




    //========================================================================
    // mapping for New sales
//========================================================================


    @GetMapping("/pharmacist/newSales")
    String pharmacistNewSalesGet(
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            Model model,HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        if(invoiceId == null){
            model.addAttribute("activeInvoice", null);
            model.addAttribute("invoice", null);
        }else {
            model.addAttribute("activeInvoice", true);
            Invoice invoice = pharmacistServices.findInvoiceById(invoiceId);

            if(invoice.getInvoiceItemList().isEmpty()){
                model.addAttribute("isCartEmpty", true);
            }else {
                model.addAttribute("isCartEmpty", false );

            }
            model.addAttribute("invoice", invoice);
        }

        return "pharmacistNewSale";
    }

    @PatchMapping("/pharmacist/newSales/invoice/cancel")
    String pharmacistCancelInvoiceGet(
            @RequestParam(value = "invoiceId") Long invoiceId,
            Model model, HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }


        pharmacistServices.cancelInvoice(invoiceId);


        return "redirect:/pharmacist/newSales";
    }

    @PostMapping("/pharmacist/newSales/invoice/generate")
    String pharmacistGenerateInvoicePost(Model model, HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        Invoice invoice = new Invoice();
        User pharmacist = (User) session.getAttribute("activeUser");

        invoice.setPharmacist(pharmacist);
        invoice.setTransactionDate(LocalDate.now());
        invoice.setInvoiceStatus(InvoiceStatus.INVOICEPENDING);
        invoice.setPaymentMethod(PaymentMethod.CASH);

        pharmacistServices.createInvoice(invoice);


        return "redirect:/pharmacist/newSales?invoiceId=" + invoice.getId();
    }

    @GetMapping("/pharmacist/newSales/invoice/search")
    String pharmacistSearchInvoiceGet(
            @RequestParam(value = "searchInvoiceInput")Long invoiceId,
            Model model, HttpSession session,RedirectAttributes redirectAttributes){


        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            Invoice invoice = pharmacistServices.findInvoiceById(invoiceId);

            if(invoice.getInvoiceStatus() != InvoiceStatus.INVOICEPENDING){
                throw new IllegalStateException("Invoice already checked out");
            }
            return "redirect:/pharmacist/newSales?invoiceId=" + invoice.getId();


        }catch (InvoiceNotFoundException | IllegalStateException e){
            redirectAttributes.addFlashAttribute("searchError",e.getMessage());
            return "redirect:/pharmacist/newSales";

        }

    }

    @GetMapping("/pharmacist/newSales/product/find")
    String pharmacistFindProductGet(
            @RequestParam("query") String query ,
            @RequestParam(value = "invoiceId", required= false) Long invoiceId,
            HttpSession session, Model model, RedirectAttributes redirectAttributes){


        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        List<InventoryBatch> inventoryBatches = pharmacistServices.searchAvailableMedicine(query);

        model.addAttribute("inventoryBatch" , inventoryBatches);


        return pharmacistNewSalesGet(invoiceId,model,session);
    }


    @PostMapping("/pharmacist/newSales/invoice/add")
    String pharmacistAddProductInInvoiceItemPost(
            @RequestParam("batchId") Long batchId,
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            @RequestParam("quantity") Long quantity
            ,Model model, HttpSession session, RedirectAttributes redirectAttributes){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }


        try {
            Invoice updatedInvoice = pharmacistServices.addingItemInInvoiceList(invoiceId,batchId,quantity);
            return "redirect:/pharmacist/newSales?invoiceId=" + updatedInvoice.getId();

        } catch (InvoiceNotFoundException e) {
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }catch (BatchNotFoundException | InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales?invoiceId=" +invoiceId;
        }

    }


    @PostMapping("/pharmacist/newSales/invoice/remove")
    String pharmacistAddProductInInvoiceItemPost(
            @RequestParam("itemId") Long itemId ,
            @RequestParam(value = "invoiceId", required = false) Long invoiceId
            ,Model model, HttpSession session, RedirectAttributes redirectAttributes){


        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try{
            Invoice updatedInvoice = pharmacistServices.removingItemInInvoiceList(invoiceId,itemId);
            return "redirect:/pharmacist/newSales?invoiceId=" + updatedInvoice.getId();
        }
        catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }

    }



    @GetMapping("/pharmacist/newSales/customer/search")
    public String searchCustomerGet(
            @RequestParam("customerId") Long customerId,
            @RequestParam("invoiceId") Long invoiceId,
            HttpSession session,RedirectAttributes redirectAttributes) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            pharmacistServices.findAndSetCustomerById(customerId,invoiceId);
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;

        }
        catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }
        catch (CustomerNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;
        }

    }

    @PostMapping("/pharmacist/newSales/customer/add")
    public String addCustomerPost(
            @ModelAttribute Customer customer ,
            @RequestParam("invoiceId")Long invoiceId,
            HttpSession session, RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";

        }

        try{
            pharmacistServices.createAndSetCustomer(customer, invoiceId);
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;

        }catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }

    }

    @PostMapping("/pharmacist/newSales/customer/edit")
    public String editCustomerPost(
            @ModelAttribute Customer customer,
            @RequestParam("invoiceId") Long invoiceId,
            HttpSession session , RedirectAttributes redirectAttributes) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }


        try {
            // Pass the bound object straight to your dedicated service
            pharmacistServices.updateCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer profile updated.");
        } catch (PharmacyBusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;


    }


    @PatchMapping("/pharmacist/newSales/customer/disconnect")
    String pharmacistDisconnectCustomerInvoicePatch(
            @RequestParam(value = "invoiceId") Long invoiceId,
            RedirectAttributes redirectAttributes,HttpSession session){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try{
            pharmacistServices.disconnectCustomerFromInvoice(invoiceId);
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;

        }catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }
    }

    @PatchMapping("/pharmacist/newSales/invoice/clear")
    String pharmacistClearInvoiceItemList(
            @RequestParam(value = "invoiceId") Long invoiceId,
            RedirectAttributes redirectAttributes,
            HttpSession session){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try{
            pharmacistServices.clearInvoiceItemList(invoiceId);
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;

        }catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }
    }


    @PostMapping("/pharmacist/newSales/invoice/checkout")
    String pharmacistCheckoutInvoicePost(
            @RequestParam(value = "invoiceId") Long invoiceId,
            RedirectAttributes redirectAttributes,
            HttpSession session, HttpServletResponse response){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try{
            pharmacistServices.checkoutInvoice(invoiceId,response);
            redirectAttributes.addFlashAttribute("invoiceSuccess", "invoice no " +invoiceId+" successfully checked " +
                    "out ");
            return "redirect:/pharmacist/dashboard?invoiceId="+invoiceId;

        }catch (InvoiceNotFoundException e ){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales";
        }catch ( InvoiceItemListEmpty | InsufficientStockException e ){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/pharmacist/newSales?invoiceId=" + invoiceId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//========================================================================
    // mapping for sales history
//========================================================================


    @GetMapping("/pharmacist/salesHistory")
    String pharmacistSalesHistory(
            @RequestParam(value = "startDate" , required = false) LocalDate startDate ,
            @RequestParam(value = "endDate" , required = false) LocalDate endDate,
            Model model,HttpSession session,RedirectAttributes redirectAttributes){

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        if(startDate == null){
            startDate= LocalDate.now();
        }

        if (endDate == null){
            endDate = LocalDate.now().plusDays(1);
        }

        try {

            List<Invoice> invoiceList = pharmacistServices.getInvoicesByDateRange(startDate,endDate);


            if(invoiceList.isEmpty()){
                model.addAttribute("isInvoiceEmpty",true);

            }else {
                model.addAttribute("isInvoiceEmpty",false);
            }

            model.addAttribute("invoice",invoiceList);

            return "pharmacistSalesHistory";
        }
        catch (InvalidDateRangeException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            model.addAttribute("isInvoiceEmpty",true);
            model.addAttribute("invoice",null);
            return "pharmacistSalesHistory";

        }


    }




}
