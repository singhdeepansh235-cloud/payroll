package com.srmcem.payroll.mail;

import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.entity.PayrollRecord;

public interface MailService {
    
    void sendLeaveApprovedEmail(LeaveRequest leaveRequest);
    
    void sendLeaveRejectedEmail(LeaveRequest leaveRequest);
    
    void sendPayrollAndPayslipEmail(PayrollRecord payrollRecord, byte[] payslipPdf);
}
