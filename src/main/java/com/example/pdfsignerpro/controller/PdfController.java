package com.example.pdfsignerpro.controller;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.constants.AppConstants;
import com.example.pdfsignerpro.dto.*;
import com.example.pdfsignerpro.service.PdfGenerationService;
import com.example.pdfsignerpro.service.PdfMergeService;
import com.example.pdfsignerpro.service.PdfSecurityService;
import com.example.pdfsignerpro.service.PdfSigningService;
import com.example.pdfsignerpro.util.Base64Util;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class PdfController {

    private final PdfGenerationService pdfGenerationService;
    private final PdfSigningService pdfSigningService;
    private final PdfMergeService pdfMergeService;
    private final PdfSecurityService pdfSecurityService;
    private final PdfConfig pdfConfig;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateResponse>> generatePdf(@Valid @RequestBody GenerateRequest request) {
        log.info("Received request to generate PDF from template: {}", request.getTemplateName());

        PdfGenerationResult generationResult = pdfGenerationService.generatePdfFromTemplate(
                request,
                pdfConfig.getDefaultSignatureAreaId()
        );
        log.info("Successfully generated PDF from template: {}", request.getTemplateName());

        String pdfBase64 = Base64Util.encode(generationResult.getPdfBytes());
        GenerateResponse response = new GenerateResponse(pdfBase64, generationResult.getSignatureFieldLocations());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generate/file")
    public ResponseEntity<byte[]> generatePdfFile(@Valid @RequestBody GenerateRequest request) {
        log.info("Received request to generate PDF file from template: {}", request.getTemplateName());

        PdfGenerationResult generationResult = pdfGenerationService.generatePdfFromTemplate(
                request,
                pdfConfig.getDefaultSignatureAreaId()
        );
        log.info("Successfully generated PDF file from template: {}", request.getTemplateName());

        return createPdfResponse(generationResult.getPdfBytes(), "generated-document.pdf");
    }

    @PostMapping(value = "/sign", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> signPdf(@Valid @RequestBody SignRequest request) {
        log.info("Received request to sign PDF");

        byte[] pdfBytes = Base64Util.decode(request.getPdfBase64());
        byte[] signatureImageBytes = Base64Util.decode(request.getSignatureImageBase64());
        byte[] signedPdfBytes = pdfSigningService.signPdf(
                pdfBytes,
                signatureImageBytes,
                request.getSignatureFieldLocation()
        );

        log.info("Successfully signed PDF");
        return createPdfResponse(signedPdfBytes, "signed-document.pdf");
    }

    @PostMapping(value = "/encrypt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> encryptPdf(@Valid @RequestBody EncryptRequest request) {
        log.info("Received request to encrypt PDF");

        byte[] pdfBytes = Base64Util.decode(request.getPdfBase64());
        byte[] encryptedPdfBytes = pdfSecurityService.encryptPdf(pdfBytes, request.getPassword());

        log.info("Successfully encrypted PDF");
        return createPdfResponse(encryptedPdfBytes, "encrypted-document.pdf");
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfBytes, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
