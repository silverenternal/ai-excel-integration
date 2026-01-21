package com.example.aiexcel.config;

import com.example.aiexcel.util.ApiResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * 全局异常处理器
 * 统一处理应用程序中的异常并返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex) {
        logger.error("未处理的异常 — 返回 500 给客户端。异常信息: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseUtil.error(
                "Internal server error occurred", 
                ex.getMessage() != null ? ex.getMessage() : "Unknown error"
            ));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        logger.error("File upload size exceeded: ", ex);
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponseUtil.error(
                "File size exceeds maximum allowed size", 
                "Maximum file size is 10MB"
            ));
    }

    /**
     * 处理文件相关的IO异常
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
        logger.error("IO exception occurred: ", ex);
        
        String errorMessage = "Error processing file";
        if (ex.getMessage() != null && ex.getMessage().contains("size")) {
            errorMessage = "File size is too large or invalid";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseUtil.error(errorMessage, ex.getMessage()));
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument provided: ", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseUtil.error("Invalid argument provided", ex.getMessage()));
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException ex) {
        logger.error("Null pointer exception occurred: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseUtil.error(
                "Invalid data provided", 
                "A required field is missing or null"
            ));
    }

    /**
     * 处理404未找到异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NoHandlerFoundException ex) {
        logger.warn("Resource not found: ", ex);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseUtil.error("Requested resource not found", ex.getRequestURL()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleStaticResourceNotFound(NoResourceFoundException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Static resource not found";
        logger.warn("静态资源未找到 — 错误: {}", msg, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseUtil.error("Static resource not found", msg));
    }

    /**
     * 处理Excel相关异常
     */
    @ExceptionHandler(org.apache.poi.openxml4j.exceptions.InvalidOperationException.class)
    public ResponseEntity<Object> handleExcelOperationException(org.apache.poi.openxml4j.exceptions.InvalidOperationException ex) {
        logger.error("Excel operation error: ", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseUtil.error(
                "Invalid Excel operation", 
                "The requested Excel operation could not be completed. Please check your file format."
            ));
    }

    /**
     * 处理自定义业务异常（如果有的话）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleBusinessException(RuntimeException ex) {
        logger.error("Runtime exception occurred: ", ex);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseUtil.error(ex.getMessage(), ex.getClass().getSimpleName()));
    }
}