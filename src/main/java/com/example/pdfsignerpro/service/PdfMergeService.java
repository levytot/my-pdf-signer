package com.example.pdfsignerpro.service;

import java.util.List;

/**
 * Service interface for merging multiple PDF documents.
 */
public interface PdfMergeService {
    /**
     * Merges multiple PDF documents into a single document.
     *
     * @param pdfs List of PDF documents as byte arrays
     * @return The merged PDF document as byte array
     */
    byte[] mergePdfs(List<byte[]> pdfs);
}
