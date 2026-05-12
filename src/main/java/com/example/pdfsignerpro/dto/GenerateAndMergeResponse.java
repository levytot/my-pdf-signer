package com.example.pdfsignerpro.dto;

import java.util.List;

public class GenerateAndMergeResponse {

    private String mergedPdfBase64;
    private List<SignatureFieldLocation> signatureFieldLocations;

    public GenerateAndMergeResponse(String mergedPdfBase64, List<SignatureFieldLocation> signatureFieldLocations) {
        this.mergedPdfBase64 = mergedPdfBase64;
        this.signatureFieldLocations = signatureFieldLocations;
    }

    // Getters and Setters
    public String getMergedPdfBase64() {
        return mergedPdfBase64;
    }

    public void setMergedPdfBase64(String mergedPdfBase64) {
        this.mergedPdfBase64 = mergedPdfBase64;
    }

    public List<SignatureFieldLocation> getSignatureFieldLocations() {
        return signatureFieldLocations;
    }

    public void setSignatureFieldLocations(List<SignatureFieldLocation> signatureFieldLocations) {
        this.signatureFieldLocations = signatureFieldLocations;
    }
}