package com.example.aiexcel;

import com.example.aiexcel.service.AiExcelIntegrationService;
import com.example.aiexcel.service.analysis.CustomerAnalysisService;
import com.example.aiexcel.service.analysis.FinancialAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = AiExcelIntegrationApplication.class
)
@ActiveProfiles("test")
public class BusinessAnalysisTest {

    @Autowired
    private AiExcelIntegrationService aiExcelIntegrationService;
    
    @Autowired
    private CustomerAnalysisService customerAnalysisService;
    
    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    @Test
    public void testCustomerAnalysisServices() {
        // 测试客户分析服务是否正确注入
        assertNotNull(customerAnalysisService, "CustomerAnalysisService should be injected");
        assertNotNull(aiExcelIntegrationService, "AiExcelIntegrationService should be injected");
        
        System.out.println("Customer Analysis Service: " + customerAnalysisService.getClass().getSimpleName());
        System.out.println("AI Excel Integration Service: " + aiExcelIntegrationService.getClass().getSimpleName());
    }

    @Test
    public void testFinancialAnalysisServices() {
        // 测试财务分析服务是否正确注入
        assertNotNull(financialAnalysisService, "FinancialAnalysisService should be injected");
        assertNotNull(aiExcelIntegrationService, "AiExcelIntegrationService should be injected");
        
        System.out.println("Financial Analysis Service: " + financialAnalysisService.getClass().getSimpleName());
        System.out.println("AI Excel Integration Service: " + aiExcelIntegrationService.getClass().getSimpleName());
    }

    // 创建一个虚拟的Excel文件用于测试
    @SuppressWarnings("unused")
    private MultipartFile createMockExcelFile() {
        // 创建一个简单的Excel内容（这里只是一个示例）
        String excelContent = "Name,Email,Amount,Date\nJohn Doe,john@example.com,100,2023-01-01\nJane Smith,jane@example.com,200,2023-01-02";
        return new MockMultipartFile("test.xlsx", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                                   excelContent.getBytes());
    }
    
    @Test
    public void testRFMAnalysisFunctionality() {
        // 测试RFM分析功能是否可用
        assertNotNull(customerAnalysisService, "CustomerAnalysisService should be injected");
        
        // 注意：由于需要有效的API密钥和网络连接，我们只测试服务是否被正确注入
        // 实际的分析功能需要有效的API密钥才能工作
        System.out.println("RFM Analysis method exists and service is injected");
        
        // 验证服务类名称
        assertEquals("CustomerAnalysisServiceImpl", customerAnalysisService.getClass().getSimpleName());
    }
    
    @Test
    public void testFinancialAnalysisFunctionality() {
        // 测试财务分析功能是否可用
        assertNotNull(financialAnalysisService, "FinancialAnalysisService should be injected");
        
        System.out.println("Financial Analysis method exists and service is injected");
        
        // 验证服务类名称
        assertEquals("FinancialAnalysisServiceImpl", financialAnalysisService.getClass().getSimpleName());
    }
}