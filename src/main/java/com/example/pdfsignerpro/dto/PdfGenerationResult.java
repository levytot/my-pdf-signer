package com.example.pdfsignerpro.dto;

import java.util.List;

public class PdfGenerationResult {

    private final byte[] pdfBytes;
    private final List<SignatureFieldLocation> signatureFieldLocations;

    public PdfGenerationResult(byte[] pdfBytes, List<SignatureFieldLocation> signatureFieldLocations) {
        this.pdfBytes = pdfBytes;
        this.signatureFieldLocations = signatureFieldLocations;
    }

    public byte[] getPdfBytes() {
        return pdfBytes;
    }

    public List<SignatureFieldLocation> getSignatureFieldLocations() {
        return signatureFieldLocations;
    }
}