package ASLENIX.pharmacy.demo.utils;

import ASLENIX.pharmacy.demo.dataTransferObject.DayEndSummaryDTO;


import ASLENIX.pharmacy.demo.dataTransferObject.InvoiceSummery;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.openpdf.text.Document;
import org.openpdf.text.PageSize;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DayClosingReportPDFExport {

    private final Color PRIMARY_ACCENT = new Color(26, 54, 93);    // Deep Navy Blue (#1A365D)
    private final Color SECONDARY_ACCENT = new Color(74, 85, 104);  // Muted Slate (#4A5568)
    private final Color ROW_BG_LIGHT = new Color(247, 250, 252);    // Light Gray (#F7FAFC)
    private final Color ROW_BG_DARK = new Color(255, 255, 255);     // White
    private final Color BORDER_COLOR = new Color(226, 232, 240);    // Light Gray (#E2E8F0)

    private final DayEndSummaryDTO desDOT;

    public DayClosingReportPDFExport(DayEndSummaryDTO desDOT) {
        this.desDOT = desDOT;
    }

    public byte[] export() throws IOException {

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
        Font sectionHeadingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_ACCENT);
        Font standardFont = FontFactory.getFont(FontFactory.HELVETICA, 10, SECONDARY_ACCENT);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        // 2. Header Section
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{5f, 5f});

        // Left Header: Title & Info
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("DAY END SUMMARY", titleFont));


        String cashierIDStr = desDOT.getCahierId() != null ? desDOT.getCahierId().toString() : "(N/A)";
        leftCell.addElement(new Paragraph("Cashier ID: " + cashierIDStr, standardFont));

        String cashierStr = desDOT.getCashierName() != null ? desDOT.getCashierName() : "(N/A)";
        leftCell.addElement(new Paragraph("Cashier Name: " + cashierStr, standardFont));



        // Right Header: Date & Status
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);

        String dateStr = desDOT.getSummaryDate() != null ? desDOT.getSummaryDate().format(DateTimeFormatter.ISO_LOCAL_DATE) :
                "(N/A)";
        Paragraph datePara = new Paragraph("Date: " + dateStr, standardFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(datePara);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Thick Horizontal Line divider
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

        // 3. Summary Totals Section
        document.add(new Paragraph("SUMMARY TOTALS", sectionHeadingFont));
        document.add(new Paragraph(" "));

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{0.60f, 0.40f});

        // Calculations grid (Left side column setup to align cleanly)
        PdfPTable calcTable = new PdfPTable(2);
        calcTable.setWidthPercentage(100);
        calcTable.setWidths(new float[]{0.5f, 0.5f});

        addTotalRow(calcTable, "Gross Sales:", "Rs. " + String.format("%.2f", desDOT.getGrossSales()), standardFont, Element.ALIGN_LEFT);
        addTotalRow(calcTable, "Discounts Given:", "Rs. " + String.format("%.2f", desDOT.getDiscountsGiven()), standardFont, Element.ALIGN_LEFT);

        // Highlighted Net Sales row
        Font netSalesFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
        PdfPCell nsLabel = createCell("Net Sales:", netSalesFont, ROW_BG_LIGHT, Element.ALIGN_LEFT);
        nsLabel.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        nsLabel.setBorderColor(PRIMARY_ACCENT);
        PdfPCell nsVal = createCell("Rs. " + String.format("%.2f", desDOT.getNetSales()), netSalesFont, ROW_BG_LIGHT, Element.ALIGN_RIGHT);
        nsVal.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        nsVal.setBorderColor(PRIMARY_ACCENT);
        calcTable.addCell(nsLabel);
        calcTable.addCell(nsVal);

        PdfPCell calcWrapper = new PdfPCell(calcTable);
        calcWrapper.setBorder(Rectangle.NO_BORDER);
        summaryTable.addCell(calcWrapper);

        // MOP Breakdown display block (Right side column)
        PdfPCell mopCell = new PdfPCell();
        mopCell.setBorder(Rectangle.NO_BORDER);
        mopCell.setPaddingLeft(20f);
        mopCell.addElement(new Paragraph("MOP Breakdown:", labelFont));

