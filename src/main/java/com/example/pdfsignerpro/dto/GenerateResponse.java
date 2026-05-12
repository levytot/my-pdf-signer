package com.example.pdfsignerpro.dto;

import java.util.List;

public class GenerateResponse {

    private String pdfBase64;
    private List<SignatureFieldLocation> signatureFieldLocations;

    public GenerateResponse(String pdfBase64, List<SignatureFieldLocation> signatureFieldLocations) {
        this.pdfBase64 = pdfBase64;
        this.signatureFieldLocations = signatureFieldLocations;
    }

    // Getters and Setters
    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }

    public List<SignatureFieldLocation> getSignatureFieldLocations() {
        return signatureFieldLocations;
    }

    public void setSignatureFieldLocations(List<SignatureFieldLocation> signatureFieldLocations) {
        this.signatureFieldLocations = signatureFieldLocations;
    }
}