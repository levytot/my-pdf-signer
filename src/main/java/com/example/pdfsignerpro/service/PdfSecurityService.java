package com.example.pdfsignerpro.service;

/**
 * Service interface for PDF security operations like encryption.
 */
public interface PdfSecurityService {
    /**
     * Encrypts a PDF document with the given password.
     *
     * @param pdfBytes The PDF document to encrypt
     * @param password The password to use for encryption
     * @return The encrypted PDF document
     */
    byte[] encryptPdf(byte[] pdfBytes, String password);
}
