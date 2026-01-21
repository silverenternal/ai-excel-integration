package com.example.aiexcel.service.ai;

import com.example.aiexcel.dto.AiRequest;
import com.example.aiexcel.dto.AiResponse;

public interface AiService {
    AiResponse generateResponse(AiRequest request);
    AiResponse generateStreamingResponse(AiRequest request);
    /**
     * Test connection to AI provider.
     * @return HTTP status code returned by a lightweight test request (e.g. 200, 401), or null if no request was made or an error occurred.
     */
    Integer testConnection();
}