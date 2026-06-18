package ASLENIX.pharmacy.demo.utils;

import ASLENIX.pharmacy.demo.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.*;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;


public class BillPDFExporter {

    // Compact Aesthetic Styling Constants
    private final Color PRIMARY_ACCENT = new Color(26, 54, 93);
    private final Color SECONDARY_ACCENT = new Color(74, 85, 104);
    private final Color BORDER_COLOR = new Color(226, 232, 240);
    private final Color HIGHLIGHT_BG = new Color(247, 250, 252);
    private final Color SUCCESS_COLOR = new Color(22, 101, 52);

    private final Invoice invoice;

    public BillPDFExporter(Invoice invoice) {
        this.invoice = invoice;
    }

    public byte[] export() throws IOException {
        // Tightened margins to maximize the printable footprint on A5 Landscape
        Document document = new Document(PageSize.A5.rotate(), 24, 24, 24, 24);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // Downscaled Typography Configuration
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_ACCENT);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_ACCENT);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font standardFont = FontFactory.getFont(FontFactory.HELVETICA, 8, SECONDARY_ACCENT);
        Font boldValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, SUCCESS_COLOR);

        // =========================================================================
        // 1. COMPACT HEADER BLOCK
        // =========================================================================
        PdfPTable brandTable = new PdfPTable(2);
        brandTable.setWidthPercentage(100);
        brandTable.setWidths(new float[]{1.5f, 8.5f});

        // Logo inclusion
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        try {
            Image logo = Image.getInstance("src/main/resources/static/images/AB+.jpg");
            logo.scaleToFit(24, 24);
            logoCell.addElement(logo);
        } catch (Exception e) { /* Fallback gracefully if image path isn't loaded */ }
        brandTable.addCell(logoCell);

        // Corporate info text block
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.addElement(new Paragraph("XYZ PHARMACY PVT. LTD.", companyFont));

        Paragraph contactLine = new Paragraph("New Road, Kathmandu, Nepal  |  PAN: 601234567  |  Phone: +977 90000000", standardFont);
        infoCell.addElement(contactLine);
        brandTable.addCell(infoCell);
        document.add(brandTable);

        addHorizontalDivider(document, PRIMARY_ACCENT, 1.0f, 4f);

        Paragraph docTitle = new Paragraph("TAX INVOICE", titleFont);
        docTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(docTitle);

        addHorizontalDivider(document, PRIMARY_ACCENT, 1.0f, 4f);

        // =========================================================================
        // 2. RUNTIME METADATA
        // =========================================================================
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{5f, 5f});
        metaTable.setSpacingAfter(6f);

        PdfPCell leftMetaCell = new PdfPCell();
        leftMetaCell.setBorder(Rectangle.NO_BORDER);
        String invNo = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-2026-00033";
        leftMetaCell.addElement(new Paragraph("Invoice No: " + invNo, boldValueFont));

        leftMetaCell.addElement(new Paragraph( " (Dispensary List Attached)", standardFont));
        metaTable.addCell(leftMetaCell);

        PdfPCell rightMetaCell = new PdfPCell();
        rightMetaCell.setBorder(Rectangle.NO_BORDER);

        String dateStr = invoice.getTransactionDate() != null ?
                invoice.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "0000-00-00";
        Paragraph datePara = new Paragraph("Date: " + dateStr, standardFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightMetaCell.addElement(datePara);

        String cashierName = invoice.getCashier() != null ? invoice.getCashier().getUsername() : "cashier";
        Paragraph cashierPara = new Paragraph("Cashier: " + cashierName, standardFont);
        cashierPara.setAlignment(Element.ALIGN_RIGHT);
        rightMetaCell.addElement(cashierPara);

        metaTable.addCell(rightMetaCell);
        document.add(metaTable);

        // =========================================================================
        // 3. FINANCIAL CALCULATION MATRIX
        // =========================================================================
        PdfPTable calcTable = new PdfPTable(2);
        calcTable.setWidthPercentage(100);
        calcTable.setWidths(new float[]{7f, 3f});

        double subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : 00.00;
        double discount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : 0.00;
        double grandTotal = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : 00.00;

        addCalculationRow(calcTable, "Sub Total:", String.format("Rs. %,10.2f", subTotal), standardFont, Element.ALIGN_RIGHT);
        addCalculationRow(calcTable, "Total Discount:", String.format("Rs. %,10.2f", discount), standardFont, Element.ALIGN_RIGHT);

        PdfPCell gtLabel = new PdfPCell(new Phrase("GRAND TOTAL:", labelFont));
        gtLabel.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        gtLabel.setBorderColor(PRIMARY_ACCENT);
        gtLabel.setPadding(4f);


        PdfPCell gtVal = new PdfPCell(new Phrase(String.format("Rs. %,10.2f", grandTotal), boldValueFont));
        gtVal.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        gtVal.setBorderColor(PRIMARY_ACCENT);
        gtVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        gtVal.setPadding(4f);

        calcTable.addCell(gtLabel);
        calcTable.addCell(gtVal);
// --- LEFT SIDE: Amount in Words ---
        LongNumberToStringWord lnts = new LongNumberToStringWord();
// Quick safety check: Handle null totals safely before converting
        Double totalForWords = invoice.getGrandTotal() != null ? invoice.getGrandTotal() : 0L;
        String amountInWordsValue = lnts.convertLongToNeplaiString( totalForWords) + " / Only";

        PdfPCell amountWordsCell = new PdfPCell();
        amountWordsCell.setBorder(Rectangle.NO_BORDER);
// 🎯 FIX 1: Set a Colspan so this text spans across multiple columns on the left
        amountWordsCell.setColspan(2);

        Paragraph pWordsLabel = new Paragraph("Amount in Words: ", labelFont);
        Paragraph pWordsValue = new Paragraph(amountInWordsValue, FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.BLACK));
