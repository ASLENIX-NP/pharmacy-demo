package ASLENIX.pharmacy.demo.utils;

import ASLENIX.pharmacy.demo.model.Invoice;
import ASLENIX.pharmacy.demo.model.InvoiceItem;
import jakarta.servlet.http.HttpServletResponse;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;


public class InvoicePDFExporter {

    private final Color PRIMARY_ACCENT = new Color(26, 54, 93); // Deep Navy Blue (#1A365D)
    private final Color SECONDARY_ACCENT = new Color(74, 85, 104); // Muted Slate (#4A5568)
    private final Color ROW_BG_LIGHT = new Color(247, 250, 252); // Light Gray (#F7FAFC)
    private final Color ROW_BG_DARK = new Color(255, 255, 255); // White
    private final Color BORDER_COLOR = new Color(226, 232, 240); // Light Gray (#E2E8F0)

    private final  Invoice invoice;

    public InvoicePDFExporter(Invoice invoice) {
        this.invoice = invoice;
    }

    public byte[] export( ) throws IOException {
        
        // 1. Document Setup & Geometry
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        // Setup Footer & Page Numbering
        PageNumberFooter footer = new PageNumberFooter(SECONDARY_ACCENT);
        writer.setPageEvent(footer);

        document.open();

        // Define standard fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_ACCENT);
        Font standardFont = FontFactory.getFont(FontFactory.HELVETICA, 10, SECONDARY_ACCENT);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        // 2. Header Section
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{6f, 4f});

        // Pharmacy Info (Left)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        
        try {
            // Attempt to load SVG. Note: OpenPDF natively may not support SVG, so it is wrapped in try-catch
            Image logo = Image.getInstance("src/main/resources/static/images/AB+.jpg");
            logo.scaleToFit(30, 30);
            leftCell.addElement(logo);
        } catch (Exception e) {
            // Fallback gracefully if image format is unsupported
        }
        
