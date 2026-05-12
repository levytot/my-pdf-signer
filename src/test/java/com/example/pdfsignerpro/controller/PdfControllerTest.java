package com.example.pdfsignerpro.controller;

import com.example.pdfsignerpro.dto.ApiResponse;
import com.example.pdfsignerpro.dto.EncryptRequest;
import com.example.pdfsignerpro.dto.GenerateRequest;
import com.example.pdfsignerpro.dto.GenerateResponse;
import com.example.pdfsignerpro.dto.PdfGenerationResult;
import com.example.pdfsignerpro.dto.SignRequest;
import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.example.pdfsignerpro.service.PdfGenerationService;
import com.example.pdfsignerpro.service.PdfMergeService;
import com.example.pdfsignerpro.service.PdfSecurityService;
import com.example.pdfsignerpro.service.PdfSigningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PdfController.
 */
@WebMvcTest(PdfController.class)
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PdfGenerationService pdfGenerationService;

    @MockBean
    private PdfSigningService pdfSigningService;

    @MockBean
    private PdfMergeService pdfMergeService;

    @MockBean
    private PdfSecurityService pdfSecurityService;

    private byte[] testPdfBytes;
    private String testPdfBase64;
    private GenerateRequest testGenerateRequest;
    private SignRequest testSignRequest;
    private EncryptRequest testEncryptRequest;

    @BeforeEach
    void setUp() {
        // Create test PDF bytes (simplified)
        testPdfBytes = new byte[] { '%', 'P', 'D', 'F', '-' };
        testPdfBase64 = Base64.getEncoder().encodeToString(testPdfBytes);

        // Create generate request
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test User");
        testGenerateRequest = new GenerateRequest();
        testGenerateRequest.setTemplateName("test-template");
        testGenerateRequest.setData(data);

        // Create sign request
        SignatureFieldLocation location = SignatureFieldLocation.builder()
                .page(1)
                .x(100f)
                .y(100f)
                .width(200f)
                .height(50f)
                .build();
        testSignRequest = new SignRequest();
        testSignRequest.setPdfBase64(testPdfBase64);
        testSignRequest.setSignatureImageBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        testSignRequest.setSignatureFieldLocation(location);

        // Create encrypt request
        testEncryptRequest = new EncryptRequest();
        testEncryptRequest.setPdfBase64(testPdfBase64);
        testEncryptRequest.setPassword("test123");
    }

    @Test
    void generatePdf_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Mock service response
        PdfGenerationResult mockResult = PdfGenerationResult.builder()
                .pdfBytes(testPdfBytes)
                .signatureFieldLocations(Collections.emptyList())
                .build();
        when(pdfGenerationService.generatePdfFromTemplate(any(GenerateRequest.class), anyString()))
                .thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/documents/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGenerateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void generatePdf_WithMissingTemplateName_ShouldReturnBadRequest() throws Exception {
        testGenerateRequest.setTemplateName(null);

        mockMvc.perform(post("/api/v1/documents/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGenerateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generatePdfFile_WithValidRequest_ShouldReturnPdf() throws Exception {
        PdfGenerationResult mockResult = PdfGenerationResult.builder()
                .pdfBytes(testPdfBytes)
                .signatureFieldLocations(Collections.emptyList())
                .build();
        when(pdfGenerationService.generatePdfFromTemplate(any(GenerateRequest.class), anyString()))
                .thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/documents/generate/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGenerateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void signPdf_WithValidRequest_ShouldReturnSignedPdf() throws Exception {
        when(pdfSigningService.signPdf(any(byte[].class), any(byte[].class), any(SignatureFieldLocation.class)))
                .thenReturn(testPdfBytes);

        mockMvc.perform(post("/api/v1/documents/sign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSignRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void encryptPdf_WithValidRequest_ShouldReturnEncryptedPdf() throws Exception {
        when(pdfSecurityService.encryptPdf(any(byte[].class), anyString()))
                .thenReturn(testPdfBytes);

        mockMvc.perform(post("/api/v1/documents/encrypt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEncryptRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