// Clean line spacing
        pWordsLabel.setSpacingAfter(2f);

        amountWordsCell.addElement(pWordsLabel);
        amountWordsCell.addElement(pWordsValue);
        calcTable.addCell(amountWordsCell);


// --- RIGHT SIDE: Appending Payment Breakdown ---
        String payMode = invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : "CASH";
        double amtReceived = invoice.getAmountReceived() != null ? invoice.getAmountReceived() : 0.00;
        double changeRet = invoice.getChangeReturned() != null ? invoice.getChangeReturned() : 0.00;

        PdfPCell paymentCalcCell = new PdfPCell();
        paymentCalcCell.setBorder(Rectangle.NO_BORDER);
// 🎯 FIX 2: Set a Colspan for the remaining columns on the right
        paymentCalcCell.setColspan(2);
        paymentCalcCell.setHorizontalAlignment(Element.ALIGN_RIGHT); // Align right to match financials

// Use an itemized layout style for crisp receipt spacing
        Paragraph pPayDetails = new Paragraph();
        pPayDetails.setFont(labelFont);
        pPayDetails.setAlignment(Element.ALIGN_RIGHT);
        pPayDetails.add(new Chunk("Payment Mode: " + payMode + "\n"));
        pPayDetails.add(new Chunk("Amount Received: Rs. " + String.format("%.2f", amtReceived) + "\n"));
        pPayDetails.add(new Chunk("Change Returned: Rs. " + String.format("%.2f", changeRet)));

        paymentCalcCell.addElement(pPayDetails);
        calcTable.addCell(paymentCalcCell);
        document.add(calcTable);

        // =========================================================================
        // 4. SETTLEMENT STATUS BOX
        // =========================================================================
        PdfPTable statusTable = new PdfPTable(1);
        statusTable.setWidthPercentage(100);

        PdfPCell containerCell = new PdfPCell();
        containerCell.setBorder(Rectangle.BOX);
        containerCell.setBorderColor(BORDER_COLOR);
        containerCell.setBackgroundColor(HIGHLIGHT_BG);
        containerCell.setPadding(6f);

        String currentStatus = invoice.getInvoiceStatus() != null ? invoice.getInvoiceStatus().toString() : "PAID";
        Paragraph statePara = new Paragraph("STATUS: " + currentStatus + "   |   Thank you! Please keep this receipt with your Dispensary Slip.", statusFont);
        statePara.setAlignment(Element.ALIGN_CENTER);
        containerCell.addElement(statePara);

        statusTable.addCell(containerCell);
        document.add(statusTable);

        document.close();
        return baos.toByteArray();
    }

    private void addCalculationRow(PdfPTable table, String label, String value, Font font, int alignment) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(alignment);
        valueCell.setPadding(2f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHorizontalDivider(Document doc, Color color, float width, float padding) throws DocumentException {
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        lineTable.setSpacingBefore(padding);
        lineTable.setSpacingAfter(padding);
        PdfPCell lineCell = new PdfPCell(new Phrase(""));
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderWidthBottom(width);
        lineCell.setBorderColorBottom(color);
        lineTable.addCell(lineCell);
        doc.add(lineTable);
    }
}
