package com.example.pdfsignerpro.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfMergeService {

    /**
     * Merges multiple PDF documents into a single document.
     *
     * @param pdfs A list of byte arrays, where each byte array is a complete PDF document.
     * @return A byte array of the merged PDF document.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] mergePdfs(List<byte[]> pdfs) throws IOException {
        if (pdfs == null || pdfs.isEmpty()) {
            return new byte[0];
        }
        if (pdfs.size() == 1) {
            return pdfs.get(0);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument mergedDocument = new PDDocument()) {
            
            // Merge each PDF into the merged document
            for (byte[] pdfBytes : pdfs) {
                try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
                     PDDocument sourceDocument = PDDocument.load(inputStream)) {
                    
                    // Import all pages from the source document
                    int numberOfPages = sourceDocument.getNumberOfPages();
                    for (int i = 0; i < numberOfPages; i++) {
                        mergedDocument.addPage(sourceDocument.getPage(i));
                    }
                }
            }
            
            // Save the merged document
            mergedDocument.save(outputStream);
            
            return outputStream.toByteArray();
        }
    }
}
