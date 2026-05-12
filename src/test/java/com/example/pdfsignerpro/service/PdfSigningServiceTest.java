package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.impl.PdfSigningServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PdfSigningService.
 */
@ExtendWith(MockitoExtension.class)
class PdfSigningServiceTest {

    @InjectMocks
    private PdfSigningServiceImpl pdfSigningService;

    private byte[] testPdfBytes;
    private byte[] testImageBytes;
    private SignatureFieldLocation validLocation;

    @BeforeEach
    void setUp() throws IOException {
        // Create a simple test PDF
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            testPdfBytes = baos.toByteArray();
        }

        // Create dummy image bytes (PNG header)
        testImageBytes = new byte[] {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };

        // Create valid signature location
        validLocation = SignatureFieldLocation.builder()
                .page(1)
                .x(100f)
                .y(100f)
                .width(200f)
                .height(50f)
                .build();
    }

    @Test
    void signPdf_WithValidInputs_ShouldReturnSignedPdf() {
        byte[] result = pdfSigningService.signPdf(testPdfBytes, testImageBytes, validLocation);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void signPdf_WithNullLocation_ShouldThrowException() {
        PdfSignerException exception = assertThrows(PdfSignerException.class, () ->
            pdfSigningService.signPdf(testPdfBytes, testImageBytes, null)
        );

        assertEquals(ErrorCode.SIGNATURE_LOCATION_INVALID, exception.getErrorCode());
    }

    @Test
    void signPdf_WithInvalidPageNumber_ShouldThrowException() {
        SignatureFieldLocation invalidPageLocation = SignatureFieldLocation.builder()
                .page(999)
                .x(100f)
                .y(100f)
                .width(200f)
                .height(50f)
                .build();

        PdfSignerException exception = assertThrows(PdfSignerException.class, () ->
            pdfSigningService.signPdf(testPdfBytes, testImageBytes, invalidPageLocation)
        );

        assertEquals(ErrorCode.SIGNATURE_LOCATION_INVALID, exception.getErrorCode());
    }

    @Test
    void signPdf_WithZeroPageNumber_ShouldThrowException() {
        SignatureFieldLocation zeroPageLocation = SignatureFieldLocation.builder()
                .page(0)
                .x(100f)
                .y(100f)
                .width(200f)
                .height(50f)
                .build();

        PdfSignerException exception = assertThrows(PdfSignerException.class, () ->
            pdfSigningService.signPdf(testPdfBytes, testImageBytes, zeroPageLocation)
        );

        assertEquals(ErrorCode.SIGNATURE_LOCATION_INVALID, exception.getErrorCode());
    }
}
