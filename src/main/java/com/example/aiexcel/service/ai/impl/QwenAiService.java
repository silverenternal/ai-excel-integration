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
import java.util.logging.Logger;

@Service
public class QwenAiService implements AiService {

    private static final Logger logger = Logger.getLogger(QwenAiService.class.getName());

    private final String apiBaseUrl;
    private final String defaultModel;
    private final String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public QwenAiService(@Value("${qwen.api.api-key:}") String apiKeyFromConfig,
                         @Value("${qwen.api.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                         @Value("${qwen.api.default-model:qwen-max}") String model) {
        // 仅使用从配置属性获取的API Key，系统会自动从.env文件加载
        String resolvedKey = apiKeyFromConfig;
        if (resolvedKey == null || resolvedKey.isEmpty()) {
            // 回退到 System properties
            resolvedKey = System.getProperty("qwen.api.api-key");
            if (resolvedKey != null && !resolvedKey.isEmpty()) {
                logger.info("Qwen API key resolved from System properties");
            }
        }

        if (resolvedKey == null || resolvedKey.isEmpty()) {
            // 回退到环境变量
            resolvedKey = System.getenv("QWEN_API_KEY");
            if (resolvedKey != null && !resolvedKey.isEmpty()) {
                logger.info("Qwen API key resolved from environment variable QWEN_API_KEY");
            }
        }

        this.apiKey = resolvedKey;
        this.apiBaseUrl = baseUrl;
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

            // 创建HTTP请求
            HttpPost httpPost = new HttpPost(apiBaseUrl + "/chat/completions");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // 发送请求，使用 ResponseHandler 替代已弃用的 execute(ClassicHttpRequest)
            String responseString = httpClient.execute(httpPost, httpResponse -> {
                HttpEntity responseEntity = httpResponse.getEntity();
                String body;
                try {
                    body = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                } catch (org.apache.hc.core5.http.ParseException e) {
                    logger.severe("Error parsing HTTP response: " + e.getMessage());
                    throw new RuntimeException("Error parsing HTTP response", e);
                }

                int status = httpResponse.getCode();
                if (status != 200) {
                    logger.severe("API request failed with status: " + status + ", response: " + body);
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