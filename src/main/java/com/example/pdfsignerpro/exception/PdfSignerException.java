package com.example.pdfsignerpro.exception;

public class PdfSignerException extends RuntimeException {
    private final ErrorCode errorCode;

    public PdfSignerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PdfSignerException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PdfSignerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public PdfSignerException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
