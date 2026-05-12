package com.example.pdfsignerpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GenerateRequest {

    @NotBlank(message = "Template name is required")
    private String templateName;

    private Map<String, Object> data;

    private Map<String, List<Map<String, Object>>> listData;

    private Map<String, String> htmlSnippets;

    private Map<String, Object> config;

    private PdfOperations operations;

    @Data
    public static class PdfOperations {
        private String password;
        private String watermark;
    }
}
