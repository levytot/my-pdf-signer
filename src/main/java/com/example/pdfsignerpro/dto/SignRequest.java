package com.example.pdfsignerpro.dto;

public class SignRequest {

    private String pdfBase64;
    private String signatureImageBase64;
    private SignatureFieldLocation signatureFieldLocation;

    // Getters and Setters
    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }

    public String getSignatureImageBase64() {
        return signatureImageBase64;
    }

    public void setSignatureImageBase64(String signatureImageBase64) {
        this.signatureImageBase64 = signatureImageBase64;
    }

    public SignatureFieldLocation getSignatureFieldLocation() {
        return signatureFieldLocation;
    }

    public void setSignatureFieldLocation(SignatureFieldLocation signatureFieldLocation) {
        this.signatureFieldLocation = signatureFieldLocation;
    }
}