package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.dto.GenerateRequest;
import com.example.pdfsignerpro.dto.PdfGenerationResult;

/**
 * Service interface for PDF generation from templates.
 */
public interface PdfGenerationService {
    /**
     * Generates a PDF document from a template with the given data.
     *
     * @param request The request containing template name and data
     * @param elementIdToLocate The HTML element ID to locate for signature placement
     * @return PdfGenerationResult containing the generated PDF bytes and signature locations
     */
    PdfGenerationResult generatePdfFromTemplate(GenerateRequest request, String elementIdToLocate);
}
