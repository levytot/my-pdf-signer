package com.example.pdfsignerpro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignRequest {

    @NotNull(message = "PDF Base64 data is required")
    private String pdfBase64;

    @NotNull(message = "Signature image Base64 data is required")
    private String signatureImageBase64;

    @NotNull(message = "Signature field location is required")
    private SignatureFieldLocation signatureFieldLocation;
}