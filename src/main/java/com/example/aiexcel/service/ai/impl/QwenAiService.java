package com.example.aiexcel.service.ai.impl;

import com.example.aiexcel.dto.AiRequest;
import com.example.aiexcel.dto.AiResponse;
import com.example.aiexcel.service.ai.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

@Service
public class QwenAiService implements AiService {

    private static final Logger logger = Logger.getLogger(QwenAiService.class.getName());

    private final String apiBaseUrl;
    private final String defaultModel;
    private final String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private String maskKey(String key) {
        if (key == null) return "<null>";
        String trimmed = key.trim();
        if (trimmed.length() <= 10) return "***";
        return trimmed.substring(0, Math.min(6, trimmed.length())) + "***" + trimmed.substring(Math.max(trimmed.length() - 4, 0));
    }

    private void logExceptionWithTrace(Exception e, String context) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        logger.severe(context + ": " + e.getMessage() + "\n" + sw.toString());
    }

    public QwenAiService(@Value("${qwen.api.api-key:}") String apiKeyFromConfig,
                         @Value("${qwen.api.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                         @Value("${qwen.api.default-model:qwen-max}") String model) {
        // 尝试按优先级解析 API Key：环境变量优先（DASHSCOPE_API_KEY -> QWEN_API_KEY），
        // 然后 System properties，再最后使用配置注入的值（用于测试）。
        String resolvedKey = null;

        // 1) 优先 DASHSCOPE_API_KEY（适配 DashScope 文档推荐）
        resolvedKey = System.getenv("DASHSCOPE_API_KEY");
        if (resolvedKey != null && !resolvedKey.isEmpty()) {
            logger.info("Qwen API key resolved from environment variable DASHSCOPE_API_KEY");
        }

        // 2) 回退到 QWEN_API_KEY（历史兼容）
        if (resolvedKey == null || resolvedKey.isEmpty()) {
            resolvedKey = System.getenv("QWEN_API_KEY");
            if (resolvedKey != null && !resolvedKey.isEmpty()) {
                logger.info("Qwen API key resolved from environment variable QWEN_API_KEY");
            }
        }

        // 3) 再尝试 System properties
        if (resolvedKey == null || resolvedKey.isEmpty()) {
            resolvedKey = System.getProperty("qwen.api.api-key");
            if (resolvedKey != null && !resolvedKey.isEmpty()) {
                logger.info("Qwen API key resolved from System properties");
            }
        }

        // 4) 最后使用注入的配置值（通常来自 application.properties 或 EnvFile 环境注入）
        if (resolvedKey == null || resolvedKey.isEmpty()) {
            resolvedKey = apiKeyFromConfig;
            if (resolvedKey != null && !resolvedKey.isEmpty()) {
                logger.info("Qwen API key resolved from configuration property qwen.api.api-key");
            }
        }

        this.apiKey = resolvedKey;
        // 简单格式校验并给出可操作提示（避免常见填错其他厂商的 key）
        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            String trimmed = this.apiKey.trim();
            if (!trimmed.startsWith("sk-")) {
                logger.warning("Resolved API key does not start with 'sk-'. Ensure you are using a DashScope API key (sk-...).");
            }
        }
        // 确保 baseUrl 有效（防止被错误覆盖为空或被截断导致 HttpClient 报错 "Target host is not specified"）
        String resolvedBase = baseUrl != null ? baseUrl.trim() : null;

        // 尝试从 System properties 或环境变量回退（如果传入的 baseUrl 看起来不完整）
        if (resolvedBase == null || resolvedBase.isEmpty()) {
            resolvedBase = System.getProperty("qwen.api.base-url");
            if (resolvedBase == null || resolvedBase.isEmpty()) {
                resolvedBase = System.getenv("QWEN_API_BASE_URL");
            }
        }

        // 简单校验 URL 是否看起来像一个完整的主机地址（protocol://host...）
        boolean validBase = resolvedBase != null && resolvedBase.matches("^https?://[^/\\s]+.*$");
        if (!validBase) {
            String received = resolvedBase == null ? "<null>" : resolvedBase;
            logger.warning("qwen.api.base-url appears invalid: '" + received + "'. Falling back to default base URL.");
            this.apiBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        } else {
            this.apiBaseUrl = resolvedBase;
        }

        logger.info("qwen.api.base-url resolved to: " + this.apiBaseUrl);
        this.defaultModel = model;
    }

    @Override
    public AiResponse generateResponse(AiRequest request) {
        // 如果没有设置模型，使用默认模型
        if (request.getModel() == null || request.getModel().isEmpty()) {
            request.setModel(defaultModel);
        }

        // 检查API密钥 - 仅使用配置属性获取的值
        if (apiKey == null || apiKey.isEmpty()) {
            logger.severe("API Key is not configured. Please set QWEN_API_KEY in .env file.");
            throw new RuntimeException("API Key is not configured. Please set QWEN_API_KEY in .env file.");
        }

        try {
            // 创建请求体 - 移除不被Qwen API支持的字段
            QwenRequest qwenRequest = new QwenRequest();
            qwenRequest.setModel(request.getModel());
            qwenRequest.setMessages(request.getMessages());
            qwenRequest.setTemperature(request.getTemperature());
            qwenRequest.setMax_tokens(request.getMaxTokens());
            qwenRequest.setStream(request.getStream());

            String requestBody = objectMapper.writeValueAsString(qwenRequest);

            // 日志：记录请求目标和模型（不记录完整 API Key）
            try {
                logger.info("Qwen request -> url=" + apiBaseUrl + "/chat/completions" + ", model=" + qwenRequest.getModel() + ", apiKeyPresent=" + (apiKey != null && !apiKey.isEmpty()));
                logger.info("Qwen API key (masked): " + maskKey(apiKey));
                logger.fine("Qwen request body: " + requestBody);
            } catch (Exception ignore) {
                // 日志尽力而为，不能让日志抛出异常影响主流程
            }

            // 创建HTTP请求
            HttpPost httpPost = new HttpPost(apiBaseUrl + "/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // 发送请求，使用 ResponseHandler
            String responseString = httpClient.execute(httpPost, httpResponse -> {
                HttpEntity responseEntity = httpResponse.getEntity();
                String body;
                try {
                    body = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                } catch (org.apache.hc.core5.http.ParseException e) {
                    logExceptionWithTrace(e, "Error parsing HTTP response");
                    throw new RuntimeException("Error parsing HTTP response", e);
                }

                int status = httpResponse.getCode();
                if (status != 200) {
                    // 更丰富的错误日志
                    logger.severe("API request failed -> url="+ apiBaseUrl + "/chat/completions" + ", status=" + status + ", model=" + qwenRequest.getModel());
                    logger.severe("Response body: " + body);
                    logger.severe("API key (masked): " + maskKey(apiKey));

                    // 针对 401 提供可操作提示（参考 DashScope 文档）
                    if (status == 401) {
                        try {
                            if (body != null && body.contains("invalid_api_key")) {
                                logger.warning("Invalid API key provided (invalid_api_key). Suggestions:");
                                logger.warning(" - Ensure you're providing the correct API key (starts with 'sk-') and not a literal code snippet.");
                                logger.warning(" - If you set the key via environment variables, prefer 'DASHSCOPE_API_KEY' or 'QWEN_API_KEY'.");
                                logger.warning(" - Confirm the Base URL matches the key's region: use 'dashscope.aliyuncs.com' for China (Beijing) or 'dashscope-intl.aliyuncs.com' for Intl (Singapore).");
                                logger.warning(" - If unsure, re-create or retrieve a fresh API key from DashScope console.");
                            }
                        } catch (Exception ignore) {
                        }
                    }

                    throw new RuntimeException("API request failed with status: " + status + ", response: " + body);
                }

                return body;
            });

            // 解析响应
            AiResponse aiResponse;
            try {
                aiResponse = objectMapper.readValue(responseString, AiResponse.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.severe("Error parsing AI response: " + e.getMessage());
                logger.severe("Response content: " + responseString);
                logExceptionWithTrace(e, "Error parsing AI response");
                throw new RuntimeException("Error parsing AI response", e);
            }

            return aiResponse;
        } catch (IOException e) {
            logger.severe("Error calling Qwen API: " + e.getMessage());
            throw new RuntimeException("Error calling Qwen API", e);
        }
    }

    @Override
    public AiResponse generateStreamingResponse(AiRequest request) {
        // 对于流式响应，我们暂时返回非流式的响应
        // 在实际实现中，这里应该使用 SSE 或 WebSocket 实现真正的流式响应
        request.setStream(true);
        return generateResponse(request);
    }

    @Override
    public boolean testConnection() {
        // 检查API密钥 - 仅使用配置属性获取的值
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warning("API Key is not configured for connection test.");
            return false;
        }

        try {
            AiRequest testRequest = new AiRequest();
            testRequest.setModel(defaultModel);
            testRequest.setMessages(java.util.Arrays.asList(
                new AiRequest.Message("user", "Hello")
            ));
            testRequest.setTemperature(0.1);
            testRequest.setMaxTokens(10);

            AiResponse response = generateResponse(testRequest);
            return response != null && response.getChoices() != null &&
                   response.getChoices().length > 0;
        } catch (Exception e) {
            logger.warning("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    // 内部类用于适配Qwen API格式
    @SuppressWarnings("unused")
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    private static class QwenRequest {
        private String model;
        private java.util.List<AiRequest.Message> messages;
        private Double temperature;
        private Integer max_tokens;
        private Boolean stream;

        public String getModel() { return model; }
        public java.util.List<AiRequest.Message> getMessages() { return messages; }
        public Double getTemperature() { return temperature; }
        public Integer getMax_tokens() { return max_tokens; }
        public Boolean getStream() { return stream; }
        

        public void setModel(String model) { this.model = model; }
        public void setMessages(java.util.List<AiRequest.Message> messages) { this.messages = messages; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public void setMax_tokens(Integer max_tokens) { this.max_tokens = max_tokens; }
        public void setStream(Boolean stream) { this.stream = stream; }
    }
}