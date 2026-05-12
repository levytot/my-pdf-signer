package com.example.pdfsignerpro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "pdf.config")
public class PdfConfig {
    
    private float signatureWidth = 200f;
    private float signatureHeight = 50f;
    private String defaultSignatureAreaId = "signature-area";
    private boolean useFastMode = true;
    private int encryptionKeyLength = 128;
    private boolean allowPrinting = true;
    private boolean allowModification = false;
}
