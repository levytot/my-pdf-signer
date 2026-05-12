package com.example.pdfsignerpro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EncryptRequest {
    @NotBlank(message = "PDF Base64 data is required")
    private String pdfBase64;
    
    private String password;
}
