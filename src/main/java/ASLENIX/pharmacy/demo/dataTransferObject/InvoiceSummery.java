package ASLENIX.pharmacy.demo.dataTransferObject;

import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InvoiceSummery {

    private String invoiceNumber;
    private PaymentMethod paymentMethod;
    private Double totalAmount;
}
