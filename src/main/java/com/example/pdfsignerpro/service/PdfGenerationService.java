package com.example.pdfsignerpro.service;

import com.example.pdfsignerpro.dto.PdfGenerationResult;
import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfGenerationService {

    private final TemplateEngine templateEngine;
    private final PdfSecurityService pdfSecurityService;

    @Autowired
    public PdfGenerationService(TemplateEngine templateEngine, PdfSecurityService pdfSecurityService) {
        this.templateEngine = templateEngine;
        this.pdfSecurityService = pdfSecurityService;
    }

    /**
     * Generates a PDF from an HTML template and locates a specific element.
     *
     * @param templateName       the name of the HTML template (without .html extension)
     * @param data               the data to be injected into the template
     * @param elementIdToLocate  the ID of the element to locate for signature area
     * @param password           the password to encrypt the PDF. Can be null or empty for no encryption
     * @return a {@link PdfGenerationResult} containing the PDF bytes and the marker's location
     * @throws IOException if an error occurs during I/O operations
     */
    public PdfGenerationResult generatePdfFromTemplate(com.example.pdfsignerpro.dto.GenerateRequest request, String elementIdToLocate) throws IOException {
        log.debug("Starting PDF generation for template: {}", request.getTemplateName());
        Context context = new Context();
        
        if (request.getData() != null) {
            for (Map.Entry<String, Object> entry : request.getData().entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }
        if (request.getHtmlSnippets() != null) {
            context.setVariable("htmlSnippets", request.getHtmlSnippets());
        }
        if (request.getListData() != null) {
            context.setVariable("listData", request.getListData());
        }
        if (request.getConfig() != null) {
            context.setVariable("config", request.getConfig());
        }

        String templateName = request.getTemplateName();
        String password = request.getOperations() != null ? request.getOperations().getPassword() : null;
        String watermarkText = (request.getOperations() != null && request.getOperations().getWatermark() != null) ? request.getOperations().getWatermark() : null;

        log.debug("Processing Thymeleaf template: {}", templateName);
        String htmlContent = templateEngine.process(templateName, context);

        SignatureFieldLocation fieldLocation = null;
        String marker = "SIGN_MARKER_" + System.currentTimeMillis();

        // Inject a hidden marker into the HTML to find the exact location later
        if (elementIdToLocate != null && !elementIdToLocate.trim().isEmpty()) {
            String idAttr1 = "id=\"" + elementIdToLocate + "\"";
            String idAttr2 = "id='" + elementIdToLocate + "'";
            int idx = htmlContent.indexOf(idAttr1);
            if (idx == -1) idx = htmlContent.indexOf(idAttr2);
            
            if (idx != -1) {
                int closeBracket = htmlContent.indexOf(">", idx);
                if (closeBracket != -1) {
                    htmlContent = htmlContent.substring(0, closeBracket + 1) +
                                  "<span style=\"color:transparent;font-size:1px;\">" + marker + "</span>" +
                                  htmlContent.substring(closeBracket + 1);
                }
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            // Configure font resolver with Chinese font support
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            
            // Load Chinese fonts from classpath
            loadChineseFonts(builder);
            
            builder.toStream(outputStream);
            
            // Build the PDF
            log.debug("Running PDF renderer builder");
            builder.run();
            
            // Get the generated PDF bytes
            byte[] unencryptedPdfBytes = outputStream.toByteArray();
            log.debug("PDF generated successfully, size: {} bytes", unencryptedPdfBytes.length);

            // Find exact location if marker was injected
            if (elementIdToLocate != null && !elementIdToLocate.trim().isEmpty()) {
                fieldLocation = findExactLocation(unencryptedPdfBytes, marker);
                // Fallback to heuristic if marker not found
                if (fieldLocation == null) {
                    fieldLocation = estimateElementLocation(htmlContent, elementIdToLocate);
                }
            }

            // Add page numbers to the footer
            unencryptedPdfBytes = addPageNumbers(unencryptedPdfBytes);

            // Add watermark if provided
            if (watermarkText != null && !watermarkText.trim().isEmpty()) {
                unencryptedPdfBytes = addWatermark(unencryptedPdfBytes, watermarkText);
            }

            // Encrypt the PDF if a password is provided
            byte[] finalPdfBytes = pdfSecurityService.encryptPdf(unencryptedPdfBytes, password);
            if (password != null && !password.isEmpty()) {
                log.debug("PDF encrypted successfully");
            }

            List<SignatureFieldLocation> signatureFieldLocations = new ArrayList<>();
            if (null != fieldLocation) {
                signatureFieldLocations.add(fieldLocation);
            }

            return new PdfGenerationResult(finalPdfBytes, signatureFieldLocations);
        }
    }

    /**
     * Adds a custom watermark text to each page of the PDF.
     */
    private byte[] addWatermark(byte[] pdfBytes, String watermarkText) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Use a standard font for watermark
            PDType1Font font = PDType1Font.HELVETICA_BOLD;
            float fontSize = 60;
            
            for (PDPage page : document.getPages()) {
                PDRectangle mediaBox = page.getMediaBox();
                float width = mediaBox.getWidth();
                float height = mediaBox.getHeight();
                
                // Calculate text width and height to center it
                float textWidth = font.getStringWidth(watermarkText) / 1000 * fontSize;
                float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                
                // Calculate center position
                float x = (width - textWidth) / 2;
                float y = (height - textHeight) / 2;
                
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    // Set transparency (alpha) for watermark
                    org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState gs = new org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState();
                    gs.setNonStrokingAlphaConstant(0.2f);
                    contentStream.setGraphicsStateParameters(gs);
                    
                    // Set color to light gray
                    contentStream.setNonStrokingColor(200, 200, 200);
                    
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    
                    // Rotate text 45 degrees around the center
                    org.apache.pdfbox.util.Matrix matrix = org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(45), x + textWidth/2, y + textHeight/2);
                    matrix.translate(-textWidth/2, -textHeight/2);
                    contentStream.setTextMatrix(matrix);
                    
                    contentStream.showText(watermarkText);
                    contentStream.endText();
                }
            }
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Adds centered page numbers to the footer of each page.
     */
    private byte[] addPageNumbers(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int numberOfPages = document.getNumberOfPages();
            for (int i = 0; i < numberOfPages; i++) {
                PDPage page = document.getPage(i);
                PDRectangle mediaBox = page.getMediaBox();
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    String text = (i + 1) + " of " + numberOfPages;
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * 10;
                    float x = (mediaBox.getWidth() - textWidth) / 2;
                    float y = 20; // Footer position
                    contentStream.newLineAtOffset(x, y);
                    contentStream.showText(text);
                    contentStream.endText();
                }
            }
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Finds the exact location of the injected marker in the generated PDF.
     */
    private SignatureFieldLocation findExactLocation(byte[] pdfBytes, String marker) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            final SignatureFieldLocation[] loc = new SignatureFieldLocation[1];
            
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                final int currentPage = page;
                final float pageHeight = document.getPage(page - 1).getMediaBox().getHeight();
                
                PDFTextStripper stripper = new PDFTextStripper() {
                    @Override
                    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                        if (text.contains(marker) && loc[0] == null) {
                            int index = text.indexOf(marker);
                            if (index >= 0 && index < textPositions.size()) {
                                TextPosition pos = textPositions.get(index);
                                // Convert top-down Y to bottom-up Y for signature placement
                                float x = pos.getXDirAdj();
                                float y = pageHeight - pos.getYDirAdj();
                                loc[0] = new SignatureFieldLocation(currentPage, x, y, 200f, 50f);
                            }
                        }
                        super.writeString(text, textPositions);
                    }
                };
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                stripper.getText(document);
                if (loc[0] != null) {
                    break;
                }
            }
            return loc[0];
        }
    }

    /**
     * Load Chinese fonts for PDF generation.
     */
    private void loadChineseFonts(PdfRendererBuilder builder) throws IOException {
        // Try to load SimSun font from classpath
        ClassPathResource fontResource = new ClassPathResource("fonts/simsun.ttc");
        if (fontResource.exists()) {
            File fontFile = fontResource.getFile();
            
            // Use the simpler useFont method which is available on PdfRendererBuilder
            // This method registers the font for use in the PDF
            builder.useFont(fontFile, "SimSun");
        } else {
            // Fallback: try to load from file system relative to working directory
            File fontFile = new File("src/main/resources/fonts/simsun.ttc");
            if (fontFile.exists()) {
                builder.useFont(fontFile, "SimSun");
            }
        }
    }

    /**
     * Estimate the location of an element in the rendered HTML.
     * OpenHTMLtoPDF doesn't expose rendering internals like Flying Saucer,
     * so we estimate position based on HTML structure analysis.
     */
    private SignatureFieldLocation estimateElementLocation(String htmlContent, String elementId) {
        // Try to find the element by id attribute
        int elementIndex = htmlContent.indexOf("id=\"" + elementId + "\"");
        if (elementIndex == -1) {
            elementIndex = htmlContent.indexOf("id='" + elementId + "'");
        }
        
        if (elementIndex != -1) {
            // Analyze content before the element to estimate position
            String contentBefore = htmlContent.substring(0, elementIndex);
            
            // Count block-level elements to estimate vertical position
            int blockElements = countOccurrences(contentBefore, "<div")
                              + countOccurrences(contentBefore, "<p>")
                              + countOccurrences(contentBefore, "<h1")
                              + countOccurrences(contentBefore, "<h2")
                              + countOccurrences(contentBefore, "<h3")
                              + countOccurrences(contentBefore, "<table")
                              + countOccurrences(contentBefore, "<tr")
                              + countOccurrences(contentBefore, "<br")
                              + countOccurrences(contentBefore, "<li");
            
            // Strip HTML tags to get raw text length
            String textContent = contentBefore.replaceAll("<[^>]*>", "");
            int charCount = textContent.length();
            
            // A4 page dimensions in points: 595 x 842
            // Standard margins: ~50pt
            float topMargin = 50f;
            float leftMargin = 50f;
            float pageHeight = 842f - (2 * topMargin); // Usable height
            
            // Heuristic estimation:
            // Assume ~50 characters per line, 15pt per line
            float estimatedTextHeight = (charCount / 50f) * 15f;
            // Assume each block element adds some vertical space (margins/padding)
            float estimatedBlockHeight = blockElements * 15f;
            
            // Estimate Y position (PDF uses bottom-left origin)
            float totalContentHeight = estimatedTextHeight + estimatedBlockHeight;
            float yOnPage = pageHeight - (totalContentHeight % pageHeight);
            
            // Estimate page number
            int pageNum = (int) (totalContentHeight / pageHeight) + 1;
            
            // Default width and height for signature area
            float width = 200f;
            float height = 50f;
            
            return new SignatureFieldLocation(
                Math.max(1, pageNum),
                leftMargin,
                Math.max(topMargin, yOnPage),
                width,
                height
            );
        }
        
        return null;
    }

    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 1;
        for (char c : text.toCharArray()) {
            if (c == '\n' || c == '\r') {
                count++;
            }
        }
        return count;
    }

    private int countOccurrences(String text, String pattern) {
        if (text == null || text.isEmpty() || pattern == null || pattern.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
