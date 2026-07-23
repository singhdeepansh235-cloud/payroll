package com.srmcem.payroll.mail;

import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.entity.PayrollRecord;
import com.srmcem.payroll.util.DateUtil;

public class EmailTemplateUtil {

    private EmailTemplateUtil() {}

    private static final String BASE_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { background-color: #ffffff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); max-width: 600px; margin: 0 auto; }
                    .header { background-color: #3498db; color: white; padding: 10px 20px; border-radius: 5px 5px 0 0; text-align: center; }
                    .content { padding: 20px; color: #333; line-height: 1.6; }
                    .footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; }
                    .highlight { font-weight: bold; color: #2c3e50; }
                    .status-approved { color: #27ae60; font-weight: bold; }
                    .status-rejected { color: #e74c3c; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>SRMCEM Payroll System</h2>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        This is an automated message. Please do not reply.
                    </div>
                </div>
            </body>
            </html>
            """;

    public static String getLeaveApprovedTemplate(LeaveRequest leave) {
        String content = String.format("""
                <p>Dear %s,</p>
                <p>Your leave request has been <span class="status-approved">APPROVED</span>.</p>
                <ul>
                    <li><span class="highlight">Leave Type:</span> %s</li>
                    <li><span class="highlight">Start Date:</span> %s</li>
                    <li><span class="highlight">End Date:</span> %s</li>
                    <li><span class="highlight">Total Days:</span> %d</li>
                    <li><span class="highlight">Admin Remarks:</span> %s</li>
                </ul>
                <p>Have a great time off!</p>
                """,
                leave.getEmployee().getFirstName(),
                leave.getLeaveType(),
                DateUtil.format(leave.getStartDate()),
                DateUtil.format(leave.getEndDate()),
                leave.getTotalDays(),
                leave.getAdminRemarks() != null ? leave.getAdminRemarks() : "None"
        );
        return String.format(BASE_TEMPLATE, content);
    }

    public static String getLeaveRejectedTemplate(LeaveRequest leave) {
        String content = String.format("""
                <p>Dear %s,</p>
                <p>We regret to inform you that your leave request has been <span class="status-rejected">REJECTED</span>.</p>
                <ul>
                    <li><span class="highlight">Leave Type:</span> %s</li>
                    <li><span class="highlight">Dates:</span> %s to %s</li>
                    <li><span class="highlight">Admin Remarks:</span> %s</li>
                </ul>
                <p>Please contact HR for further details.</p>
                """,
                leave.getEmployee().getFirstName(),
                leave.getLeaveType(),
                DateUtil.format(leave.getStartDate()),
                DateUtil.format(leave.getEndDate()),
                leave.getAdminRemarks() != null ? leave.getAdminRemarks() : "None"
        );
        return String.format(BASE_TEMPLATE, content);
    }

    public static String getPayrollGeneratedTemplate(PayrollRecord payroll) {
        String content = String.format("""
                <p>Dear %s,</p>
                <p>Your payroll for the month of <span class="highlight">%s</span> has been successfully generated.</p>
                <p>A summary of your payslip is below:</p>
                <ul>
                    <li><span class="highlight">Basic Salary:</span> ₹%s</li>
                    <li><span class="highlight">Gross Salary:</span> ₹%s</li>
                    <li><span class="highlight">Deductions:</span> ₹%s</li>
                    <li><span class="highlight">Net Salary:</span> ₹%s</li>
                </ul>
                <p>Your official PDF Payslip is attached to this email.</p>
                """,
                payroll.getEmployee().getFirstName(),
                payroll.getPayrollMonth(),
                payroll.getBasicSalary(),
                payroll.getGrossSalary(),
                payroll.getDeductions(),
                payroll.getNetSalary()
        );
        return String.format(BASE_TEMPLATE, content);
    }
}
