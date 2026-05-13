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
public class PdfGenerationResult {

    private byte[] pdfBytes;
    private List<SignatureFieldLocation> signatureFieldLocations;
}