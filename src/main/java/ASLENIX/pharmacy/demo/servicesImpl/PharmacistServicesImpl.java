package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.InvoiceStatus;
import ASLENIX.pharmacy.demo.exception.*;
import ASLENIX.pharmacy.demo.model.Customer;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.InvoiceItem;
import ASLENIX.pharmacy.demo.repository.CustomerRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceItemRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceRepository;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.services.PharmacistServices;
import ASLENIX.pharmacy.demo.utils.InvoicePDFExporter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class PharmacistServicesImpl implements PharmacistServices {
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    InventoryBatchRepository inventoryBatchRepository;
    @Autowired
    InvoiceItemRepository invoiceItemRepository;

    //=============== Invoices =================

    @Override
    public void createInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice findInvoiceById(Long id) {

        Invoice invoice =  invoiceRepository.findById(id).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if(invoice.getInvoiceStatus() == InvoiceStatus.CANCELED){
            throw new IllegalStateException("Cannot view Canceled invoices");
        }

        return invoice;

    }

    @Override
    public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {

        if (endDate.isBefore(startDate)){
            throw new InvalidDateRangeException("The start date (\" + startDate + \") must be before or equal to the " +
                    "end date (\" + endDate + \").");
        }

        return invoiceRepository.findActiveInvoicesByDateRange(startDate, endDate);
    }

    @Override
    public int countPharmacistsInvoiceOfToday(Long id) {
        return invoiceRepository.countActivePrescriptions(id,LocalDate.now());
    }

    @Override
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id).get();
        invoice.setInvoiceStatus(InvoiceStatus.CANCELED);
        invoiceRepository.save(invoice);
    }

    @Override
    public boolean isInvoiceExist(Long id) {
        return invoiceRepository.existsById(id);
    }

    @Override
    public byte[] exportInvoicePDF(Long invoiceId) throws IOException {

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        InvoicePDFExporter invoicePDFExporter = new InvoicePDFExporter(invoice);

        return invoicePDFExporter.export();
    }

    @Override
    @Transactional
    public void checkoutInvoice(Long invoiceId, HttpServletResponse response) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if(invoice.getInvoiceItemList().isEmpty()){
            throw  new InvoiceItemListEmpty("There is no item in the invoice");
        }

        List<InventoryBatch> updatedQuantityBatch = new ArrayList<>();

        for (InvoiceItem invoiceItem : invoice.getInvoiceItemList()){
            InventoryBatch inventoryBatch = getInventoryBatch(invoiceItem);

            updatedQuantityBatch.add(inventoryBatch);

        }

        inventoryBatchRepository.saveAll(updatedQuantityBatch);

        invoice.setInvoiceStatus(InvoiceStatus.PAYMENTPENDING);

    }

    private static InventoryBatch getInventoryBatch(InvoiceItem invoiceItem) {
        InventoryBatch inventoryBatch = invoiceItem.getBatch();

        long currentStock = inventoryBatch.getCurrentStock();
        Long requestedQuantity = invoiceItem.getQuantity();

        if(currentStock < requestedQuantity){
            throw new InsufficientStockException(invoiceItem.getProduct().getName()+" has insufficient stock! in " +"the " +"batch "+inventoryBatch.getBatchNumber());
        }

        inventoryBatch.setCurrentStock(currentStock-requestedQuantity);
        return inventoryBatch;
    }

    //=============== Invoices items =================

    @Override
    public boolean isInvoiceItemExist(Long itemId) {
        return invoiceItemRepository.existsById(itemId);
    }

    @Override
    public Invoice addingItemInInvoiceList(Long invoiceId, Long batchId, Long quantity) {

        if(invoiceId == null) {
            throw new InvoiceNotFoundException("Please generate an invoice first.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        InventoryBatch inventoryBatch = inventoryBatchRepository.findById(batchId).orElseThrow(()
                -> new BatchNotFoundException("The medicine batch "+batchId+"does not exist."));

        // 2. Check if this exact medicine batch is already present in the invoice list
        Optional<InvoiceItem> existingInvoiceItem = invoice.getInvoiceItemList().stream()
                .filter(item -> item.getBatch().getId().equals(batchId))
                .findFirst();

        Long finalTargetQuantity = quantity;
        InvoiceItem itemToUpdate;

        if (existingInvoiceItem.isPresent()) {
            // Item exists in cart: Calculate the combined quantity
            itemToUpdate = existingInvoiceItem.get();
            finalTargetQuantity = itemToUpdate.getQuantity() + quantity;
        } else {
            // New item: Instantiate a fresh child wrapper
            itemToUpdate = getInvoiceItem(quantity, inventoryBatch);

        }

        if(finalTargetQuantity > inventoryBatch.getCurrentStock()){
            throw new InsufficientStockException("Insufficient stock! in the batch");
        }

        itemToUpdate.setQuantity(finalTargetQuantity);

        if(existingInvoiceItem.isEmpty()){
            invoice.getInvoiceItemList().add(itemToUpdate);
        }

        invoice.recalculateTotals();

        invoiceRepository.save(invoice);

        return invoice;
    }


    private static InvoiceItem getInvoiceItem(Long quantity, InventoryBatch inventoryBatch) {

        InvoiceItem invoiceItem = new InvoiceItem();

        invoiceItem.setProduct(inventoryBatch.getProduct());
        invoiceItem.setBatch(inventoryBatch);
        invoiceItem.setQuantity(quantity);
        invoiceItem.setUnitPrice(inventoryBatch.getSellingPrice());
        invoiceItem.setDiscountPercentage(invoiceItem.getProduct().getDiscountPercentage());
        invoiceItem.setVatPercentage((double) inventoryBatch.getProduct().getCategory().getVatPercent());

        return invoiceItem;
    }


    @Override
    public Invoice removingItemInInvoiceList(Long invoiceId, Long itemId) {

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));


        boolean removed = invoice.getInvoiceItemList().removeIf(item -> item.getId().equals(itemId));

        if (removed){
            invoice.recalculateTotals();
            invoiceRepository.save(invoice);
        }


        return invoice;
    }

    @Override
    public void clearInvoiceItemList(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if (invoice.getInvoiceItemList() != null) {
            invoice.getInvoiceItemList().clear();
        } else {
            // Optional: If it IS null, Hibernate needs a collection to work with.
            invoice.setInvoiceItemList(new ArrayList<>());
        }

        invoiceRepository.save(invoice);

    }

       //=============== InventoryBatch =================

    @Override
    public List<InventoryBatch> searchAvailableMedicine(String keyword) {
        return inventoryBatchRepository.searchBatches(keyword);
    }

    @Override
    public InventoryBatch getInventoryBatchById(Long id) {
        return inventoryBatchRepository.findById(id).get();
    }

    @Override
    public boolean isBatchExist(Long id) {
        return invoiceRepository.existsById(id);
    }

    //=============== customers =================

    @Override
    public void createAndSetCustomer(Customer customer, Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        Customer savedCustomer =  customerRepository.save(customer);


        invoice.setCustomer(savedCustomer);

        invoiceRepository.save(invoice);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public void findAndSetCustomerById(Long customerId, Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        Customer customer = customerRepository.findById(customerId).orElseThrow(()->new
                        CustomerNotFoundException("Customer Id do not exists!"));

        invoice.setCustomer(customer);

        invoiceRepository.save(invoice);
    }

    @Override
    public void disconnectCustomerFromInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        invoice.setCustomer(null);

        invoiceRepository.save(invoice);
    }

    @Override
    public List<Customer> getAllCustomer() {
        return customerRepository.findAll();
    }
}
