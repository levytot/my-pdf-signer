package com.example.pdfsignerpro.util;

import com.example.pdfsignerpro.constants.AppConstants;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

@Slf4j
public final class Base64Util {

    private Base64Util() {}

    public static byte[] decode(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            throw new PdfSignerException(ErrorCode.INVALID_BASE64_DATA, "Base64 string cannot be null or empty");
        }
        
        String cleanBase64 = cleanBase64String(base64String);
        
        try {
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 string", e);
            throw new PdfSignerException(ErrorCode.INVALID_BASE64_DATA, "Invalid Base64 format", e);
        }
    }

    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            throw new PdfSignerException(ErrorCode.INVALID_BASE64_DATA, "Data to encode cannot be null or empty");
        }
        return Base64.getEncoder().encodeToString(data);
    }

    private static String cleanBase64String(String base64String) {
        String cleaned = base64String.trim();
        
        int separatorIndex = cleaned.indexOf(AppConstants.BASE64_SEPARATOR);
        if (separatorIndex != -1) {
            cleaned = cleaned.substring(separatorIndex + 1);
        }
        
        return cleaned;
    }
}
