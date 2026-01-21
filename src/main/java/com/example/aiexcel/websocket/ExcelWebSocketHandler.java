package com.example.aiexcel.websocket;

import com.example.aiexcel.service.AiExcelIntegrationService;
import com.example.aiexcel.service.FileManagerService;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{clientId}")
@SuppressWarnings("unused")
public class ExcelWebSocketHandler {

    private static AiExcelIntegrationService aiExcelIntegrationService;
    private static FileManagerService fileManagerService;
    
    @Autowired
    public void setAiExcelIntegrationService(AiExcelIntegrationService service) {
        ExcelWebSocketHandler.aiExcelIntegrationService = service;
    }

    @Autowired
    public void setFileManagerService(FileManagerService service) {
        ExcelWebSocketHandler.fileManagerService = service;
    }

    // 静态变量，用来记录当前在线连接数
    private static int onlineCount = 0;
    
    // concurrent包的线程安全Set，用来存放每个客户端对应的Session对象
    private static ConcurrentHashMap<String, ExcelWebSocketHandler> webSocketSet = 
            new ConcurrentHashMap<>();
    
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    // 接收clientId
    private String clientId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) {
        this.session = session;
        this.clientId = clientId;
        webSocketSet.put(clientId, this); // 加入set中
        addOnlineCount(); // 在线数加1
        System.out.println("有新连接加入，当前在线人数为: " + getOnlineCount());
        
        // 发送连接成功消息
        try {
            sendMessage("{\"type\":\"connected\",\"message\":\"WebSocket connection established\",\"clientId\":\"" + clientId + "\"}");
        } catch (IOException e) {
            System.out.println("连接建立时发送消息失败");
        }

        // 尝试在握手参数中读取 userId 和 workspaceId，如果存在则设置对应工作区为公开
        try {
            if (fileManagerService == null) {
                System.out.println("FileManagerService 未注入，无法设置工作区公开");
            } else {
                Map<String, List<String>> params = session.getRequestParameterMap();
                if (params != null) {
                    List<String> users = params.get("userId");
                    List<String> wsIds = params.get("workspaceId");
                    if (users != null && !users.isEmpty() && wsIds != null && !wsIds.isEmpty()) {
                        String userId = users.get(0);
                        String wsIdStr = wsIds.get(0);
                        try {
                            Long wsId = Long.valueOf(wsIdStr);
                            boolean ok = fileManagerService.setWorkspacePublic(wsId, true, userId);
                            System.out.println("尝试将工作区设为公开 — workspaceId=" + wsId + ", userId=" + userId + ", 结果=" + ok);
                        } catch (NumberFormatException nfe) {
                            System.out.println("解析 workspaceId 失败: " + wsIdStr);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("设置工作区公开时发生异常: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this.clientId); // 从set中删除
        subOnlineCount(); // 在线数减1
        System.out.println("有一连接关闭，当前在线人数为: " + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息: " + message);
        
        try {
            // 解析客户端发送的消息
            ObjectMapper objectMapper = new ObjectMapper();
            WebSocketMessage request = objectMapper.readValue(message, WebSocketMessage.class);
            
            // 根据消息类型处理
            String messageType = request.getType();
            String command = request.getCommand();
            String excelData = request.getExcelData();
            
            if ("excel_command".equals(messageType)) {
                // 处理Excel命令
                handleExcelCommand(command, excelData);
            } else if ("analyze_data".equals(messageType)) {
                // 处理数据分析
                handleDataAnalysis(excelData, command);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendMessage("{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 处理Excel命令
     */
    private void handleExcelCommand(String command, String excelData) {
        try {
            // 由于我们不能直接访问上传的文件，这里我们模拟处理过程
            // 在实际实现中，您需要存储客户端上传的Excel文件
            String aiResponse = "This is a simulated response to command: " + command + 
                               ". In a real implementation, this would analyze the Excel data and return appropriate formulas or operations.";
            
            WebSocketMessage response = new WebSocketMessage();
            response.setType("ai_response");
            response.setMessage(aiResponse);
            response.setCommand(command);
            
            String jsonResponse = new ObjectMapper().writeValueAsString(response);
            sendMessage(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理数据分析
     */
    private void handleDataAnalysis(String excelData, String analysisRequest) {
        try {
            String aiResponse = "This is a simulated analysis response for request: " + analysisRequest + 
                               ". In a real implementation, this would analyze the provided Excel data and return insights.";
            
            WebSocketMessage response = new WebSocketMessage();
            response.setType("analysis_result");
            response.setMessage(aiResponse);
            response.setAnalysisRequest(analysisRequest);
            
            String jsonResponse = new ObjectMapper().writeValueAsString(response);
            sendMessage(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("clientId") String clientId) throws IOException {
        System.out.println("推送消息到客户端: " + clientId + "，推送内容: " + message);
        
        for (String id : webSocketSet.keySet()) {
            try {
                ExcelWebSocketHandler item = webSocketSet.get(id);
                // 这里可以进行条件判断，只推送消息给特定的客户端
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ExcelWebSocketHandler.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ExcelWebSocketHandler.onlineCount--;
    }
    
    // 消息类
    public static class WebSocketMessage {
        private String type;
        private String message;
        private String command;
        private String excelData;
        private String analysisRequest;
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getCommand() {
            return command;
        }
        
        public void setCommand(String command) {
            this.command = command;
        }
        
        public String getExcelData() {
            return excelData;
        }
        
        public void setExcelData(String excelData) {
            this.excelData = excelData;
        }
        
        public String getAnalysisRequest() {
            return analysisRequest;
        }
        
        public void setAnalysisRequest(String analysisRequest) {
            this.analysisRequest = analysisRequest;
        }
    }
}