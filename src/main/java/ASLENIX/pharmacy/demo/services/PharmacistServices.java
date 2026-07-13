package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.model.Customer;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface PharmacistServices {

//============== Invoices  =================

    void createInvoice(Invoice invoice);

    Invoice findInvoiceById( Long id);


    List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate);

    int countPharmacistsInvoiceOfToday(Long id);

    void cancelInvoice(Long id);

    boolean isInvoiceExist(Long id);

    byte[] exportInvoicePDF(Long invoiceId) throws IOException;

    void checkoutInvoice( Long invoiceId ,HttpServletResponse response) throws IOException;


//============== Invoices  =================

    boolean isInvoiceItemExist(Long itemId);

    Invoice addingItemInInvoiceList(Long invoiceId, Long batchId , Long quantity);

    Invoice removingItemInInvoiceList(Long invoiceId, Long itemId  );

    void clearInvoiceItemList(Long invoiceId);


//============== Invoices  =================

    List<InventoryBatch> searchAvailableMedicine(String keyword);


    InventoryBatch getInventoryBatchById(Long id);

    boolean isBatchExist(Long id);

//============== Invoices  =================

    void  createAndSetCustomer(Customer customer , Long invoiceId);

    void  updateCustomer(Customer customer);

    void findAndSetCustomerById(Long customerId, Long invoiceId);

    void disconnectCustomerFromInvoice(Long invoiceId);

    List<Customer> getAllCustomer();



}
