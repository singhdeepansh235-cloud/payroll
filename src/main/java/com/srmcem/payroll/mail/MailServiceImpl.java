package com.srmcem.payroll.mail;

import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.entity.PayrollRecord;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendLeaveApprovedEmail(LeaveRequest leaveRequest) {
        String to = leaveRequest.getEmployee().getEmail();
        String subject = "Leave Request Approved - SRMCEM Payroll";
        String htmlBody = EmailTemplateUtil.getLeaveApprovedTemplate(leaveRequest);
        sendHtmlEmail(to, subject, htmlBody, null, null);
    }

    @Async
    @Override
    public void sendLeaveRejectedEmail(LeaveRequest leaveRequest) {
        String to = leaveRequest.getEmployee().getEmail();
        String subject = "Leave Request Rejected - SRMCEM Payroll";
        String htmlBody = EmailTemplateUtil.getLeaveRejectedTemplate(leaveRequest);
        sendHtmlEmail(to, subject, htmlBody, null, null);
    }

    @Async
    @Override
    public void sendPayrollAndPayslipEmail(PayrollRecord payrollRecord, byte[] payslipPdf) {
        String to = payrollRecord.getEmployee().getEmail();
        String subject = "Payslip Generated: " + payrollRecord.getPayrollMonth() + " - SRMCEM Payroll";
        String htmlBody = EmailTemplateUtil.getPayrollGeneratedTemplate(payrollRecord);
        String attachmentName = "Payslip_" + payrollRecord.getPayrollMonth() + ".pdf";
        sendHtmlEmail(to, subject, htmlBody, attachmentName, payslipPdf);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody, String attachmentName, byte[] attachmentData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true indicates multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, (attachmentName != null), "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML
            
            if (attachmentName != null && attachmentData != null) {
                helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData));
            }
            
            mailSender.send(message);
            log.info("Successfully sent email to: {} with subject: {}", to, subject);
        } catch (MessagingException | MailException e) {
            // Log the error but do not throw it so we don't crash the calling business logic
            log.error("Failed to send email to: {}. Reason: {}", to, e.getMessage());
        }
    }
}