// Handle concrete fields with null safety fallbacks
        double cash = desDOT.getCashTotal() != null ? desDOT.getCashTotal() : 0.0;
        double qr = desDOT.getQrTotal() != null ? desDOT.getQrTotal() : 0.0;
        double card = desDOT.getCardTotal() != null ? desDOT.getCardTotal() : 0.0;

        mopCell.addElement(new Paragraph("- CASH: Rs. " + String.format("%.2f", cash), standardFont));
        mopCell.addElement(new Paragraph("- FONEPAY/QR: Rs. " + String.format("%.2f", qr), standardFont));
        mopCell.addElement(new Paragraph("- CARD: Rs. " + String.format("%.2f", card), standardFont));

        summaryTable.addCell(mopCell);
        document.add(summaryTable);


        // 4. Detailed Transaction Log Section
        document.add(new Paragraph("DETAILED TRANSACTION LOG", sectionHeadingFont));
        document.add(new Paragraph(" "));

        Map<String, Float> columnConfig = new LinkedHashMap<>();
        columnConfig.put("INV NO.", 0.3f);
        columnConfig.put("MOP", 0.3f);
        columnConfig.put("TOTAL ( Rs. )", 0.4f);

        PdfPTable itemTable = new PdfPTable(columnConfig.size());
        itemTable.setWidthPercentage(100);

        float[] widths = new float[columnConfig.size()];
        int index = 0;
        for (float width : columnConfig.values()) {
            widths[index++] = width;
        }
        itemTable.setWidths(widths);

        // Table Header Layout setup
        Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String h : columnConfig.keySet()) {
            PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
            cell.setBackgroundColor(PRIMARY_ACCENT);
            cell.setHorizontalAlignment(h.equals("TOTAL") ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6f);
            cell.setBorderColor(BORDER_COLOR);
            itemTable.addCell(cell);
        }

        // Table Body Processing Loop
        Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        boolean alternate = false;

        if (desDOT.getInvoiceSummeryList() != null) {
            for (InvoiceSummery log : desDOT.getInvoiceSummeryList()) {
                Color bgColor = alternate ? ROW_BG_LIGHT : ROW_BG_DARK;


                itemTable.addCell(createCell(log.getInvoiceNumber(), tdFont, bgColor, Element.ALIGN_CENTER));
                itemTable.addCell(createCell(log.getPaymentMethod().getValue(), tdFont, bgColor, Element.ALIGN_CENTER));
                itemTable.addCell(createCell(String.format("%.2f", log.getTotalAmount()), tdFont, bgColor, Element.ALIGN_RIGHT));

                alternate = !alternate;
            }
        }
        document.add(itemTable);
        document.add(new Paragraph(" "));

        // 5. Lower Meta Summary Metadata Footer Row
        PdfPTable metaTable = getPdfPTable(labelFont);
        document.add(metaTable);


        // Add spacing before the signature block
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" ")); // Double spacing for breathing room

// 6. Signature Block Row (Prepared By / Verified By)
        PdfPTable footerSignTable = getPTable(labelFont);

// Add the signature table to the document
        document.add(footerSignTable);

        document.close();
        return baos.toByteArray();
    }

    private static PdfPTable getPTable(Font labelFont) {
        PdfPTable footerSignTable = new PdfPTable(2);
        footerSignTable.setWidthPercentage(100);

// Prepared By (Left Aligned)
        PdfPCell cellPrepared = new PdfPCell(new Paragraph("Prepared By: ___________________", labelFont));
        cellPrepared.setBorder(PdfPCell.NO_BORDER);
        cellPrepared.setHorizontalAlignment(Element.ALIGN_LEFT);
        footerSignTable.addCell(cellPrepared);

// Verified By (Right Aligned)
        PdfPCell cellVerified = new PdfPCell(new Paragraph("Verified By: ___________________", labelFont));
        cellVerified.setBorder(PdfPCell.NO_BORDER);
        cellVerified.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footerSignTable.addCell(cellVerified);
        return footerSignTable;
    }

    private PdfPTable getPdfPTable(Font labelFont) {
        PdfPTable metaTable = new PdfPTable(1);
        metaTable.setWidthPercentage(100);

        String summaryMetaText = String.format("Total Invoices: %d",
                desDOT.getTotalInvoices());

        PdfPCell metaCell = new PdfPCell(new Phrase(summaryMetaText, labelFont));
        metaCell.setBackgroundColor(ROW_BG_LIGHT);
        metaCell.setBorder(Rectangle.BOX);
        metaCell.setBorderColor(BORDER_COLOR);
        metaCell.setPadding(8f);
        metaCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        metaTable.addCell(metaCell);
        return metaTable;
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
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell2.setPadding(4f);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    // Inner class tracking page numbers using OpenPDF engine hook elements
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