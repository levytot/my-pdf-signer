package com.example.pdfsignerpro.service.impl;

import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.PdfSigningService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of PDF signing service.
 * Places signature images onto PDF documents at specified locations.
 */
@Slf4j
@Service
public class PdfSigningServiceImpl implements PdfSigningService {

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] signPdf(byte[] originalPdfBytes, byte[] signatureImageBytes, SignatureFieldLocation location) {
        if (location == null) {
            throw new PdfSignerException(ErrorCode.SIGNATURE_LOCATION_INVALID, "Signature location cannot be null");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = PDDocument.load(new ByteArrayInputStream(originalPdfBytes))) {

            int pageIndex = location.getPage() - 1;
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new PdfSignerException(ErrorCode.SIGNATURE_LOCATION_INVALID,
                        "Invalid page number: " + location.getPage());
            }

            PDPage page = document.getPage(pageIndex);
            PDRectangle pageSize = page.getMediaBox();
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, signatureImageBytes, "signature");

            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float x = location.getX();
            float y = location.getY();
            float width = location.getWidth();
            float height = location.getHeight();

            // Adjust signature size to fit within page boundaries if needed
            if (x + width > pageWidth) {
                width = pageWidth - x;
            }
            if (y + height > pageHeight) {
                height = pageHeight - y;
            }

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.drawImage(pdImage, x, y, width, height);
            }

            document.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("PDF signing failed", e);
            throw new PdfSignerException(ErrorCode.PDF_SIGNING_FAILED, e);
        }
    }
}
