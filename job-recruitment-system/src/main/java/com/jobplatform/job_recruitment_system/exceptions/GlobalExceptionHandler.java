package com.jobplatform.job_recruitment_system.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<Map<String, Object>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", errorCode.name());
        errorResponse.put("error", errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(errorResponse);
    }
    
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handlingRuntimeException(RuntimeException exception) {
        Map<String, Object> errorResponse = new HashMap<>();
        

        errorResponse.put("error", exception.getMessage());

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(errorResponse);
    }
    // bắt lỗi cho dto
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handlingValidationException(MethodArgumentNotValidException exception) {

        // 1. Spring sẽ tóm lấy lỗi đầu tiên (ví dụ: "INVALID_EMAIL") từ DTO
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode;
        try {
            // 2. Chuyển chuỗi "INVALID_EMAIL" thành Enum ErrorCode.INVALID_EMAIL
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // 3. Đề phòng trường hợp bạn gõ tên message trong DTO mà quên chưa khai báo trong ErrorCode
            errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        }

        // 4. Trả về JSON y hệt như cấu trúc của AppException
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", errorCode.name());
        errorResponse.put("error", errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(errorResponse);
    }

}
