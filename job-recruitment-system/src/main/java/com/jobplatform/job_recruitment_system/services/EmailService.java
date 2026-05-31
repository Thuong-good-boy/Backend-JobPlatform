package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.request.SendEmailRequest;
import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    @Lazy
    private UserService userService;
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) throws MessagingException {

        String verifyLink = "https://pathuongdev.id.vn/register-verify?email="
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
    @Async
    public void sendForgotPasswordEmail(String toEmail, String otpCode) throws MessagingException {

        String resetLink = "https://pathuongdev.id.vn/forgot-password-verify?email=" + toEmail  + "&code=" + otpCode;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("security@jobplatform.com");
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
    @Async
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
            statusColor = "#e74c3c";
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
    public void sendCandidateAccountLockedEmail(String toEmail, String candidateName,
                                                String reasonTitle, String adminNote) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("support@jobplatform.com");
        helper.setTo(toEmail);
        helper.setSubject("[Job Platform] THÔNG BÁO QUAN TRỌNG: Tài khoản của bạn đã bị khóa");

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"

                        + "<div style='background-color: #fee2e2; padding: 20px; border-bottom: 1px solid #fca5a5; text-align: center;'>"
                        + "   <h2 style='color: #b91c1c; margin: 0;'>Tài Khoản Đã Bị Khóa</h2>"
                        + "</div>"

                        + "<div style='padding: 30px 20px; background-color: #ffffff;'>"
                        + "   <p style='color: #334155; font-size: 16px;'>Xin chào <b>" + candidateName + "</b>,</p>"
                        + "   <p style='color: #475569; font-size: 15px; line-height: 1.6;'>"
                        + "     Chúng tôi rất tiếc phải thông báo rằng tài khoản ứng viên của bạn trên Job Platform đã bị tạm khóa do vi phạm Tiêu chuẩn Cộng đồng & Điều khoản Dịch vụ của chúng tôi."
                        + "   </p>"

                        + "   <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 25px 0; border-left: 4px solid #ef4444;'>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Lý do vi phạm:</b> <span style='color: #ef4444; font-weight: bold;'>" + reasonTitle + "</span></p>"
                        + "       <p style='margin: 0; color: #475569; font-size: 14px;'><b>Chi tiết từ Ban Quản Trị:</b> <i>" + (adminNote != null && !adminNote.isEmpty() ? adminNote : "Hồ sơ hoặc hành vi của bạn đã vi phạm quy định của hệ thống.") + "</i></p>"
                        + "   </div>"

                        + "   <p style='color: #64748b; font-size: 14px; line-height: 1.6;'>"
                        + "     Việc khóa tài khoản đồng nghĩa với việc bạn không thể đăng nhập, ứng tuyển hoặc tương tác trên nền tảng. "
                        + "     Nếu bạn cho rằng quyết định này là một sự nhầm lẫn, vui lòng phản hồi lại email này kèm theo các minh chứng để chúng tôi xem xét lại."
                        + "   </p>"
                        + "</div>"

                        + "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                        + "   <p style='margin: 0; color: #94a3b8; font-size: 12px;'>Đây là email tự động từ hệ thống Job Platform.</p>"
                        + "   <p style='margin: 5px 0 0 0; color: #94a3b8; font-size: 12px;'>&copy; 2026 Job Platform. Đội ngũ Kiểm duyệt & Bảo mật.</p>"
                        + "</div>"

                        + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
    public void sendCompanyAccountLockedEmail(String toEmail, String companyName,
                                              String reasonTitle, String adminNote) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("support@jobplatform.com");
        helper.setTo(toEmail);
        helper.setSubject("[Job Platform] THÔNG BÁO QUAN TRỌNG: Tài khoản Doanh nghiệp của bạn đã bị đình chỉ");

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"

                        + "<div style='background-color: #fee2e2; padding: 20px; border-bottom: 1px solid #fca5a5; text-align: center;'>"
                        + "   <h2 style='color: #b91c1c; margin: 0;'>Đình Chỉ Tài Khoản Doanh Nghiệp</h2>"
                        + "</div>"

                        + "<div style='padding: 30px 20px; background-color: #ffffff;'>"
                        + "   <p style='color: #334155; font-size: 16px;'>Kính gửi <b>" + companyName + "</b>,</p>"
                        + "   <p style='color: #475569; font-size: 15px; line-height: 1.6;'>"
                        + "     Chúng tôi rất tiếc phải thông báo rằng tài khoản nhà tuyển dụng của Quý công ty trên Job Platform đã bị tạm thời đình chỉ. Quyết định này được đưa ra sau khi chúng tôi xác minh các báo cáo vi phạm liên quan đến hoạt động của công ty trên nền tảng."
                        + "   </p>"

                        + "   <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 25px 0; border-left: 4px solid #ef4444;'>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Lý do vi phạm:</b> <span style='color: #ef4444; font-weight: bold;'>" + reasonTitle + "</span></p>"
                        + "       <p style='margin: 0; color: #475569; font-size: 14px;'><b>Chi tiết xử lý từ Ban Quản Trị:</b> <i>" + (adminNote != null && !adminNote.trim().isEmpty() ? adminNote : "Hoạt động hoặc thông tin của công ty không tuân thủ Điều khoản Dịch vụ của hệ thống.") + "</i></p>"
                        + "   </div>"

                        + "   <p style='color: #64748b; font-size: 14px; line-height: 1.6;'>"
                        + "     Trong thời gian đình chỉ, toàn bộ tin tuyển dụng của công ty sẽ bị ẩn và Quý công ty không thể truy cập vào hệ thống quản trị. "
                        + "     Nếu Quý công ty cho rằng đây là một sự nhầm lẫn hoặc cần hỗ trợ làm rõ vấn đề, vui lòng phản hồi lại email này kèm theo các tài liệu/chứng từ liên quan để ban quản trị xem xét."
                        + "   </p>"
                        + "</div>"

                        + "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                        + "   <p style='margin: 0; color: #94a3b8; font-size: 12px;'>Đây là email tự động từ hệ thống Job Platform.</p>"
                        + "   <p style='margin: 5px 0 0 0; color: #94a3b8; font-size: 12px;'>&copy; 2026 Job Platform. Đội ngũ Kiểm duyệt chất lượng.</p>"
                        + "</div>"

                        + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
    public void sendJobRemovedEmail(String toEmail, String companyName, String jobTitle,
                                    String reasonTitle, String adminNote) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("support@jobplatform.com");
        helper.setTo(toEmail);
        helper.setSubject("[Job Platform] THÔNG BÁO: Tin tuyển dụng của bạn đã bị gỡ do vi phạm");

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>"

                        + "<div style='background-color: #ffedd5; padding: 20px; border-bottom: 1px solid #fdba74; text-align: center;'>"
                        + "   <h2 style='color: #c2410c; margin: 0;'>Gỡ Tin Tuyển Dụng</h2>"
                        + "</div>"

                        + "<div style='padding: 30px 20px; background-color: #ffffff;'>"
                        + "   <p style='color: #334155; font-size: 16px;'>Kính gửi <b>" + companyName + "</b>,</p>"
                        + "   <p style='color: #475569; font-size: 15px; line-height: 1.6;'>"
                        + "     Chúng tôi xin thông báo rằng một tin tuyển dụng của Quý công ty trên Job Platform vừa bị buộc phải gỡ bỏ (chuyển sang trạng thái Đóng) do phát hiện vi phạm Tiêu chuẩn Cộng đồng của hệ thống."
                        + "   </p>"

                        + "   <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 25px 0; border-left: 4px solid #f97316;'>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Tin tuyển dụng:</b> <span style='color: #1e293b; font-weight: bold;'>" + jobTitle + "</span></p>"
                        + "       <p style='margin: 0 0 10px 0; color: #475569; font-size: 14px;'><b>Lý do vi phạm:</b> <span style='color: #ea580c; font-weight: bold;'>" + reasonTitle + "</span></p>"
                        + "       <p style='margin: 0; color: #475569; font-size: 14px;'><b>Ghi chú từ Kiểm duyệt viên:</b> <i>" + (adminNote != null && !adminNote.trim().isEmpty() ? adminNote : "Nội dung tin tuyển dụng không phù hợp với quy định của nền tảng.") + "</i></p>"
                        + "   </div>"

                        + "   <p style='color: #64748b; font-size: 14px; line-height: 1.6;'>"
                        + "     Tài khoản doanh nghiệp của Quý công ty vẫn hoạt động bình thường. Tuy nhiên, việc đăng tải nhiều tin vi phạm có thể dẫn đến hình thức xử lý nặng hơn (đình chỉ tài khoản). "
                        + "     Vui lòng kiểm tra lại nội dung các tin tuyển dụng khác. Nếu Quý công ty có thắc mắc, vui lòng phản hồi lại email này."
                        + "   </p>"
                        + "</div>"

                        + "<div style='background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;'>"
                        + "   <p style='margin: 0; color: #94a3b8; font-size: 12px;'>Đây là email tự động từ hệ thống Job Platform.</p>"
                        + "   <p style='margin: 5px 0 0 0; color: #94a3b8; font-size: 12px;'>&copy; 2026 Job Platform. Đội ngũ Kiểm duyệt chất lượng.</p>"
                        + "</div>"

                        + "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
    @Async
    public void sendInterviewEmail(SendEmailRequest request) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(request.getEmail());
        helper.setSubject(request.getSubject());
        String buttonUrl = "https://pathuongdev.id.vn/messages";
        String buttonText = "💬 Nhắn tin trao đổi ngay";

        if (request.getJobId() != null) {
            buttonUrl = "https://pathuongdev.id.vn/jobs/" + request.getJobId();
            buttonText = "🚀 Ứng tuyển ngay";
        }
        String contentWithBr = request.getBody().replace("\n", "<br>");
        String htmlBody =
                "<div style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; margin: 20px auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 12px; color: #334155; background-color: #ffffff; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);\">"
                        + "    "
                        + "    <div style=\"text-align: center; margin-bottom: 24px; padding-bottom: 15px; border-bottom: 1px solid #f1f5f9;\">"
                        + "        <h2 style=\"color: #1e40af; margin: 0; font-size: 20px; font-weight: 600; letter-spacing: 0.5px;\">HỆ THỐNG TUYỂN DỤNG JOBPLATFORM</h2>"
                        + "    </div>"
                        + "    "
                        + "    "
                        + "    <div style=\"font-size: 15px; line-height: 1.7; color: #334155; min-height: 100px;\">"
                        +          contentWithBr
                        + "    </div>"
                        + "    "
                        + "    "
                        + "    <div style=\"text-align: center; margin: 35px 0 25px 0;\">"
                        + "        <a href=\"" + buttonUrl + "\" target=\"_blank\" style=\"background-color: #2563eb; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 8px; font-weight: 600; display: inline-block; font-size: 15px; box-shadow: 0 4px 10px rgba(37, 99, 235, 0.2);\">"
                        +              buttonText
                        + "        </a>"
                        + "    </div>"
                        + "    "
                        + "    "
                        + "    <hr style=\"border: 0; border-top: 1px solid #f1f5f9; margin: 25px 0;\" />"
                        + "    <div style=\"text-align: center; font-size: 12px; color: #94a3b8; line-height: 1.5;\">"
                        + "        <p style=\"margin: 0 0 4px 0;\">Đây là email tự động được gửi từ hệ thống <strong>JobPlatform</strong>.</p>"
                        + "        <p style=\"margin: 0;\">Nếu cần hỗ trợ, bạn có thể bấm vào nút nhắn tin ở trên để liên hệ trực tiếp với Nhà tuyển dụng.</p>"
                        + "    </div>"
                        + "</div>";

        // Gán nội dung HTML vào mail helper
        helper.setText(htmlBody, true);
        helper.setFrom("email.he.thong.cua.ban@gmail.com", "Hệ thống JobPlatform");

        if ("company".equalsIgnoreCase(request.getSenderType())) {
            Long UserId = userService.getCurrentUserId();
            User user = userService.getReferenceById(UserId);
            helper.setReplyTo(user.getEmail());
        } else {
            helper.setReplyTo("noreply@jobplatform.com");
        }

        mailSender.send(message);
    }

}