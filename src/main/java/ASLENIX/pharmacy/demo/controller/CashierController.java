package ASLENIX.pharmacy.demo.controller;

import ASLENIX.pharmacy.demo.Enums.InvoiceStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import ASLENIX.pharmacy.demo.dataTransferObject.DayEndSummaryDTO;
import ASLENIX.pharmacy.demo.exception.InsufficientPaymentException;
import ASLENIX.pharmacy.demo.exception.InvoiceNotFoundException;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.servicesImpl.CashierServicesImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CashierController {

    @Autowired
    CashierServicesImpl cashierServices;

     //======= mapping for Dashboard =======

    @GetMapping("/cashier/dashboard")
    String cahierDashboardGet(Model model , HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("activeUser");


        List<Invoice> invoicePaymentPending = cashierServices.getInvoicesPaymentPendingToday();

        int numberOfPaymentPendingInvoice = invoicePaymentPending.size();
        int numberOfCompletedInvoice= cashierServices.countCashierInvoiceCompleteToday(user.getId());

        model.addAttribute("completedInvoiceCount",numberOfCompletedInvoice );
        model.addAttribute("pendingInvoiceCount", numberOfPaymentPendingInvoice);
        model.addAttribute("invoicePaymentPending", invoicePaymentPending);



        return "cashierDashboard";
    }

    //======= mapping for view bill =======

    @GetMapping("/cashier/viewBill")
    String cahierViewBillGet(
            @RequestParam(value = "invoiceId", required = false) Long invoiceId,
            Model model ,RedirectAttributes redirectAttributes ,HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        if(invoiceId == null){
            model.addAttribute("activeInvoice", null);
            model.addAttribute("invoice", null);


        }else {

            try {

                Invoice invoice = cashierServices.findInvoiceById(invoiceId);
                model.addAttribute("invoice", invoice);


                model.addAttribute("isFormDisabled", (invoice.getInvoiceStatus() != InvoiceStatus.PAYMENTPENDING));

                model.addAttribute("isFormPrintable",invoice.getInvoiceStatus() != InvoiceStatus.COMPLETE);

                model.addAttribute("activeInvoice", true);

                model.addAttribute("paymentOptions" , PaymentMethod.values());



            }catch (InvoiceNotFoundException | IllegalStateException e){
                model.addAttribute("invoiceError", e.getMessage());

            }

        }

        return "cashierViewBill";
    }

    @PostMapping("/cashier/viewBill/invoice/complete")
    String cashierInvoiceCompletePost(
            @RequestParam("invoiceId") Long invoiceId,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            @RequestParam("amountPaid") Double amountPaid,
             Model model, RedirectAttributes redirectAttributes , HttpSession session
    ){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            User cashier  = (User) session.getAttribute("activeUser");
            cashierServices.completePayment(invoiceId,paymentMethod,amountPaid, cashier);
            redirectAttributes.addFlashAttribute("invoiceSuccess", "Invoice transaction completed successfully ");


            return "redirect:/cashier/viewBill?invoiceId="+invoiceId;

        }catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/cashier/viewBill";
        }
        catch (InsufficientPaymentException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());

            return "redirect:/cashier/viewBill?invoiceId="+invoiceId;
        }
    }

    @PostMapping("/cashier/viewBill/invoice/discard")
    String cashierInvoiceDiscardPost(
            @RequestParam("invoiceId") Long invoiceId,
            Model model, RedirectAttributes redirectAttributes , HttpSession session
    ){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {

            cashierServices.discardInvoice(invoiceId);
            redirectAttributes.addFlashAttribute("invoiceSuccess","Invoice discarded successfully" );

            return "redirect:/cashier/viewBill";

        }catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/cashier/viewBill";
        }
        catch ( IllegalStateException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());

            return "redirect:/cashier/viewBill?invoiceId="+invoiceId;
        }
    }

    @GetMapping("/cashier/viewBill/invoice/print")
    Object pharmacistDashboard(
            @RequestParam(value = "invoiceId") Long invoiceId,
            Model model,HttpSession session,RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        try {
            byte[] invoicePDFByte = cashierServices.exportPDFBill(invoiceId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=bill_" + invoiceId + "-"+ LocalDateTime.now() + ".pdf")
                    .body(invoicePDFByte);
        }
        catch (InvoiceNotFoundException e){
            redirectAttributes.addFlashAttribute("invoiceError", e.getMessage());
            return "redirect:/cashier/viewBill";
        } catch (IOException e) {
            throw new RuntimeException("Fatal engine error during PDF rendering", e);        }

    }

      //======= mapping for day closing  =======

    @GetMapping("/cashier/dayClosing")
    String cahierDayClosingGet(Model model , HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            User cashier = (User) session.getAttribute("activeUser");
            DayEndSummaryDTO dayEndSummaryDTO = cashierServices.getDayEndSummaryDTO(cashier);

            model.addAttribute("data"  ,dayEndSummaryDTO);

        }

        catch (InvoiceNotFoundException e){

        }

        return "cashierDayClosing";
    }

    @GetMapping("/cashier/dayClosing/print")
    Object cahierDayClosingPrintReportGet(Model model , HttpSession session){
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        try {
            User user = (User) session.getAttribute("activeUser");
            DayEndSummaryDTO dayEndSummaryDTO = cashierServices.getDayEndSummaryDTO(user);
            byte[] billPDFByte = cashierServices.exportDayEndSummaryPDF(dayEndSummaryDTO);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=invoice_" + user.getId() + "-"+LocalDateTime.now() + ".pdf")
                    .body(billPDFByte);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
