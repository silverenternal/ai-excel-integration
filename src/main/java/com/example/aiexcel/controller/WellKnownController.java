package com.example.aiexcel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {

    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<String> devtools() {
        // 返回空的 JSON，避免静态资源查找异常
        return ResponseEntity.ok("{}");
    }
}
