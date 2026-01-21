package com.example.aiexcel.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 提供运行时前端需要的应用配置（例如后端端口 / API base url）
 */
@RestController
@RequestMapping("/api")
public class ConfigController {

    @Value("${server.port:8081}")
    private String serverPort;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        String baseUrl = "http://localhost:" + serverPort;
        Map<String, String> resp = new HashMap<>();
        resp.put("serverPort", serverPort);
        resp.put("apiBaseUrl", baseUrl);
        return ResponseEntity.ok(resp);
    }
}
