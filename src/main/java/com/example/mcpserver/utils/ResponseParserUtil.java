package com.example.mcpserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Utility class for parsing API responses and returning clean JSON
 */
@Slf4j
public class ResponseParserUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse response body and return data field as Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseAndReturnCleanJson(String responseBody) {
        try {
            // Parse once and convert directly to Map to avoid multiple parsing
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            if (responseMap.containsKey("data")) {
                Object dataValue = responseMap.get("data");
                
                // If data is already a Map, return it directly
                if (dataValue instanceof Map) {
                    return (Map<String, Object>) dataValue;
                }
                // If data is a simple value, wrap it in a map
                else {
                    return Map.of("result", dataValue);
                }
            }
            
            return responseMap;
        } catch (Exception e) {
            log.warn("Failed to parse response JSON: {}", e.getMessage());
            return Map.of("error", responseBody);
        }
    }
    
    /**
     * Parse error response and extract meaningful error message
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseErrorResponse(String errorResponseBody, String defaultMessage) {
        try {
            // Try to parse the error response
            Map<String, Object> errorMap = objectMapper.readValue(errorResponseBody, Map.class);
            
            // Look for error message in various common fields
            String errorMessage = extractErrorMessage(errorMap);
            
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                return Map.of("error", errorMessage);
            }
            
            // If no error message found, log with traceId for investigation
            String traceId = generateTraceId();
            log.error("Unexpected error response format - TraceId: {}, Response: {}", traceId, errorResponseBody);
            return Map.of("error", String.format("Unexpected error occurred. Contact support with trace ID: %s", traceId));
            
        } catch (Exception e) {
            String traceId = generateTraceId();
            log.error("Failed to parse error response - TraceId: {}, Error: {}, Response: {}", traceId, e.getMessage(), errorResponseBody);
            return Map.of("error", String.format("Unexpected error occurred. Contact support with trace ID: %s", traceId));
        }
    }
    
    /**
     * Generate a unique trace ID for error tracking
     */
    private static String generateTraceId() {
        return String.format("TR-%d-%04d", System.currentTimeMillis(), (int)(Math.random() * 10000));
    }
    
    /**
     * Parse error response specifically for customer tools that use error.message format
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseCustomerErrorResponse(String errorResponseBody, String defaultMessage) {
        try {
            // Try to parse the error response
            Map<String, Object> errorMap = objectMapper.readValue(errorResponseBody, Map.class);
            
            // Look for error.message format (customer tool specific)
            String errorMessage = extractCustomerErrorMessage(errorMap);
            
            if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                return Map.of("error", errorMessage);
            }
            
            // If no error.message found, return default with traceId
            String traceId = generateTraceId();
            log.error("Customer error response format not recognized - TraceId: {}, Response: {}", traceId, errorResponseBody);
            return Map.of("error", String.format("Unexpected error occurred. Contact support with trace ID: %s", traceId));
            
        } catch (Exception e) {
            String traceId = generateTraceId();
            log.error("Failed to parse customer error response - TraceId: {}, Error: {}, Response: {}", traceId, e.getMessage(), errorResponseBody);
            return Map.of("error", String.format("Unexpected error occurred. Contact support with trace ID: %s", traceId));
        }
    }
    
    /**
     * Extract error message specifically from customer tool error format
     */
    @SuppressWarnings("unchecked")
    private static String extractCustomerErrorMessage(Map<String, Object> errorMap) {
        // Check for nested error.message (customer tool format)
        if (errorMap.containsKey("error")) {
            Object errorValue = errorMap.get("error");
            if (errorValue instanceof Map) {
                Map<String, Object> nestedError = (Map<String, Object>) errorValue;
                if (nestedError.containsKey("message")) {
                    Object messageValue = nestedError.get("message");
                    if (messageValue instanceof String) {
                        return (String) messageValue;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract error message from various possible error response formats
     */
    @SuppressWarnings("unchecked")
    private static String extractErrorMessage(Map<String, Object> errorMap) {
        // Try common error message fields, with responseMessage first for VA responses
        String[] messageFields = {"responseMessage", "message", "errorMessage", "error_message", "msg", "description"};
        
        for (String field : messageFields) {
            if (errorMap.containsKey(field)) {
                Object messageValue = errorMap.get(field);
                if (messageValue instanceof String) {
                    return (String) messageValue;
                }
            }
        }
        
        // Check for nested error object
        if (errorMap.containsKey("error")) {
            Object errorValue = errorMap.get("error");
            if (errorValue instanceof Map) {
                Map<String, Object> nestedError = (Map<String, Object>) errorValue;
                for (String field : messageFields) {
                    if (nestedError.containsKey(field)) {
                        Object messageValue = nestedError.get(field);
                        if (messageValue instanceof String) {
                            return (String) messageValue;
                        }
                    }
                }
            } else if (errorValue instanceof String) {
                return (String) errorValue;
            }
        }
        
        return null;
    }
}