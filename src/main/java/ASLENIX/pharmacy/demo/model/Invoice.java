package ASLENIX.pharmacy.demo.model;

import ASLENIX.pharmacy.demo.Enums.InvoiceStatus;
import ASLENIX.pharmacy.demo.Enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice_tbl")
@Getter
@Setter
@ToString
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber;

    @ManyToOne //  FIX: Many batches can belong to One Product
    @JoinColumn(name = "customer_id" )
    private Customer customer;

    // 🥼 The clinician who selected the medicine
    @ManyToOne
    @JoinColumn(name = "pharmacist_id", nullable = false)
    private User pharmacist;

    // 💵 The money-handler who collects the cash (Null until paid!)
    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate transactionDate;


    private Double subTotal;

    private Double discountAmount;
    private Double vatAmount;
    private  Double grandTotal;

    private Double amountReceived;
    private Double changeReturned;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceItem> invoiceItemList = new ArrayList<>();


    @PostPersist
    public void generateInvoiceNumber() {
        // This runs automatically right after the entity is saved and gets its ID
        this.invoiceNumber = String.format("INV-%06d", this.id);
    }

    private Double roundOffOne(Double value) {
        if (value == null) {
            return 0.0; // Or return null depending on your business logic
        }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void recalculateTotals(){
        double totalSubTotal = 0.0;
        double totalDiscountAmount = 0.0;
        double totalVatAmount = 0.0;
        double totalGrandTotal = 0.0;

        for(InvoiceItem invoiceItems :invoiceItemList){
            Double subTotal = invoiceItems.getQuantity() *invoiceItems.getUnitPrice();
            invoiceItems.setSubTotal(subTotal);

            Double discountAmount = subTotal * invoiceItems.getDiscountPercentage()/100;
            invoiceItems.setDiscountAmount(discountAmount);

            double vatAmount = (subTotal -discountAmount) * invoiceItems.getVatPercentage()/100;
            invoiceItems.setVatAmount(vatAmount);

            double lineTotal =subTotal -discountAmount + vatAmount;
            invoiceItems.setLineTotal(lineTotal);

            totalSubTotal += subTotal;
            totalDiscountAmount += discountAmount;
            totalVatAmount += vatAmount;
            totalGrandTotal += lineTotal;
        }

        this.subTotal = roundOffOne(totalSubTotal);
        this.discountAmount = roundOffOne(totalDiscountAmount);
        this.vatAmount = roundOffOne(totalVatAmount);
        this.grandTotal = roundOffOne(totalGrandTotal);

    }

}
