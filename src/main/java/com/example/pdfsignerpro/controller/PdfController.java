package com.example.pdfsignerpro.controller;

import com.example.pdfsignerpro.dto.*;
import com.example.pdfsignerpro.service.PdfGenerationService;
import com.example.pdfsignerpro.service.PdfMergeService;
import com.example.pdfsignerpro.service.PdfSecurityService;
import com.example.pdfsignerpro.service.PdfSigningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
public class PdfController {

    private final PdfGenerationService pdfGenerationService;
    private final PdfSigningService pdfSigningService;
    private final PdfMergeService pdfMergeService;
    private final PdfSecurityService pdfSecurityService;

    @Autowired
    public PdfController(PdfGenerationService pdfGenerationService, PdfSigningService pdfSigningService, PdfMergeService pdfMergeService, PdfSecurityService pdfSecurityService) {
        this.pdfGenerationService = pdfGenerationService;
        this.pdfSigningService = pdfSigningService;
        this.pdfMergeService = pdfMergeService;
        this.pdfSecurityService = pdfSecurityService;
    }

    /**
     * Generates a PDF from a template, optionally encrypting it.
     * Returns the PDF as a Base64 string along with the signature field location.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generatePdf(@RequestBody GenerateRequest request) {
        log.info("Received request to generate PDF from template: {}", request.getTemplateName());
        try {
            PdfGenerationResult generationResult = pdfGenerationService.generatePdfFromTemplate(
                    request,
                    "signature-area" // The ID of the element to locate
            );
            log.info("Successfully generated PDF from template: {}", request.getTemplateName());

//            if (generationResult.getSignatureFieldLocation() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature area not found in template.");
//            }

            String pdfBase64 = Base64.getEncoder().encodeToString(generationResult.getPdfBytes());

            GenerateResponse response = new GenerateResponse(pdfBase64, generationResult.getSignatureFieldLocations());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating PDF from template: {}", request.getTemplateName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/generate/file")
    public ResponseEntity<byte[]> generatePdfV2(@RequestBody GenerateRequest request) {
        log.info("Received request to generate PDF file from template: {}", request.getTemplateName());
        try {
            PdfGenerationResult generationResult = pdfGenerationService.generatePdfFromTemplate(
                    request,
                    "signature-area" // The ID of the element to locate
            );
            log.info("Successfully generated PDF file from template: {}", request.getTemplateName());

//            if (generationResult.getSignatureFieldLocation() == null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature area not found in template.");
//            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "generated-document.pdf");

            return new ResponseEntity<>(generationResult.getPdfBytes(), headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating PDF file from template: {}", request.getTemplateName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Signs a PDF document with an image and returns the signed PDF file.
     */
    @PostMapping(value = "/sign", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> signPdf(@RequestBody SignRequest request) {
        try {
            // 1. Decode the Base64 PDF and signature image
            String pdfBase64 = request.getPdfBase64();
            if (pdfBase64 != null && pdfBase64.contains(",")) {
                pdfBase64 = pdfBase64.substring(pdfBase64.indexOf(",") + 1);
            }
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
            
            String sigBase64 = request.getSignatureImageBase64();
            if (sigBase64 != null && sigBase64.contains(",")) {
                sigBase64 = sigBase64.substring(sigBase64.indexOf(",") + 1);
            }
            byte[] signatureImageBytes = Base64.getDecoder().decode(sigBase64);

            // 2. Sign the PDF
            byte[] signedPdfBytes = pdfSigningService.signPdf(
                    pdfBytes,
                    signatureImageBytes,
                    request.getSignatureFieldLocation()
            );

            // 3. Return the signed PDF file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "signed-document.pdf");

            return new ResponseEntity<>(signedPdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generates multiple PDFs, merges them, and returns the merged PDF along with all signature locations.
     */
    /**
     * Encrypts an existing PDF document.
     */
    @PostMapping(value = "/encrypt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> encryptPdf(@RequestBody EncryptRequest request) {
        try {
            String pdfBase64 = request.getPdfBase64();
            if (pdfBase64 != null && pdfBase64.contains(",")) {
                pdfBase64 = pdfBase64.substring(pdfBase64.indexOf(",") + 1);
            }
            byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);

            byte[] encryptedPdfBytes = pdfSecurityService.encryptPdf(pdfBytes, request.getPassword());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "encrypted-document.pdf");

            return new ResponseEntity<>(encryptedPdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    /**
//     * Generates multiple PDFs, merges them, and returns the merged PDF along with all signature locations.
//     */
//    @PostMapping("/generate-and-merge")
//    public ResponseEntity<?> generateAndMergePdfs(@RequestBody GenerateAndMergeRequest request) {
//        try {
//            List<byte[]> individualPdfs = new ArrayList<>();
//            List<SignatureFieldLocation> finalLocations = new ArrayList<>();
//            int pageOffset = 0;
//
//            // 1. Generate each PDF, find its signature location, and correct the page number
//            for (GenerationTask task : request.getTasks()) {
//                PdfGenerationResult result = pdfGenerationService.generatePdfFromTemplate(
//                        task.getTemplateName(),
//                        task.getData(),
//                        task.getSignatureAreaId(), // Pass the ID from the task
//                        null  // No individual encryption
//                );
//
//                byte[] currentPdfBytes = result.getPdfBytes();
//                individualPdfs.add(currentPdfBytes);
//
//                // If a signature location was found, correct its page number and add to the final list
////                if (result.getSignatureFieldLocations() != null) {
////                    SignatureFieldLocation originalLocation = result.getSignatureFieldLocation();
////                    originalLocation.setPage(originalLocation.getPage() + pageOffset);
////                    finalLocations.add(originalLocation);
////                }
//
//                // Add the number of pages of the current PDF to the offset for the next one
//                try (PDDocument document = PDDocument.load(new ByteArrayInputStream(currentPdfBytes))) {
//                    pageOffset += document.getNumberOfPages();
//                }
//            }
//
//            // 2. Merge all generated PDFs
//            byte[] mergedPdfBytes = pdfMergeService.mergePdfs(individualPdfs);
//
//            // 3. Encrypt the final merged PDF if a password is provided
//            byte[] finalPdfBytes = pdfSecurityService.encryptPdf(mergedPdfBytes, request.getPassword());
//
//            // 4. Prepare the response
//            String finalPdfBase64 = Base64.getEncoder().encodeToString(finalPdfBytes);
//            GenerateAndMergeResponse response = new GenerateAndMergeResponse(finalPdfBase64, finalLocations);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
}
