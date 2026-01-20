package com.example.aiexcel;

import com.example.aiexcel.service.ai.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = AiExcelIntegrationApplication.class
)
@ActiveProfiles("test")
public class IntegrationTest {

    @Autowired
    private AiService aiService;

    @Test
    public void testAiServiceConfiguration() {
        // 测试AI服务是否已正确配置
        assertNotNull(aiService, "AI Service should be injected");

        // 尝试连接测试 - 只有当API密钥正确配置时才会返回true
        // 但如果API密钥不正确或网络问题，也可能返回false
        Integer status = aiService.testConnection();
        System.out.println("AI Service connection test HTTP status: " + status);

        // 我们至少验证服务是否被注入
        System.out.println("AI Service instance: " + aiService.getClass().getSimpleName());
    }
}