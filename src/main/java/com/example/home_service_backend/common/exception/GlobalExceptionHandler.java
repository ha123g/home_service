package com.example.home_service_backend.common.exception;

import com.example.home_service_backend.common.result.Result;
import com.example.home_service_backend.common.result.ResultFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<? extends Result> handleBusinessException(BusinessException ex) {
        HttpStatus status = mapStatus(ex.getCode());
        return ResponseEntity.status(status)
                .body(ResultFactory.buildError(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<? extends Result> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResultFactory.buildError("403", "无权限访问该资源"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<? extends Result> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ResultFactory.buildError("400", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<? extends Result> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(item -> item.getPropertyPath() + ": " + item.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ResultFactory.buildError("400", message));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<? extends Result> handleMultipartException() {
        return ResponseEntity.badRequest().body(ResultFactory.buildError("400", "上传文件过大或格式不完整"));
    }

    private String formatFieldError(FieldError error) {
        String field = error.getField();
        String msg = error.getDefaultMessage();
        return field + ": " + msg;
    }

    private HttpStatus mapStatus(String code) {
        return switch (code) {
            case ResultFactory.UNAUTHORIZED_CODE -> HttpStatus.UNAUTHORIZED;
            case "403" -> HttpStatus.FORBIDDEN;
            case "409" -> HttpStatus.CONFLICT;
            case "502" -> HttpStatus.BAD_GATEWAY;
            case "503" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "500" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
