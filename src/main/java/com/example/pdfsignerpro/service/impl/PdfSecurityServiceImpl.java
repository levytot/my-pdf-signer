package com.example.pdfsignerpro.service.impl;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.PdfSecurityService;
import com.example.pdfsignerpro.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of PDF security service.
 * Handles PDF encryption with configurable permissions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfSecurityServiceImpl implements PdfSecurityService {

    private final PdfConfig pdfConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encryptPdf(byte[] pdfBytes, String password) {
        if (StringUtil.isEmpty(password)) {
            return pdfBytes;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {

            AccessPermission accessPermission = new AccessPermission();
            accessPermission.setCanPrint(pdfConfig.isAllowPrinting());
            accessPermission.setCanPrintDegraded(pdfConfig.isAllowPrinting());
            accessPermission.setCanModify(pdfConfig.isAllowModification());
            accessPermission.setCanAssembleDocument(pdfConfig.isAllowModification());
            accessPermission.setCanFillInForm(pdfConfig.isAllowModification());
            accessPermission.setCanExtractContent(pdfConfig.isAllowModification());
            accessPermission.setCanExtractForAccessibility(true);

            StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(
                    password, password, accessPermission);
            protectionPolicy.setEncryptionKeyLength(pdfConfig.getEncryptionKeyLength());

            document.protect(protectionPolicy);
            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("PDF encryption failed", e);
            throw new PdfSignerException(ErrorCode.PDF_ENCRYPTION_FAILED, e);
        }
    }
}
