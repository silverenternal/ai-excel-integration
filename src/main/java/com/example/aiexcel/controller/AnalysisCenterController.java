package com.example.aiexcel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析中心控制器
 * 提供业务分析功能的API
 */
@RestController
@RequestMapping("/api/analysis-center")
public class AnalysisCenterController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisCenterController.class);

    /**
     * 获取分析中心数据
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAnalysisCenterData() {
        logger.info("Received request to get analysis center data");

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("salesTrend", "上升趋势");
            data.put("topProducts", List.of("产品A", "产品B", "产品C"));
            data.put("customerDistribution", Map.of("新客户", 30, "老客户", 70));
            data.put("revenueGrowth", "+15%");

            Map<String, Object> response = Map.of(
                "success", true,
                "data", data
            );

            logger.info("Successfully returned analysis center data");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving analysis center data: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving analysis center data: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取系统设置
     */
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSystemSettings(HttpServletRequest request) {
        logger.info("Received request to get system settings");

        try {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String apiEndpoint = scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port);

            Map<String, Object> data = new HashMap<>();
            data.put("apiEndpoint", apiEndpoint);
            data.put("serverPort", port);
            data.put("language", "zh-CN");
            data.put("theme", "dark");
            data.put("autoSave", true);
            data.put("notifications", true);

            Map<String, Object> response = Map.of(
                "success", true,
                "data", data
            );

            logger.info("Successfully returned system settings: {}", apiEndpoint);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving system settings: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error retrieving system settings: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 保存系统设置
     */
    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> saveSystemSettings(@RequestBody Map<String, Object> settings) {
        logger.info("Received request to save system settings");

        try {
            // 这里可以添加实际的设置保存逻辑
            // 为了演示目的，我们只是简单地返回接收到的设置

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "设置保存成功",
                "data", settings
            );

            logger.info("Successfully saved system settings");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving system settings: {}", e.getMessage(), e);
            Map<String, Object> response = Map.of(
                "success", false,
                "error", "Error saving system settings: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}