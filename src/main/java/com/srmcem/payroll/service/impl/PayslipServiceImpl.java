package com.srmcem.payroll.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.PayrollRecord;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.PayrollRecordRepository;
import com.srmcem.payroll.service.PayslipService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Generates a professional PDF payslip using the OpenPDF library.
 *
 * <p>Layout:
 * <ol>
 *   <li>Header band — company name and "PAYSLIP" title</li>
 *   <li>Employee info table — name, ID, department, designation, period</li>
 *   <li>Earnings table — basic salary, bonus, overtime → gross total</li>
 *   <li>Deductions table — deductions</li>
 *   <li>Net-pay highlight box</li>
 *   <li>Footer — generated timestamp and confidentiality note</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private final PayrollRecordRepository payrollRecordRepository;
    private final com.srmcem.payroll.service.CompanySettingsService settingsService;

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color HEADER_BG      = new Color(31, 57, 98);   // deep navy
    private static final Color ACCENT_BG      = new Color(52, 152, 219); // sky blue
    private static final Color SECTION_BG     = new Color(236, 240, 245); // light grey-blue
    private static final Color NET_PAY_BG     = new Color(39, 174, 96);  // green
    private static final Color TABLE_BORDER   = new Color(189, 195, 199);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_MUTED     = new Color(127, 140, 141);
    private static final Color WHITE          = Color.WHITE;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_COMPANY   = new Font(Font.HELVETICA, 22, Font.BOLD,   WHITE);
    private static final Font FONT_TITLE     = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(179, 205, 224));
    private static final Font FONT_SECTION   = new Font(Font.HELVETICA, 9,  Font.BOLD,   HEADER_BG);
    private static final Font FONT_LABEL     = new Font(Font.HELVETICA, 9,  Font.NORMAL, TEXT_MUTED);
    private static final Font FONT_VALUE     = new Font(Font.HELVETICA, 9,  Font.BOLD,   TEXT_DARK);
    private static final Font FONT_TABLE_HDR = new Font(Font.HELVETICA, 9,  Font.BOLD,   WHITE);
    private static final Font FONT_TABLE_ROW = new Font(Font.HELVETICA, 9,  Font.NORMAL, TEXT_DARK);
    private static final Font FONT_TOTAL     = new Font(Font.HELVETICA, 9,  Font.BOLD,   TEXT_DARK);
    private static final Font FONT_NET_LABEL = new Font(Font.HELVETICA, 12, Font.BOLD,   WHITE);
    private static final Font FONT_NET_AMT   = new Font(Font.HELVETICA, 16, Font.BOLD,   WHITE);
    private static final Font FONT_FOOTER    = new Font(Font.HELVETICA, 7,  Font.ITALIC, TEXT_MUTED);

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePayslip(Long payrollId) {
        PayrollRecord pr = payrollRecordRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRecord", "payrollId", payrollId));

        log.info("Generating payslip PDF for payrollId={}", payrollId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);

            doc.open();
            buildContent(doc, pr);
            doc.close();

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate payslip PDF for payrollId={}", payrollId, e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ── Document builder ─────────────────────────────────────────────────────

    private void buildContent(Document doc, PayrollRecord pr) throws DocumentException {
        Employee   emp         = pr.getEmployee();
        String     fullName    = emp.getFirstName() + " " + emp.getLastName();
        String     department  = emp.getDepartment().getDepartmentName();
        String     designation = emp.getDesignation().getDesignationName();
        YearMonth  period      = YearMonth.parse(pr.getPayrollMonth());
        String     displayMonth = DateUtil.format(period);

        // Fetch dynamic company settings
        String companyName = settingsService.getSettings().getCompanyName();
        if (companyName == null || companyName.isEmpty()) {
            companyName = "SRMCEM PAYROLL";
        } else {
            companyName = companyName.toUpperCase() + " PAYROLL";
        }

        // ── 1. Header ────────────────────────────────────────────────────────
        doc.add(buildHeader(companyName, displayMonth));
        doc.add(Chunk.NEWLINE);

        // ── 2. Employee info ─────────────────────────────────────────────────
        doc.add(buildSectionTitle("EMPLOYEE INFORMATION"));
        doc.add(buildEmployeeInfoTable(emp, fullName, department, designation, displayMonth));
        doc.add(Chunk.NEWLINE);

        // ── 3. Earnings ──────────────────────────────────────────────────────
        doc.add(buildSectionTitle("EARNINGS"));
        doc.add(buildEarningsTable(pr));
        doc.add(Chunk.NEWLINE);

        // ── 4. Deductions ────────────────────────────────────────────────────
        doc.add(buildSectionTitle("DEDUCTIONS"));
        doc.add(buildDeductionsTable(pr));
        doc.add(Chunk.NEWLINE);

        // ── 5. Net pay ───────────────────────────────────────────────────────
        doc.add(buildNetPayTable(pr.getNetSalary()));
        doc.add(Chunk.NEWLINE);

        // ── 6. Footer ────────────────────────────────────────────────────────
        doc.add(buildFooter());
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private PdfPTable buildHeader(String companyNameText, String displayMonth) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 1f});

        // Left cell — company name + subtitle
        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(HEADER_BG);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPadding(16);

        Paragraph companyName = new Paragraph(companyNameText, FONT_COMPANY);
        companyName.setSpacingAfter(4);
        left.addElement(companyName);
        left.addElement(new Paragraph("Human Resources Department", FONT_TITLE));

        // Right cell — payslip label + period
        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(ACCENT_BG);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPadding(16);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font payslipFont = new Font(Font.HELVETICA, 18, Font.BOLD, WHITE);
        Font periodFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(214, 234, 248));

        Paragraph payslipLabel = new Paragraph("PAYSLIP", payslipFont);
        payslipLabel.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(payslipLabel);

        Paragraph periodLabel = new Paragraph("Period: " + displayMonth, periodFont);
        periodLabel.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(periodLabel);

        table.addCell(left);
        table.addCell(right);
        return table;
    }

    // ── Section title strip ──────────────────────────────────────────────────

    private PdfPTable buildSectionTitle(String title) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_SECTION));
        cell.setBackgroundColor(SECTION_BG);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(SECTION_BG);          // hide all borders by matching background
        cell.setBorderWidthLeft(3f);
        cell.setBorderColorLeft(ACCENT_BG);       // show only the left accent bar
        cell.setPadding(6);
        cell.setPaddingLeft(10);

        table.addCell(cell);
        return table;
    }

    // ── Employee info table ──────────────────────────────────────────────────

    private PdfPTable buildEmployeeInfoTable(
            Employee emp, String fullName, String department,
            String designation, String displayMonth) throws DocumentException {

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setWidths(new float[]{1.2f, 2f, 1.2f, 2f});

        addInfoRow(table, "Employee Name",  fullName,
                           "Employee ID",    "EMP-" + String.format("%04d", emp.getEmployeeId()));
        addInfoRow(table, "Department",     department,
                           "Designation",   designation);
        addInfoRow(table, "Email",          emp.getEmail(),
                           "Phone",         emp.getPhone());
        addInfoRow(table, "Joining Date",   emp.getJoiningDate().toString(),
                           "Pay Period",    displayMonth);

        return table;
    }

    private void addInfoRow(PdfPTable table,
                            String lbl1, String val1,
                            String lbl2, String val2) {
        table.addCell(labelCell(lbl1));
        table.addCell(valueCell(val1));
        table.addCell(labelCell(lbl2));
        table.addCell(valueCell(val2));
    }

    // ── Earnings table ───────────────────────────────────────────────────────

    private PdfPTable buildEarningsTable(PayrollRecord pr) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setWidths(new float[]{3f, 2f});

        addTableHeader(table, "Earnings Component", "Amount (₹)");
        addMoneyRow(table, "Basic Salary",  pr.getBasicSalary(), false);
        addMoneyRow(table, "Bonus",         pr.getBonus(),        false);
        addMoneyRow(table, "Overtime Pay",  pr.getOvertime(),     false);
        addMoneyTotalRow(table, "Gross Salary", pr.getGrossSalary());

        return table;
    }

    // ── Deductions table ─────────────────────────────────────────────────────

    private PdfPTable buildDeductionsTable(PayrollRecord pr) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setWidths(new float[]{3f, 2f});

        addTableHeader(table, "Deduction Component", "Amount (₹)");
        addMoneyRow(table, "Total Deductions", pr.getDeductions(), false);
        addMoneyTotalRow(table, "Total Deducted", pr.getDeductions());

        return table;
    }

    // ── Net-pay highlight ────────────────────────────────────────────────────

    private PdfPTable buildNetPayTable(BigDecimal netSalary) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f});

        PdfPCell labelCell = new PdfPCell(new Phrase("NET PAY (Take-Home Salary)", FONT_NET_LABEL));
        labelCell.setBackgroundColor(NET_PAY_BG);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(12);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell amtCell = new PdfPCell(new Phrase("₹ " + formatAmount(netSalary), FONT_NET_AMT));
        amtCell.setBackgroundColor(NET_PAY_BG);
        amtCell.setBorder(Rectangle.NO_BORDER);
        amtCell.setPadding(12);
        amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amtCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(amtCell);
        return table;
    }

    // ── Footer ───────────────────────────────────────────────────────────────

    private Paragraph buildFooter() {
        String generatedAt = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
        Paragraph footer = new Paragraph(
                "Generated on: " + generatedAt
                + "    |    This is a system-generated payslip. No signature is required.",
                FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(8);
        return footer;
    }

    // ── Cell helpers ─────────────────────────────────────────────────────────

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_LABEL));
        cell.setBorderColor(TABLE_BORDER);
        cell.setBackgroundColor(SECTION_BG);
        cell.setPadding(7);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_VALUE));
        cell.setBorderColor(TABLE_BORDER);
        cell.setPadding(7);
        return cell;
    }

    private void addTableHeader(PdfPTable table, String col1, String col2) {
        PdfPCell h1 = new PdfPCell(new Phrase(col1, FONT_TABLE_HDR));
        h1.setBackgroundColor(HEADER_BG);
        h1.setBorder(Rectangle.NO_BORDER);
        h1.setPadding(8);

        PdfPCell h2 = new PdfPCell(new Phrase(col2, FONT_TABLE_HDR));
        h2.setBackgroundColor(HEADER_BG);
        h2.setBorder(Rectangle.NO_BORDER);
        h2.setPadding(8);
        h2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(h1);
        table.addCell(h2);
    }

    private void addMoneyRow(PdfPTable table, String label, BigDecimal amount, boolean shade) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, FONT_TABLE_ROW));
        lCell.setBorderColor(TABLE_BORDER);
        lCell.setPadding(7);
        if (shade) lCell.setBackgroundColor(SECTION_BG);

        PdfPCell aCell = new PdfPCell(new Phrase("₹ " + formatAmount(amount), FONT_TABLE_ROW));
        aCell.setBorderColor(TABLE_BORDER);
        aCell.setPadding(7);
        aCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (shade) aCell.setBackgroundColor(SECTION_BG);

        table.addCell(lCell);
        table.addCell(aCell);
    }

    private void addMoneyTotalRow(PdfPTable table, String label, BigDecimal amount) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, FONT_TOTAL));
        lCell.setBackgroundColor(new Color(215, 219, 221));
        lCell.setBorderColor(TABLE_BORDER);
        lCell.setBorderWidthTop(1.5f);
        lCell.setPadding(8);

        PdfPCell aCell = new PdfPCell(new Phrase("₹ " + formatAmount(amount), FONT_TOTAL));
        aCell.setBackgroundColor(new Color(215, 219, 221));
        aCell.setBorderColor(TABLE_BORDER);
        aCell.setBorderWidthTop(1.5f);
        aCell.setPadding(8);
        aCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(lCell);
        table.addCell(aCell);
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
