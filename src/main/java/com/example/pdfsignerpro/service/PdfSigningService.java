package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfSigningService {

    /**
     * Stamps a signature image onto a PDF document at a specified location, preserving encryption.
     *
     * @param originalPdfBytes    The byte array of the PDF to be signed.
     * @param signatureImageBytes The byte array of the signature image (e.g., PNG).
     * @param location            The location where the signature should be placed.
     * @param password            The password for the PDF. Can be null or empty if not encrypted.
     * @return A byte array of the newly signed PDF.
     * @throws IOException       if an I/O error occurs.
     */
    public byte[] signPdf(byte[] originalPdfBytes, byte[] signatureImageBytes, SignatureFieldLocation location)
            throws IOException {

        if (location == null) {
            throw new IllegalArgumentException("Signature location cannot be null.");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = PDDocument.load(new ByteArrayInputStream(originalPdfBytes))) {
            
            // Get the page (PDFBox uses 0-based indexing)
            int pageIndex = location.getPage() - 1;
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new IllegalArgumentException("Invalid page number: " + location.getPage());
            }
            
            PDPage page = document.getPage(pageIndex);
            PDRectangle pageSize = page.getMediaBox();
            
            // Create image from signature bytes
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, signatureImageBytes, "signature");
            
            // Get page dimensions
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            
            // PDFBox uses bottom-left origin, same as the location coordinates
            float x = location.getX();
            float y = location.getY();
            float width = location.getWidth();
            float height = location.getHeight();
            
            // Ensure the image fits within the page bounds
            if (x + width > pageWidth) {
                width = pageWidth - x;
            }
            if (y + height > pageHeight) {
                height = pageHeight - y;
            }
            
            // Draw the image onto the page using content stream
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.drawImage(pdImage, x, y, width, height);
            }
            
            // Save the document
            document.save(outputStream);
            
            return outputStream.toByteArray();
        }
    }
    
}
