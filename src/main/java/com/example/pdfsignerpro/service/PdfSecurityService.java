package com.example.pdfsignerpro.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfSecurityService {

    /**
     * Encrypts a PDF document with a user-provided password.
     *
     * @param pdfBytes The byte array of the PDF to encrypt.
     * @param password The password to be used for encryption.
     * @return A byte array of the encrypted PDF.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encryptPdf(byte[] pdfBytes, String password) throws IOException {
        if (password == null || password.isEmpty()) {
            return pdfBytes; // If no password, return original bytes
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            
            // Set encryption permissions
            AccessPermission accessPermission = new AccessPermission();
            accessPermission.setCanPrint(true);
            accessPermission.setCanPrintDegraded(true);
            accessPermission.setCanModify(false);
            accessPermission.setCanAssembleDocument(false);
            accessPermission.setCanFillInForm(false);
            accessPermission.setCanExtractContent(false);
            accessPermission.setCanExtractForAccessibility(true);
            
            // Create protection policy with owner and user password
            StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(
                password, // Owner password
                password, // User password
                accessPermission
            );
            
            // Use 128-bit encryption
            protectionPolicy.setEncryptionKeyLength(128);
            
            // Apply encryption
            document.protect(protectionPolicy);
            
            // Save the encrypted document
            document.save(outputStream);
            
            return outputStream.toByteArray();
        }
    }
}
