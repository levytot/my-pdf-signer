package com.example.pdfsignerpro.exception;

public enum ErrorCode {
    INVALID_REQUEST(400, "Invalid request parameters"),
    TEMPLATE_NOT_FOUND(404, "Template not found"),
    SIGNATURE_LOCATION_INVALID(400, "Invalid signature location"),
    PDF_GENERATION_FAILED(500, "PDF generation failed"),
    PDF_SIGNING_FAILED(500, "PDF signing failed"),
    PDF_MERGE_FAILED(500, "PDF merge failed"),
    PDF_ENCRYPTION_FAILED(500, "PDF encryption failed"),
    INVALID_BASE64_DATA(400, "Invalid Base64 data"),
    INTERNAL_ERROR(500, "Internal server error");

    private final int statusCode;
    private final String message;

    ErrorCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
