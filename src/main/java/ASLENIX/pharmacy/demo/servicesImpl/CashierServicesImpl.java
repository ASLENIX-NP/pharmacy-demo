package ASLENIX.pharmacy.demo.servicesImpl;

import ASLENIX.pharmacy.demo.Enums.InvoiceStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import ASLENIX.pharmacy.demo.dataTransferObject.DayEndSummaryDTO;
import ASLENIX.pharmacy.demo.dataTransferObject.InvoiceSummery;
import ASLENIX.pharmacy.demo.exception.InsufficientPaymentException;
import ASLENIX.pharmacy.demo.exception.InvoiceNotFoundException;
import ASLENIX.pharmacy.demo.model.InventoryBatch;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.InvoiceItem;
import ASLENIX.pharmacy.demo.model.User;
import ASLENIX.pharmacy.demo.repository.InventoryBatchRepository;
import ASLENIX.pharmacy.demo.repository.InvoiceRepository;
import ASLENIX.pharmacy.demo.services.CashierServices;
import ASLENIX.pharmacy.demo.utils.BillPDFExporter;
import ASLENIX.pharmacy.demo.utils.DayClosingReportPDFExport;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class CashierServicesImpl implements CashierServices {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    InventoryBatchRepository inventoryBatchRepository;


    @Override
    public int countCashierInvoiceCompleteToday(Long cashierId) {
        return invoiceRepository.countCashierCompletePrescriptions(cashierId,LocalDate.now());
    }

    @Override
    public List<Invoice> getInvoicesPaymentPendingToday() {
        return invoiceRepository.findPaymentPendingInvoicesByDate(LocalDate.now());
    }

    @Override
    public byte[] exportPDFBill(Long invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        BillPDFExporter billPDFExporter = new BillPDFExporter(invoice);

        return billPDFExporter.export();


    }


    @Override
    public Invoice findInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if(invoice.getInvoiceStatus() == InvoiceStatus.CANCELED || invoice.getInvoiceStatus() == InvoiceStatus.INVOICEPENDING){

            throw new IllegalStateException("Cannot view PENDING or CANCELED invoices.");

        }

        return invoice;
    }

    @Transactional
    @Override
    public void discardInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        if(invoice.getInvoiceStatus() == InvoiceStatus.CANCELED || invoice.getInvoiceStatus() == InvoiceStatus.COMPLETE){
            throw  new IllegalStateException("Cannot discard CANCELED or COMPLETED invoices ");
        }

        List<InventoryBatch> updatedBranch = new ArrayList<>();

        for (InvoiceItem item : invoice.getInvoiceItemList()){
            InventoryBatch batch  = item.getBatch();

            if (batch != null) {
                // Add the quantities back to live stock
                Long restoredQty = batch.getCurrentStock() + item.getQuantity();
                batch.setCurrentStock(restoredQty);

                updatedBranch.add(batch);
            }
        }

        if (!updatedBranch.isEmpty()){
            inventoryBatchRepository.saveAll(updatedBranch);
        }

        invoice.setInvoiceStatus(InvoiceStatus.CANCELED);
        invoice.setAmountReceived(0.00);
        invoice.setChangeReturned(0.00);
        invoice.setPaymentMethod(null);


        invoiceRepository.save(invoice);
    }

    @Transactional
    @Override
    public void completePayment(Long invoiceId, PaymentMethod paymentMethod, Double amountPaid,User cashier) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new InvoiceNotFoundException("Invoice not found"));

        Double grandTotal = invoice.getGrandTotal();

        if(amountPaid < grandTotal){

            throw new InsufficientPaymentException("Amount received cannot be less than the grand total.");
        }


        invoice.setCashier(cashier);
        invoice.setInvoiceStatus(InvoiceStatus.COMPLETE);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setAmountReceived(amountPaid);
        invoice.setChangeReturned(amountPaid-grandTotal);

        invoiceRepository.save(invoice);

    }

    @Override
    public DayEndSummaryDTO getDayEndSummaryDTO(User cashier) {
        DayEndSummaryDTO desDOT = new DayEndSummaryDTO();

        Double discountsGiven= 0.0;
        Double grossSales= 0.0;
        Double netSales= 0.0;

        Double cashTotal= 0.0;
        Double qrTotal= 0.0;
        Double cardTotal= 0.0;

        List<InvoiceSummery> invoiceSummeryList = new ArrayList<>();

        List<Invoice> invoiceList = invoiceRepository.findCompleteInvoicesByDateAndCashier(LocalDate.now(),cashier.getId());

        for(Invoice invoice : invoiceList){
            grossSales += invoice.getSubTotal();
            discountsGiven += invoice.getDiscountAmount();
            netSales += invoice.getGrandTotal();

            switch (invoice.getPaymentMethod()){
                case CASH -> cashTotal += invoice.getGrandTotal();
                case E_WALLET -> qrTotal += invoice.getGrandTotal();
                case CREDIT_CARD -> cardTotal+= invoice.getGrandTotal();
            }

            InvoiceSummery invoiceSummery = new InvoiceSummery();

            invoiceSummery.setInvoiceNumber(invoice.getInvoiceNumber());
            invoiceSummery.setPaymentMethod(invoice.getPaymentMethod());
            invoiceSummery.setTotalAmount(invoice.getGrandTotal());

            invoiceSummeryList.add(invoiceSummery);

        }
        desDOT.setSummaryDate(LocalDate.now());
        desDOT.setCahierId(cashier.getId());
        desDOT.setCashierName(cashier.getFirstName() +" "  + cashier.getLastName());
        desDOT.setGrossSales(grossSales);
        desDOT.setDiscountsGiven(discountsGiven);
        desDOT.setNetSales(netSales);
        desDOT.setCashTotal(cashTotal);
        desDOT.setQrTotal(qrTotal);
        desDOT.setCardTotal(cardTotal);
        desDOT.setTotalInvoices(invoiceSummeryList.size());
        desDOT.setInvoiceSummeryList(invoiceSummeryList);


        return desDOT;
    }

    @Override
    public byte[] exportDayEndSummaryPDF(DayEndSummaryDTO dayEndSummaryDTO) throws IOException {

        DayClosingReportPDFExport dayClosingReportPDFExport = new DayClosingReportPDFExport(dayEndSummaryDTO);

        return dayClosingReportPDFExport.export();


    }


}
