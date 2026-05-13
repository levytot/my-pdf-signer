package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.dto.GenerateRequest;
import com.example.pdfsignerpro.dto.PdfGenerationResult;
import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.impl.PdfGenerationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PdfGenerationService.
 */
@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private PdfSecurityService pdfSecurityService;

    @Mock
    private PdfConfig pdfConfig;

    @InjectMocks
    private PdfGenerationServiceImpl pdfGenerationService;

    private GenerateRequest testRequest;

    @BeforeEach
    void setUp() {
        // Create test request
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test User");

        testRequest = new GenerateRequest();
        testRequest.setTemplateName("test-template");
        testRequest.setData(data);

        // Configure mock behavior
        when(pdfConfig.getDefaultSignatureAreaId()).thenReturn("signature-area");
        when(pdfConfig.getSignatureWidth()).thenReturn(200f);
        when(pdfConfig.getSignatureHeight()).thenReturn(50f);
        when(pdfConfig.isUseFastMode()).thenReturn(true);
    }

    @Test
    void generatePdfFromTemplate_WithValidTemplate_ShouldReturnResult() {
        // Mock successful template processing
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("""
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Test PDF</h1>
                <div id="signature-area"></div>
            </body>
            </html>
        """);

        // Mock encryption to return input as-is
        when(pdfSecurityService.encryptPdf(any(byte[].class), any())).thenAnswer(inv -> inv.getArgument(0));

        PdfGenerationResult result = pdfGenerationService.generatePdfFromTemplate(testRequest, "signature-area");

        assertNotNull(result);
        assertNotNull(result.getPdfBytes());
        assertTrue(result.getPdfBytes().length > 0);
    }

    @Test
    void generatePdfFromTemplate_WithoutSignatureElement_ShouldStillGeneratePdf() {
        // Mock template without signature area
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("""
            <!DOCTYPE html>
            <html>
            <body>
                <h1>Test PDF without signature</h1>
            </body>
            </html>
        """);

        when(pdfSecurityService.encryptPdf(any(byte[].class), any())).thenAnswer(inv -> inv.getArgument(0));

        PdfGenerationResult result = pdfGenerationService.generatePdfFromTemplate(testRequest, "signature-area");

        assertNotNull(result);
        assertNotNull(result.getPdfBytes());
        assertTrue(result.getPdfBytes().length > 0);
        // Signature location may be null when element not found
        List<SignatureFieldLocation> locations = result.getSignatureFieldLocations();
        assertNotNull(locations);
    }

    @Test
    void generatePdfFromTemplate_WithTemplateProcessingError_ShouldThrowException() {
        // Mock template processing failure
        when(templateEngine.process(anyString(), any(Context.class)))
            .thenThrow(new RuntimeException("Template not found"));

        PdfSignerException exception = assertThrows(PdfSignerException.class, () ->
            pdfGenerationService.generatePdfFromTemplate(testRequest, "signature-area")
        );

        assertEquals(ErrorCode.TEMPLATE_NOT_FOUND, exception.getErrorCode());
    }
}
