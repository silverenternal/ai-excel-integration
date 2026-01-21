package com.example.aiexcel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一响应工具类
 * 提供标准化的API响应格式
 */
public class ApiResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseUtil.class);

    /**
     * 成功响应
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        resp.put("timestamp", LocalDateTime.now());
        resp.put("error", null);
        return resp;
    }

    /**
     * 成功响应（带消息）
     */
    public static Map<String, Object> success(Object data, String message) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        resp.put("message", message);
        resp.put("timestamp", LocalDateTime.now());
        resp.put("error", null);
        return resp;
    }

    /**
     * 失败响应
     */
    public static Map<String, Object> error(String errorMessage) {
        logger.debug("构建错误响应 — message: {}", errorMessage);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", false);
        resp.put("data", null);
        resp.put("message", errorMessage);
        resp.put("timestamp", LocalDateTime.now());
        resp.put("error", errorMessage);
        return resp;
    }

    /**
     * 失败响应（带HTTP状态码）
     */
    public static Map<String, Object> error(String errorMessage, HttpStatus status) {
        logger.debug("构建错误响应 — message: {}, status: {}", errorMessage, status);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", false);
        resp.put("data", null);
        resp.put("message", errorMessage);
        resp.put("status", status.value());
        resp.put("timestamp", LocalDateTime.now());
        resp.put("error", errorMessage);
        return resp;
    }

    /**
     * 失败响应（带详细错误信息）
     */
    public static Map<String, Object> error(String errorMessage, String errorDetails) {
        logger.debug("构建错误响应 — message: {}, details: {}", errorMessage, errorDetails);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", false);
        resp.put("data", null);
        resp.put("message", errorMessage);
        resp.put("errorDetails", errorDetails);
        resp.put("timestamp", LocalDateTime.now());
        resp.put("error", errorMessage);
        return resp;
    }
}