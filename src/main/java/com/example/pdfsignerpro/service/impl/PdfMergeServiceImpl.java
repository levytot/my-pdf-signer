package com.example.pdfsignerpro.service.impl;

import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.PdfMergeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Implementation of PDF merge service.
 * Combines multiple PDF documents into a single document.
 */
@Slf4j
@Service
public class PdfMergeServiceImpl implements PdfMergeService {

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] mergePdfs(List<byte[]> pdfs) {
        if (pdfs == null || pdfs.isEmpty()) {
            return new byte[0];
        }
        if (pdfs.size() == 1) {
            return pdfs.get(0);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument mergedDocument = new PDDocument()) {

            for (byte[] pdfBytes : pdfs) {
                try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
                     PDDocument sourceDocument = PDDocument.load(inputStream)) {

                    int numberOfPages = sourceDocument.getNumberOfPages();
                    for (int i = 0; i < numberOfPages; i++) {
                        mergedDocument.addPage(sourceDocument.getPage(i));
                    }
                }
            }

            mergedDocument.save(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("PDF merge failed", e);
            throw new PdfSignerException(ErrorCode.PDF_MERGE_FAILED, e);
        }
    }
}
