package com.example.pdfsignerpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateResponse {
    private String pdfBase64;
    private List<SignatureFieldLocation> signatureFieldLocations;
}