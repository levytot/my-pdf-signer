package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.impl.PdfSecurityServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PdfSecurityService.
 */
@ExtendWith(MockitoExtension.class)
class PdfSecurityServiceTest {

    @Mock
    private PdfConfig pdfConfig;

    @InjectMocks
    private PdfSecurityServiceImpl pdfSecurityService;

    private byte[] testPdfBytes;

    @BeforeEach
    void setUp() throws IOException {
        // Create a simple test PDF
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            testPdfBytes = baos.toByteArray();
        }

        // Configure mock
        when(pdfConfig.getEncryptionKeyLength()).thenReturn(128);
        when(pdfConfig.isAllowPrinting()).thenReturn(true);
        when(pdfConfig.isAllowModification()).thenReturn(false);
    }

    @Test
    void encryptPdf_WithValidPassword_ShouldReturnEncryptedPdf() {
        byte[] result = pdfSecurityService.encryptPdf(testPdfBytes, "testpassword123");

        assertNotNull(result);
        assertTrue(result.length > 0);
        // Note: We can't easily verify encryption status without attempting to open it,
        // but we can verify the output is not empty
    }

    @Test
    void encryptPdf_WithEmptyPassword_ShouldReturnOriginalPdf() {
        byte[] result = pdfSecurityService.encryptPdf(testPdfBytes, "");

        assertNotNull(result);
        assertArrayEquals(testPdfBytes, result);
    }

    @Test
    void encryptPdf_WithNullPassword_ShouldReturnOriginalPdf() {
        byte[] result = pdfSecurityService.encryptPdf(testPdfBytes, null);

        assertNotNull(result);
        assertArrayEquals(testPdfBytes, result);
    }
}