        leftCell.addElement(new Paragraph("ABIS PLUS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PRIMARY_ACCENT)));
        leftCell.addElement(new Paragraph("123 Health Ave, Medical District", FontFactory.getFont(FontFactory.HELVETICA, 9, SECONDARY_ACCENT)));
        leftCell.addElement(new Paragraph("Phone: +977 1234567890", FontFactory.getFont(FontFactory.HELVETICA, 9, SECONDARY_ACCENT)));

        headerTable.addCell(leftCell);

        // Invoice Info (Right)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        Paragraph title = new Paragraph("DISPENSARY SLIP", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(title);

        String invNo = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "(Not generated yet)";
        Paragraph invNoPara = new Paragraph("Invoice No: " + invNo, standardFont);
        invNoPara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(invNoPara);

        String dateStr = invoice.getTransactionDate() != null ? invoice.getTransactionDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "(Not specified)";
        Paragraph datePara = new Paragraph("Date: " + dateStr, standardFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(datePara);

        String pharmacistName = invoice.getPharmacist() != null ?
                invoice.getPharmacist().getFirstName() + " " + invoice.getPharmacist().getLastName() : "(Not coded yet)";
        Paragraph billedByPara = new Paragraph("Drafted By: " + pharmacistName, standardFont);
        billedByPara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(billedByPara);

        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Thick Horizontal Line
        document.add(new Paragraph(" "));
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(""));
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setBorderColorBottom(PRIMARY_ACCENT);
        lineTable.addCell(lineCell);
        document.add(lineTable);
        document.add(new Paragraph(" "));

        // 3. Client / Patient Info Section
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setWidths(new float[]{5f, 5f});

        String customerName = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Walk-in Customer";
        String customerPhone = invoice.getCustomer() != null && invoice.getCustomer().getPhone() != null ? invoice.getCustomer().getPhone() : "(N/A)";
        String customerPan = invoice.getCustomer() != null && invoice.getCustomer().getPanNumber() != null ? invoice.getCustomer().getPanNumber() : "(N/A)";

        String customerEmail = invoice.getCustomer() != null && invoice.getCustomer().getEmail() != null ?invoice.getCustomer().getEmail() : "(N/A)";


        PdfPCell c1 = new PdfPCell(); c1.setBorder(Rectangle.NO_BORDER);
        c1.addElement(new Paragraph("To:", labelFont));
        c1.addElement(new Paragraph(customerName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK)));

        PdfPCell c2 = new PdfPCell(); c2.setBorder(Rectangle.NO_BORDER);
        c2.addElement(new Paragraph("Contact Info:", labelFont));
        c2.addElement(new Paragraph("Phone: " + customerPhone, standardFont));
        c2.addElement(new Paragraph("Email: " + customerEmail, standardFont));
        c2.addElement(new Paragraph("PAN/VAT: " + customerPan, standardFont));

        clientTable.addCell(c1);
        clientTable.addCell(c2);
        document.add(clientTable);
        document.add(new Paragraph(" "));

        // 4. Main Items Table

        Map<String, Float> columnConfig = new LinkedHashMap<>();
        columnConfig.put("S.No", 0.08f);
        columnConfig.put("Description / Batch", 0.35f); // Adjusted to make total = 1.0
        columnConfig.put("Qty", 0.10f);
        columnConfig.put("Rate (Rs.)", 0.13f);
        columnConfig.put("Disc %", 0.10f);              // Adjusted to make total = 1.0
        columnConfig.put("VAT %", 0.11f);               // Adjusted to make total = 1.0
        columnConfig.put("Amount (Rs.)", 0.13f);


        PdfPTable itemTable = new PdfPTable(columnConfig.size());
        itemTable.setWidthPercentage(100);

        //Extract widths from the map and apply them
        float[] widths = new float[columnConfig.size()];
        int index = 0;
        for (float width : columnConfig.values()) {
            widths[index++] = width;
        }
        itemTable.setWidths(widths);

        // Table Header
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String h : columnConfig.keySet()) {
            PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
            cell.setBackgroundColor(PRIMARY_ACCENT);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6f);
            cell.setBorderColor(BORDER_COLOR);
            itemTable.addCell(cell);
        }

        // Table Body
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font tdSubFont = FontFactory.getFont(FontFactory.HELVETICA, 8, SECONDARY_ACCENT);

        int sNo = 1;
        boolean alternate = false;

        if (invoice.getInvoiceItemList() != null) {
            for (InvoiceItem item : invoice.getInvoiceItemList()) {
                Color bgColor = alternate ? ROW_BG_LIGHT : ROW_BG_DARK;

                // S.No
                PdfPCell snoCell = createCell(String.valueOf(sNo++), tdFont, bgColor, Element.ALIGN_CENTER);
                itemTable.addCell(snoCell);

                // Description & Batch
                String prodName = item.getProduct() != null ? item.getProduct().getName() : "(Not coded yet)";
                String batchNo = item.getBatch() != null ? item.getBatch().getBatchNumber() : "(No batch)";

                PdfPCell descCell = new PdfPCell();
                descCell.setBackgroundColor(bgColor);
                descCell.setBorderColor(BORDER_COLOR);
                descCell.setPadding(6f);
                descCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Paragraph p1 = new Paragraph(prodName, tdFont);
                Paragraph p2 = new Paragraph("Batch: " + batchNo, tdSubFont);
                descCell.addElement(p1);
                descCell.addElement(p2);
                itemTable.addCell(descCell);

                // Qty
                itemTable.addCell(createCell(String.valueOf(item.getQuantity()), tdFont, bgColor, Element.ALIGN_CENTER));

                // Rate
                itemTable.addCell(createCell(String.format("%.2f", item.getUnitPrice()), tdFont, bgColor, Element.ALIGN_RIGHT));

                // Disc %
                itemTable.addCell(createCell(String.format("%.2f%%", item.getDiscountPercentage()), tdFont, bgColor, Element.ALIGN_RIGHT));

                // Disc %
                itemTable.addCell(createCell(String.format("%.2f%%", item.getVatPercentage()), tdFont, bgColor,Element.ALIGN_RIGHT));


                // Amount
                itemTable.addCell(createCell(String.format("%.2f", item.getLineTotal()), tdFont, bgColor, Element.ALIGN_RIGHT));

                alternate = !alternate;
            }
        }
        document.add(itemTable);
        document.add(new Paragraph(" "));

        // 5. Totals & Financial Breakdown
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[]{0.60f, 0.40f});

        // Left Empty / Amount in Words
        LongNumberToStringWord lnts = new LongNumberToStringWord();
        String amountInWordsValue = lnts.convertLongToNeplaiString(invoice.getGrandTotal()) + "/Only";
        PdfPCell amountWordsCell = new PdfPCell();
        amountWordsCell.setBorder(Rectangle.NO_BORDER);
        amountWordsCell.addElement(new Paragraph("Amount in Words: ", labelFont));
        amountWordsCell.addElement(new Paragraph(amountInWordsValue, FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10,
                Color.BLACK)));
        totalsTable.addCell(amountWordsCell);

        // Calculations Table
        PdfPTable calcTable = new PdfPTable(2);
        calcTable.setWidthPercentage(100);
        calcTable.setWidths(new float[]{0.6f, 0.4f});

        addTotalRow(calcTable, "Sub Total:", String.format("%.2f", invoice.getSubTotal() != null ? invoice.getSubTotal() : 0.0), standardFont, Element.ALIGN_RIGHT);
        addTotalRow(calcTable, "Discount:", String.format("%.2f", invoice.getDiscountAmount()), standardFont, Element.ALIGN_RIGHT);
        addTotalRow(calcTable, "VAT:", String.format("%.2f", invoice.getVatAmount()), standardFont, Element.ALIGN_RIGHT);

        // Grand Total row
        Font gTotalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        PdfPCell gtLabel = createCell("Grand Total:", gTotalFont, ROW_BG_LIGHT, Element.ALIGN_RIGHT);
        gtLabel.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        gtLabel.setBorderColor(PRIMARY_ACCENT);
        gtLabel.setBorderWidth(1.5f);
        PdfPCell gtVal = createCell(String.format("%.2f", invoice.getGrandTotal()), gTotalFont, ROW_BG_LIGHT, Element.ALIGN_RIGHT);
        gtVal.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        gtVal.setBorderColor(PRIMARY_ACCENT);
        gtVal.setBorderWidth(1.5f);
        calcTable.addCell(gtLabel);
        calcTable.addCell(gtVal);

        PdfPCell calcWrapper = new PdfPCell(calcTable);
        calcWrapper.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(calcWrapper);

        document.add(totalsTable);

        // 6. Footer & Legal Disclaimer
        document.add(new Paragraph("\n\n\n"));
        PdfPTable signTable = new PdfPTable(1);
        signTable.setWidthPercentage(100);
        
        PdfPCell disclaimerCell = new PdfPCell();
        disclaimerCell.setBorder(Rectangle.BOX);
        disclaimerCell.setBorderWidth(1f);

        Paragraph warningPara = new Paragraph("UNPROCESSED SLIP - NOT A BILL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.RED));
        warningPara.setAlignment(Element.ALIGN_CENTER);
        disclaimerCell.addElement(warningPara);

        Paragraph instructionPara1 = new Paragraph("This slip contains your detailed prescription dispense list. For financial records and tax compliance, please refer to your attached official Tax Invoice.", FontFactory.getFont(FontFactory.HELVETICA, 10, SECONDARY_ACCENT));
        instructionPara1.setAlignment(Element.ALIGN_JUSTIFIED);
        disclaimerCell.addElement(instructionPara1);
        
        Paragraph instructionPara2 = new Paragraph("Please present to cashier for payment.",
                FontFactory.getFont(FontFactory.HELVETICA, 10, SECONDARY_ACCENT));
        instructionPara2.setAlignment(Element.ALIGN_CENTER);
        disclaimerCell.addElement(instructionPara2);
        
        signTable.addCell(disclaimerCell);
        
        document.add(signTable);

        document.close();

        return baos.toByteArray();
    }

    private PdfPCell createCell(String text, Font font, Color bgColor, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font, int align) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, font));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setHorizontalAlignment(align);
        cell1.setPadding(4f);
        PdfPCell cell2 = new PdfPCell(new Phrase(value, font));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setHorizontalAlignment(align);
        cell2.setPadding(4f);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    // Inner class for page numbering
    static class PageNumberFooter extends PdfPageEventHelper {
        private final Font footerFont;

        public PageNumberFooter(Color color) {
            this.footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, color);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(String.format("Page %d", writer.getPageNumber()), footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}
