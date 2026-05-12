package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.dto.SignatureFieldLocation;

/**
 * Service interface for PDF signing operations.
 */
public interface PdfSigningService {
    /**
     * Signs a PDF document by placing a signature image at the specified location.
     *
     * @param originalPdfBytes The original PDF document as byte array
     * @param signatureImageBytes The signature image as byte array
     * @param location The location where the signature should be placed
     * @return The signed PDF document as byte array
     */
    byte[] signPdf(byte[] originalPdfBytes, byte[] signatureImageBytes, SignatureFieldLocation location);
}
