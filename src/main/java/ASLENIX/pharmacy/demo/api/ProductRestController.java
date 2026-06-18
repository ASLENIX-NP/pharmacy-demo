package ASLENIX.pharmacy.demo.api;

import ASLENIX.pharmacy.demo.dataTransferObject.DayEndSummaryDTO;
import ASLENIX.pharmacy.demo.exception.InvoiceNotFoundException;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.Product;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.UserRepository;
import ASLENIX.pharmacy.demo.services.CashierServices;
import ASLENIX.pharmacy.demo.servicesImpl.AdminServicesImpl;
import ASLENIX.pharmacy.demo.servicesImpl.PharmacistServicesImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ProductRestController {
    @Autowired
    private AdminServicesImpl adminServices;


    @Autowired
    private PharmacistServicesImpl pharmacistServices;

    @Autowired
    private CashierServices cashierServices;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/api/products")
    public List<Product>getAllProducts(){

        return adminServices.getAllProduct();
    }




    @GetMapping("/api/pharmacist/newSales/findProduct")
    List<InventoryBatch> pharmacistFindProductGetAPI(@RequestParam("query") String query){
        return pharmacistServices.searchAvailableMedicine(query);
    }

    @GetMapping("/api/pharmacist/newSales/findInvoice")
    Invoice pharmacistNewSalesGet(@RequestParam(value = "invoiceId", required = false) Long invoiceId){


        return pharmacistServices.findInvoiceById(invoiceId);
    }


    @GetMapping("/api/pharmacist/newSales/exportInvoicePDF")

    public void pharmacyInvoiceExportPDF(@RequestParam("invoiceId") Long invoiceId , HttpServletResponse response){

        try{
            pharmacistServices.exportInvoicePDF(invoiceId);
        }
        catch (InvoiceNotFoundException e) {
            System.out.println( e.getMessage());
        }catch (IOException e){
            System.out.println(e.getMessage());
        }


    }
    @GetMapping("/api/pharmacist/salesHistory")
    List<Invoice> pharmacistSalesHistory(@RequestParam(value = "start" , required = false) LocalDate startDate ,
                                     @RequestParam(value = "end" , required = false) LocalDate endDate) {


        if(startDate == null){
            startDate= LocalDate.now();
        }

        if (endDate == null){
            endDate = LocalDate.now().plusDays(1);
        }




        return pharmacistServices.getInvoicesByDateRange(startDate, endDate);
    }


    @GetMapping("/api/cashier/dashboard")
     List<Invoice> cahierDashboardGet(Model model , HttpSession session){


        return cashierServices.getInvoicesPaymentPendingToday();
    }

    @GetMapping("/api/cashier/viewBill/invoice/print")
    Object pharmacistDashboard(@RequestParam(value = "invoiceId") Long invoiceId) throws IOException {


            byte[] invoicePDFByte = cashierServices.exportPDFBill(invoiceId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=bill_" + invoiceId + "-"+ LocalDateTime.now() + ".pdf")
                    .body(invoicePDFByte);


    }


    @GetMapping("/api/pharmacist/dashboard/invoice/print")
    Object pharmacistDashboardPrintdf(@RequestParam(value = "invoiceId") Long invoiceId) throws IOException {


            byte[] invoicePDFByte = pharmacistServices.exportInvoicePDF(invoiceId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=invoice_" + invoiceId + "-"+LocalDateTime.now() + ".pdf")
                    .body(invoicePDFByte);

    }

    @GetMapping("/api/cashier/dayClosing")
    DayEndSummaryDTO cahierDayClosingGet(@RequestParam(value = "userName") String username){

            String password = "123";
            User cashier = userRepository.findByUsernameAndPassword(username,password);


        return cashierServices.getDayEndSummaryDTO(cashier);
    }

    @GetMapping("/api/cashier/dayClosing/print")
    Object cahierPrintGet() throws IOException {

        String password = "123";
        String username = "cashier8";
        User cashier = userRepository.findByUsernameAndPassword(username,password);

        DayEndSummaryDTO dayEndSummaryDTO = cashierServices.getDayEndSummaryDTO(cashier);

        byte[] report = cashierServices.exportDayEndSummaryPDF(dayEndSummaryDTO);


        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=bill_" + cashier.getId() + "-"+ LocalDateTime.now() + ".pdf")
                .body(report);

    }



    @GetMapping("/api/cashier/viewBill")
    Object cahierViewBillGet(
            @RequestParam(value = "invoiceId", required = false) Long invoiceId) {


        if (invoiceId == null) {
            return "no invoice given ";


        } else {

            try {

                return cashierServices.findInvoiceById(invoiceId);


            } catch (InvoiceNotFoundException | IllegalStateException e) {
                return e.getMessage();
            }

        }
    }

}
