package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.impl.PdfMergeServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PdfMergeService.
 */
@ExtendWith(MockitoExtension.class)
class PdfMergeServiceTest {

    @InjectMocks
    private PdfMergeServiceImpl pdfMergeService;

    private byte[] testPdf1;
    private byte[] testPdf2;

    @BeforeEach
    void setUp() throws IOException {
        // Create first test PDF
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            testPdf1 = baos.toByteArray();
        }

        // Create second test PDF with multiple pages
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            testPdf2 = baos.toByteArray();
        }
    }

    @Test
    void mergePdfs_WithMultiplePdfs_ShouldReturnMergedPdf() {
        List<byte[]> pdfs = new ArrayList<>();
        pdfs.add(testPdf1);
        pdfs.add(testPdf2);

        byte[] result = pdfMergeService.mergePdfs(pdfs);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void mergePdfs_WithSinglePdf_ShouldReturnSamePdf() {
        List<byte[]> pdfs = new ArrayList<>();
        pdfs.add(testPdf1);

        byte[] result = pdfMergeService.mergePdfs(pdfs);

        assertNotNull(result);
        assertArrayEquals(testPdf1, result);
    }

    @Test
    void mergePdfs_WithEmptyList_ShouldReturnEmptyArray() {
        List<byte[]> pdfs = new ArrayList<>();

        byte[] result = pdfMergeService.mergePdfs(pdfs);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void mergePdfs_WithNullList_ShouldReturnEmptyArray() {
        byte[] result = pdfMergeService.mergePdfs(null);

        assertNotNull(result);
        assertEquals(0, result.length);
    }
}
