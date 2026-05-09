package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) throws MessagingException {

        String verifyLink = "http://localhost:5173/register-verify?email="
                + toEmail + "&code=" + otpCode;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@jobplatform.com");
        helper.setTo(toEmail);
        helper.setSubject("Xác thực tài khoản Job Platform");

        String htmlContent =
                "<div style='font-family:Arial,sans-serif;'>"
                        + "<h2>Xin chào!</h2>"
                        + "<p>Cảm ơn bạn đã đăng ký tài khoản tại <b>Job Platform</b>.</p>"
                        + "<p>Mã xác thực của bạn là:</p>"
                        + "<h1 style='color:blue; letter-spacing:5px;'>"
                        + otpCode
                        + "</h1>"
                        + "<p>Hoặc bạn có thể bấm vào nút bên dưới để xác nhận:</p>"
                        + "<a href='" + verifyLink + "' "
                        + "style='display:inline-block;padding:10px 20px;"
                        + "background-color:#4CAF50;color:white;"
                        + "text-decoration:none;border-radius:5px;'>"
                        + "Xác nhận tài khoản"
                        + "</a>"
                        + "<p style='margin-top:20px;'>Mã sẽ hết hạn sau 5 phút.</p>"
                        + "<p>Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>"
                        + "</div>";

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
    public void sendForgotPasswordEmail(String toEmail, String otpCode) throws MessagingException {

        String resetLink = "http://localhost:5173/forgot-password-verify?email=" + toEmail  + "&code=" + otpCode;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("security@jobplatform.com"); // Đổi sang email bảo mật cho "ngầu"
        helper.setTo(toEmail);
        helper.setSubject("Yêu cầu đặt lại mật khẩu - Job Platform");

        String htmlContent =
                "<div style='font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #eee; padding:20px; border-radius:10px;'>"
                        + "<h2 style='color:#333;'>Khôi phục mật khẩu</h2>"
                        + "<p>Chào bạn,</p>"
                        + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này.</p>"
                        + "<div style='background-color:#f8f9fa; padding:15px; text-align:center; border-radius:5px; margin:20px 0;'>"
                        + "   <p style='margin:0; font-size:14px; color:#666;'>Mã xác thực (OTP) của bạn là:</p>"
                        + "   <h1 style='color:#e74c3c; letter-spacing:8px; margin:10px 0; font-size:36px;'>" + otpCode + "</h1>"
                        + "   <p style='margin:0; font-size:12px; color:#999;'>Mã có hiệu lực trong 5 phút</p>"
                        + "</div>"
                        + "<p>Vui lòng nhập mã này vào trang web hoặc bấm vào nút bên dưới:</p>"
                        + "<div style='text-align:center;'>"
                        + "   <a href='" + resetLink + "' "
                        + "   style='display:inline-block; padding:12px 25px; background-color:#3498db; color:white; text-decoration:none; border-radius:5px; font-weight:bold;'>"
                        + "   Đặt lại mật khẩu ngay"
                        + "   </a>"
                        + "</div>"
                        + "<p style='margin-top:25px; font-size:13px; color:#7f8c8d;'>"
                        + "Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này để giữ an toàn cho tài khoản."
                        + "</p>"
                        + "<hr style='border:none; border-top:1px solid #eee; margin-top:20px;'>"
                        + "<p style='font-size:11px; color:#bdc3c7;'>Đội ngũ bảo mật Job Platform</p>"
                        + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
    public void sendReportFeedbackEmail(String toEmail, String reporterName, Long reportId,
                                        String targetTypeString, String reasonTitle,
                                        ReportStatus status, String adminNote) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("support@jobplatform.com");
        helper.setTo(toEmail);

        String subjectText;
        String statusColor;
        String statusText;
        String mainMessage;

        if (status == ReportStatus.RESOLVED) {
            subjectText = "[Job Platform] Báo cáo vi phạm của bạn đã được giải quyết";
            statusColor = "#27ae60";
            statusText = "ĐÃ XÁC NHẬN VI PHẠM";
            mainMessage = "Cảm ơn bạn đã chung tay bảo vệ cộng đồng Job Platform. Chúng tôi đã tiến hành kiểm tra và áp dụng biện pháp xử lý đối với tài khoản vi phạm dựa trên bằng chứng bạn cung cấp.";
        } else {
            subjectText = "[Job Platform] Phản hồi về báo cáo vi phạm của bạn";
            statusColor = "#e74c3c"; // Màu đỏ
            statusText = "BÁO CÁO BỊ TỪ CHỐI";
            mainMessage = "Chúng tôi đã xem xét kỹ lưỡng báo cáo của bạn. Tuy nhiên, ở thời điểm hiện tại, chúng tôi chưa có đủ cơ sở hoặc bằng chứng để xác định đây là một vi phạm điều khoản của hệ thống.";
        }

        helper.setSubject(subjectText);

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"

                        + "<div style='background-color: #f8fafc; padding: 20px; border-bottom: 1px solid #e2e8f0; text-align: center;'>"
                        + "   <h2 style='color: #1e293b; margin: 0;'>Phản hồi Báo cáo Vi phạm</h2>"
                        + "   <p style='color: #64748b; margin-top: 5px; font-size: 14px;'>Mã báo cáo: <b>#" + reportId + "</b></p>"
                        + "</div>"

                        + "<div style='padding: 30px 20px; background-color: #ffffff;'>"
                        + "   <p style='color: #334155; font-size: 16px;'>Xin chào <b>" + reporterName + "</b>,</p>"
                        + "   <p style='color: #475569; font-size: 15px; line-height: 1.6;'>" + mainMessage + "</p>"

                        + "   <div style='background-color: #f1f5f9; padding: 15px; border-radius: 8px; margin: 25px 0;'>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Đối tượng bị báo cáo:</b> " + targetTypeString + "</p>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Lý do vi phạm:</b> " + reasonTitle + "</p>"
                        + "       <p style='margin: 0; color: #475569; font-size: 14px;'><b>Kết quả xử lý:</b> <span style='color: " + statusColor + "; font-weight: bold; background-color: " + statusColor + "15; padding: 3px 8px; border-radius: 4px;'>" + statusText + "</span></p>"
                        + "   </div>"

                        + "   <h3 style='color: #0f172a; font-size: 15px; margin-bottom: 10px; border-top: 1px solid #e2e8f0; padding-top: 20px;'>💬 Lời nhắn từ Ban Quản Trị:</h3>"
                        + "   <div style='background-color: #fffbeb; border-left: 4px solid #f59e0b; padding: 15px; border-radius: 0 8px 8px 0;'>"
                        + "       <p style='margin: 0; color: #92400e; font-size: 14px; font-style: italic; line-height: 1.5;'>"
                        + "         \"" + (adminNote != null && !adminNote.trim().isEmpty() ? adminNote : "Cảm ơn đóng góp của bạn. Chúng tôi đã ghi nhận và xử lý sự việc theo đúng quy định của Job Platform.") + "\""
                        + "       </p>"
                        + "   </div>"

                        + "   <p style='color: #64748b; font-size: 14px; margin-top: 30px; line-height: 1.6;'>"
                        + "   Nếu bạn có thêm bằng chứng bổ sung hoặc có thắc mắc về quyết định này, vui lòng liên hệ lại với chúng tôi qua email support@jobplatform.com."
                        + "   </p>"
                        + "</div>"

                        //
                        + "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                        + "   <p style='margin: 0; color: #94a3b8; font-size: 12px;'>Đây là email tự động, vui lòng không trả lời trực tiếp email này.</p>"
                        + "   <p style='margin: 5px 0 0 0; color: #94a3b8; font-size: 12px;'>&copy; 2026 Job Platform. Đội ngũ Kiểm Duyệt Chất Lượng.</p>"
                        + "</div>"

                        + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

}