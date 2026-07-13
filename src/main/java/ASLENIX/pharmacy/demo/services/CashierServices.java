package ASLENIX.pharmacy.demo.services;

import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import ASLENIX.pharmacy.demo.dataTransferObject.DayEndSummaryDTO;
import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.User;

import java.io.IOException;
import java.util.List;

public interface CashierServices {



    int countCashierInvoiceCompleteToday (Long cashierId);

    List<Invoice> getInvoicesPaymentPendingToday();


    byte[] exportPDFBill(Long invoiceId) throws IOException;


    Invoice findInvoiceById(Long invoiceId);

    void discardInvoice(Long invoiceId);

    void completePayment(Long invoiceId, PaymentMethod paymentMethod , Double amountPaid,User cashier);

    DayEndSummaryDTO getDayEndSummaryDTO (User cashier);

    public byte[] exportDayEndSummaryPDF(DayEndSummaryDTO dayEndSummaryDTO) throws IOException;

}
