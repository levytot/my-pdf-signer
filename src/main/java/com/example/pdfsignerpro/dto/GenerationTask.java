package com.example.pdfsignerpro.dto;

import java.util.Map;

public class GenerationTask {

    private String templateName;
    private Map<String, Object> data;
    private String signatureAreaId; // Optional: The ID of the signature element to locate

    // Getters and Setters
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getSignatureAreaId() {
        return signatureAreaId;
    }

    public void setSignatureAreaId(String signatureAreaId) {
        this.signatureAreaId = signatureAreaId;
    }
}