package ASLENIX.pharmacy.demo.dataTransferObject;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
public class DayEndSummaryDTO {
    private LocalDate summaryDate;
    private String cashierName;
    private Long cahierId;

    private Double grossSales;
    private Double discountsGiven;
    private Double netSales;

    private Double cashTotal;
    private Double qrTotal;
    private Double cardTotal;

    private int totalInvoices;


    private List<InvoiceSummery> invoiceSummeryList;
}
