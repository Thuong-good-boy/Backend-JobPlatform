package com.jobplatform.job_recruitment_system.services;

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

        String verifyLink = "http://localhost:5173/verify?email="
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

}