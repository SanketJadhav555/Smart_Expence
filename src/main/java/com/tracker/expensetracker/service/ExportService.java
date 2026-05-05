package com.tracker.expensetracker.service;

// ── PDF (OpenPDF / lowagie) ──────────────────────────────────────────────────
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

// ── Apache POI (Excel) – all explicit, no wildcards ─────────────────────────
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;       // POI Row
import org.apache.poi.ss.usermodel.Cell;      // POI Cell
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ── App ──────────────────────────────────────────────────────────────────────
import com.tracker.expensetracker.dto.TransactionDTO;
import com.tracker.expensetracker.model.TransactionType;
import com.tracker.expensetracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    // ══════════════════════════════════════════════════════════════
    //  EXCEL  (.xlsx)
    // ══════════════════════════════════════════════════════════════
    public byte[] exportToExcel(int month, int year) throws Exception {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = YearMonth.of(year, month).atEndOfMonth();

        List<TransactionDTO> transactions = transactionRepository
                .findByTransactionDateBetween(start, end)
                .stream()
                .map(t -> transactionService.toPublicDTO(t))
                .collect(Collectors.toList());

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Cell styles ──────────────────────────────────
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(new XSSFColor(new byte[]{(byte)255,(byte)255,(byte)255}, null));
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)79,(byte)70,(byte)229}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle titleStyle = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle incomeStyle = wb.createCellStyle();
            incomeStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)220,(byte)252,(byte)231}, null));
            incomeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCellStyle expenseStyle = wb.createCellStyle();
            expenseStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)254,(byte)226,(byte)226}, null));
            expenseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFDataFormat fmt = wb.createDataFormat();
            short currencyFmt = fmt.getFormat("#,##0.00");

            XSSFCellStyle currencyStyle = wb.createCellStyle();
            currencyStyle.setDataFormat(currencyFmt);

            XSSFCellStyle incomeCurrencyStyle = wb.createCellStyle();
            incomeCurrencyStyle.cloneStyleFrom(incomeStyle);
            incomeCurrencyStyle.setDataFormat(currencyFmt);

            XSSFCellStyle expenseCurrencyStyle = wb.createCellStyle();
            expenseCurrencyStyle.cloneStyleFrom(expenseStyle);
            expenseCurrencyStyle.setDataFormat(currencyFmt);

            XSSFCellStyle summaryLabelStyle = wb.createCellStyle();
            XSSFFont summaryFont = wb.createFont();
            summaryFont.setBold(true);
            summaryLabelStyle.setFont(summaryFont);

            // ── Sheet ────────────────────────────────────────
            XSSFSheet sheet = wb.createSheet("Transactions");
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 7000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 6000);
            sheet.setColumnWidth(5, 5000);

            int rowNum = 0;

            // Title row
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeight((short) 500);
            Cell titleCell = titleRow.createCell(0);
            String monthName = start.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            titleCell.setCellValue("Smart Expense Tracker — " + monthName + " " + year);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            rowNum++; // blank

            // Header row
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeight((short) 400);
            String[] headers = {"Date", "Title", "Type", "Category", "Amount", "Payment Method"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Data rows
            BigDecimal totalIncome  = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

            for (TransactionDTO tx : transactions) {
                Row row = sheet.createRow(rowNum++);
                boolean isIncome = tx.getType() == TransactionType.INCOME;

                XSSFCellStyle rowStyle = isIncome ? incomeStyle        : expenseStyle;
                XSSFCellStyle amtStyle = isIncome ? incomeCurrencyStyle : expenseCurrencyStyle;

                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(tx.getTransactionDate() != null
                        ? tx.getTransactionDate().format(dtf) : "");
                dateCell.setCellStyle(rowStyle);

                Cell titleCell2 = row.createCell(1);
                titleCell2.setCellValue(tx.getTitle() != null ? tx.getTitle() : "");
                titleCell2.setCellStyle(rowStyle);

                Cell typeCell = row.createCell(2);
                typeCell.setCellValue(tx.getType() != null ? tx.getType().name() : "");
                typeCell.setCellStyle(rowStyle);

                Cell catCell = row.createCell(3);
                catCell.setCellValue(tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized");
                catCell.setCellStyle(rowStyle);

                Cell amtCell = row.createCell(4);
                amtCell.setCellValue(tx.getAmount() != null ? tx.getAmount().doubleValue() : 0);
                amtCell.setCellStyle(amtStyle);

                Cell pmCell = row.createCell(5);
                pmCell.setCellValue(tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "");
                pmCell.setCellStyle(rowStyle);

                if (isIncome)
                    totalIncome  = totalIncome.add(tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO);
                else
                    totalExpense = totalExpense.add(tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO);
            }

            rowNum++; // blank before summary

            // Summary header
            Row summaryHeader = sheet.createRow(rowNum++);
            Cell sh = summaryHeader.createCell(3);
            sh.setCellValue("SUMMARY");
            sh.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 3, 5));

            BigDecimal netSavings = totalIncome.subtract(totalExpense);
            String[][] summaryRows = {
                    {"Total Income",  totalIncome.toPlainString()},
                    {"Total Expense", totalExpense.toPlainString()},
                    {"Net Savings",   netSavings.toPlainString()}
            };
            for (String[] s : summaryRows) {
                Row sr = sheet.createRow(rowNum++);
                Cell lbl = sr.createCell(3);
                lbl.setCellValue(s[0]);
                lbl.setCellStyle(summaryLabelStyle);
                Cell val = sr.createCell(4);
                val.setCellValue(Double.parseDouble(s[1]));
                val.setCellStyle(currencyStyle);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PDF
    // ══════════════════════════════════════════════════════════════
    public byte[] exportToPdf(int month, int year) throws Exception {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = YearMonth.of(year, month).atEndOfMonth();

        List<TransactionDTO> transactions = transactionRepository
                .findByTransactionDateBetween(start, end)
                .stream()
                .map(t -> transactionService.toPublicDTO(t))
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Colors
        Color primaryColor  = new Color(79,  70,  229);
        Color incomeColor   = new Color(34,  197, 94);
        Color expenseColor  = new Color(239, 68,  68);
        Color lightGray     = new Color(241, 245, 249);
        Color headerBg      = new Color(30,  41,  59);
        Color mutedColor    = new Color(100, 116, 139);

        // Fonts
        Font titleFont   = new Font(Font.HELVETICA, 18, Font.BOLD,   primaryColor);
        Font subFont     = new Font(Font.HELVETICA, 10, Font.NORMAL, mutedColor);
        Font headerFont  = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.WHITE);
        Font normalFont  = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(30, 41, 59));
        Font boldFont    = new Font(Font.HELVETICA,  9, Font.BOLD,   new Color(30, 41, 59));

        // Title
        String monthName = start.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        Paragraph title = new Paragraph("Smart Personal Expense Tracker", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph("Monthly Report — " + monthName + " " + year, subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(6);
        doc.add(subtitle);

        Paragraph dateLine = new Paragraph(
                "Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                subFont);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingAfter(18);
        doc.add(dateLine);

        // Compute totals
        BigDecimal totalIncome  = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (TransactionDTO tx : transactions) {
            if (tx.getAmount() == null) continue;
            if (tx.getType() == TransactionType.INCOME) totalIncome  = totalIncome.add(tx.getAmount());
            else                                        totalExpense = totalExpense.add(tx.getAmount());
        }
        BigDecimal net = totalIncome.subtract(totalExpense);

        // Summary box
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(16);
        addSummaryCell(summaryTable, "Total Income",  "Rs. " + fmt(totalIncome),  incomeColor);
        addSummaryCell(summaryTable, "Total Expense", "Rs. " + fmt(totalExpense), expenseColor);
        addSummaryCell(summaryTable, "Net Savings",   "Rs. " + fmt(net),
                net.compareTo(BigDecimal.ZERO) >= 0 ? incomeColor : expenseColor);
        doc.add(summaryTable);

        // Transactions table
        PdfPTable table = new PdfPTable(new float[]{2.2f, 3f, 1.5f, 2f, 2f, 2f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        String[] cols = {"Date", "Title", "Type", "Category", "Amount", "Payment"};
        for (String col : cols) {
            PdfPCell hc = new PdfPCell(new Phrase(col, headerFont));
            hc.setBackgroundColor(headerBg);
            hc.setPadding(7);
            hc.setBorderColor(headerBg);
            table.addCell(hc);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        boolean alt = false;
        for (TransactionDTO tx : transactions) {
            boolean isIncome = tx.getType() == TransactionType.INCOME;
            Color rowBg = alt ? lightGray : Color.WHITE;
            alt = !alt;

            addCell(table, tx.getTransactionDate() != null ? tx.getTransactionDate().format(dtf) : "", normalFont, rowBg);
            addCell(table, tx.getTitle() != null ? tx.getTitle() : "", boldFont, rowBg);

            Font typeFont = new Font(Font.HELVETICA, 8, Font.BOLD, isIncome ? incomeColor : expenseColor);
            PdfPCell typeCell = new PdfPCell(new Phrase(tx.getType() != null ? tx.getType().name() : "", typeFont));
            typeCell.setBackgroundColor(rowBg);
            typeCell.setPadding(6);
            typeCell.setBorderColor(lightGray);
            table.addCell(typeCell);

            addCell(table, tx.getCategoryName() != null ? tx.getCategoryName() : "—", normalFont, rowBg);

            Font amtFont = new Font(Font.HELVETICA, 9, Font.BOLD, isIncome ? incomeColor : expenseColor);
            PdfPCell amtCell = new PdfPCell(new Phrase("Rs. " + fmt(tx.getAmount()), amtFont));
            amtCell.setBackgroundColor(rowBg);
            amtCell.setPadding(6);
            amtCell.setBorderColor(lightGray);
            amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(amtCell);

            addCell(table, tx.getPaymentMethod() != null ? tx.getPaymentMethod() : "—", normalFont, rowBg);
        }

        if (transactions.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No transactions found for this period.", normalFont));
            empty.setColspan(6);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(16);
            table.addCell(empty);
        }

        doc.add(table);

        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, mutedColor);
        Paragraph footer = new Paragraph(
                "\nTotal: " + transactions.size() + " transaction(s)   |   Smart Personal Expense Tracker",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(12);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void addSummaryCell(PdfPTable t, String label, String value, Color color) {
        Font labelFont = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(100, 116, 139));
        Font valueFont = new Font(Font.HELVETICA, 13, Font.BOLD,   color);
        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(new Color(226, 232, 240));
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", labelFont));
        p.add(new Chunk(value, valueFont));
        cell.addElement(p);
        t.addCell(cell);
    }

    private void addCell(PdfPTable t, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(new Color(241, 245, 249));
        t.addCell(cell);
    }

    private String fmt(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
