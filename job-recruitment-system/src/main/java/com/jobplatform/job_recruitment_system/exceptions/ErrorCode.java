package com.jobplatform.job_recruitment_system.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 1. Authentication & Authorization Errors
    AUTH_001("Refresh token đã hết hạn. Vui lòng đăng nhập lại!", HttpStatus.FORBIDDEN),
    AUTH_002("Refresh Token không tồn tại trong hệ thống!", HttpStatus.UNAUTHORIZED),
    AUTH_003("Token Google không hợp lệ!", HttpStatus.UNAUTHORIZED),
    AUTH_004("Không tìm thấy người dùng sau khi xác thực!", HttpStatus.UNAUTHORIZED),
    AUTH_005("Sai email hoặc mật khẩu!", HttpStatus.UNAUTHORIZED),
    AUTH_006("Tài khoản của bạn không có quyền đăng nhập vào vai trò này!", HttpStatus.FORBIDDEN),
    AUTH_007("Phiên đổi mật khẩu không hợp lệ hoặc đã hết hạn!", HttpStatus.FORBIDDEN),
    AUTH_008("Tài khoản không tồn tại!", HttpStatus.FORBIDDEN),

    // 2. User & Account Errors
    USER_001("Email đã tồn tại trong hệ thống!", HttpStatus.BAD_REQUEST),
    USER_002("Tài khoản này đã tồn tại và được kích hoạt!", HttpStatus.BAD_REQUEST),
    USER_003("Email không tồn tại trong hệ thống!", HttpStatus.NOT_FOUND),
    USER_004("Lỗi hệ thống khi xác thực tài khoản", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_005("Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_006("Mã OTP không hợp lệ hoặc đã hết hạn!", HttpStatus.BAD_REQUEST),
    USER_007("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại!", HttpStatus.BAD_REQUEST),
    USER_008("Phiên đăng ký đã hết hạn. Vui lòng quay lại điền form đăng ký từ đầu!", HttpStatus.BAD_REQUEST),
    USER_009("Không thể gửi email OTP lúc này. Vui lòng thử lại!", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_010("Lỗi khi gửi lại email", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_011("Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_012("Tài khoản này không tìm thấy hồ sơ ứng tuyển này tồn tại!",HttpStatus.INTERNAL_SERVER_ERROR),

    // 3. Job Recruitment Errors
    JOB_001("Không tìm thấy tin tuyển dụng!", HttpStatus.NOT_FOUND),
    JOB_002("Tài khoản của bạn chưa được xác thực. Vui lòng chờ duyệt GPKD!", HttpStatus.FORBIDDEN),
    JOB_003("Bạn phải tạo hồ sơ công ty trước khi đăng tin!", HttpStatus.BAD_REQUEST),
    JOB_004("Bạn không có quyền chỉnh sửa tin này!", HttpStatus.FORBIDDEN),
    JOB_005("Bạn không có quyền thay đổi trạng thái tin này!", HttpStatus.FORBIDDEN),
    JOB_006("Bạn không có quyền xóa tin này!", HttpStatus.FORBIDDEN),
    JOB_007("Tài khoản này đã ứng tuyển công việc này rồi !",HttpStatus.INTERNAL_SERVER_ERROR),
    JOB_008("Công việc này bạn đã lưu từ trước rồi!",HttpStatus.BAD_REQUEST),


    ROOM_001("Không tìm thấy phòng chat!",HttpStatus.NOT_FOUND),
    // 4. Company Profile Errors
    COM_001("Không tìm thấy thông tin công ty!", HttpStatus.NOT_FOUND),
    COM_002("Công ty đã được xác thực trước đó!", HttpStatus.BAD_REQUEST),
    COM_003("Ảnh không rõ nét, AI không đọc được thông tin!", HttpStatus.BAD_REQUEST),
    COM_004("Thông tin trên giấy phép không khớp với hồ sơ hiện tại!", HttpStatus.BAD_REQUEST),
    COM_005("Tài khoản của bạn chưa được xác thực. Vui lòng chờ duyệt GPKD!",HttpStatus.BAD_REQUEST),
    // 5. Candidate & CV Errors
    CV_001("Chưa có profile ứng viên", HttpStatus.NOT_FOUND),
    CV_002("Không tìm thấy CV", HttpStatus.NOT_FOUND),
    CV_003("Không có quyền xóa CV này", HttpStatus.FORBIDDEN),
    CV_004("Không tìm thấy CV mặc định của user", HttpStatus.NOT_FOUND),
    CV_005("Cv này không thuộc quyền của bạn!", HttpStatus.INTERNAL_SERVER_ERROR),

    GPKD_001("Xác thực giấy kinh doanh bằng ai thành công!", HttpStatus.OK),
    GPKD_002("Xác thực giấy kinh doanh bằng ai thất bại!",HttpStatus.BAD_GATEWAY),
    GPKD_003("Lỗi trong quá trình quét AI",HttpStatus.BAD_GATEWAY),
    // 6. Job Application Errors
    APP_001("Không tìm thấy đơn ứng tuyển!", HttpStatus.NOT_FOUND),
    APP_002("Không tìm thấy hồ sơ ứng tuyển này", HttpStatus.NOT_FOUND),

    // 7. Chat Errors
    CHAT_001("Không tìm thấy phòng chat!", HttpStatus.NOT_FOUND),

    // DEFAULT
    UNCATEGORIZED_EXCEPTION("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    EMAIL_REQUIRED("Email không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("Email không đúng định dạng!", HttpStatus.BAD_REQUEST),

    PASSWORD_REQUIRED("Mật khẩu không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("Mật khẩu phải có ít nhất 6 ký tự (chữ và số)!", HttpStatus.BAD_REQUEST),

    ROLE_REQUIRED("Vai trò không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_ROLE("Vai trò không hợp lệ!", HttpStatus.BAD_REQUEST),

    OTP_REQUIRED("Mã OTP không được để trống!", HttpStatus.BAD_REQUEST),
    INVALID_OTP("Mã OTP phải bao gồm đúng 6 chữ số!", HttpStatus.BAD_REQUEST),
    TOKEN_REQUIRED("Mã xác thực không được để trống!", HttpStatus.BAD_REQUEST),
    JOB_ID_INVALID("công việc không hợp lệ!", HttpStatus.BAD_REQUEST),
    SORT_BY_INVALID("Tiêu chí sắp xếp không hợp lệ!", HttpStatus.BAD_REQUEST),;



    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
