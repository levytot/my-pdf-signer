package com.example.pdfsignerpro.service.impl;

import com.example.pdfsignerpro.config.PdfConfig;
import com.example.pdfsignerpro.constants.AppConstants;
import com.example.pdfsignerpro.dto.GenerateRequest;
import com.example.pdfsignerpro.dto.PdfGenerationResult;
import com.example.pdfsignerpro.dto.SignatureFieldLocation;
import com.example.pdfsignerpro.exception.ErrorCode;
import com.example.pdfsignerpro.exception.PdfSignerException;
import com.example.pdfsignerpro.service.PdfGenerationService;
import com.example.pdfsignerpro.service.PdfSecurityService;
import com.example.pdfsignerpro.util.StringUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PDF generation service.
 * Handles template processing, PDF rendering, watermarking, page numbering, and signature location detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final TemplateEngine templateEngine;
    private final PdfSecurityService pdfSecurityService;
    private final PdfConfig pdfConfig;

    /**
     * {@inheritDoc}
     */
    @Override
    public PdfGenerationResult generatePdfFromTemplate(GenerateRequest request, String elementIdToLocate) {
        log.debug("Starting PDF generation for template: {}", request.getTemplateName());
        
        try {
            Context context = buildContext(request);
            String htmlContent = processTemplate(request.getTemplateName(), context);
            String marker = "SIGN_MARKER_" + System.currentTimeMillis();
            htmlContent = injectMarker(htmlContent, elementIdToLocate, marker);
            
            byte[] pdfBytes = renderPdf(htmlContent);
            SignatureFieldLocation fieldLocation = findSignatureLocation(pdfBytes, marker, htmlContent, elementIdToLocate);
            pdfBytes = addPageNumbers(pdfBytes);
            
            String watermark = request.getOperations() != null ? request.getOperations().getWatermark() : null;
            if (StringUtil.isNotEmpty(watermark)) {
                pdfBytes = addWatermark(pdfBytes, watermark);
            }
            
            String password = request.getOperations() != null ? request.getOperations().getPassword() : null;
            pdfBytes = pdfSecurityService.encryptPdf(pdfBytes, password);
            
            List<SignatureFieldLocation> locations = new ArrayList<>();
            if (fieldLocation != null) {
                locations.add(fieldLocation);
            }
            
            return PdfGenerationResult.builder()
                    .pdfBytes(pdfBytes)
                    .signatureFieldLocations(locations)
                    .build();
                    
        } catch (IOException e) {
            log.error("PDF generation failed for template: {}", request.getTemplateName(), e);
            throw new PdfSignerException(ErrorCode.PDF_GENERATION_FAILED, e);
        }
    }

    /**
     * Builds the Thymeleaf context from the request data.
     *
     * @param request The generate request containing template data
     * @return The populated Thymeleaf context
     */
    private Context buildContext(GenerateRequest request) {
        Context context = new Context();
        if (request.getData() != null) {
            request.getData().forEach(context::setVariable);
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
        return context;
    }

    /**
     * Processes the Thymeleaf template with the given context.
     *
     * @param templateName The name of the template to process
     * @param context The context containing template variables
     * @return The processed HTML content
     */
    private String processTemplate(String templateName, Context context) {
        try {
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            log.error("Template processing failed: {}", templateName, e);
            throw new PdfSignerException(ErrorCode.TEMPLATE_NOT_FOUND, 
                    "Failed to process template: " + templateName, e);
        }
    }

    /**
     * Injects an invisible marker into HTML content for signature location detection.
     *
     * @param htmlContent The original HTML content
     * @param elementIdToLocate The element ID where the marker should be injected
     * @param marker The marker text to inject
     * @return The modified HTML content with marker
     */
    private String injectMarker(String htmlContent, String elementIdToLocate, String marker) {
        if (StringUtil.isEmpty(elementIdToLocate)) {
            return htmlContent;
        }
        
        String idAttr1 = "id=\"" + elementIdToLocate + "\"";
        String idAttr2 = "id='" + elementIdToLocate + "'";
        int idx = htmlContent.indexOf(idAttr1);
        if (idx == -1) {
            idx = htmlContent.indexOf(idAttr2);
        }
        
        if (idx != -1) {
            int closeBracket = htmlContent.indexOf(">", idx);
            if (closeBracket != -1) {
                return htmlContent.substring(0, closeBracket + 1) +
                        "<span style=\"color:transparent;font-size:1px;\">" + marker + "</span>" +
                        htmlContent.substring(closeBracket + 1);
            }
        }
        return htmlContent;
    }

    /**
     * Renders HTML content to PDF using OpenHTMLtoPDF.
     *
     * @param htmlContent The HTML content to render
     * @return The PDF as byte array
     * @throws IOException If rendering fails
     */
    private byte[] renderPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            if (pdfConfig.isUseFastMode()) {
                builder.useFastMode();
            }
            builder.withHtmlContent(htmlContent, null);
            loadChineseFonts(builder);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        }
    }

    /**
     * Finds the signature location by first trying exact marker detection,
     * then falling back to heuristic estimation if needed.
     *
     * @param pdfBytes The PDF to search
     * @param marker The marker text to find
     * @param htmlContent The original HTML for fallback estimation
     * @param elementIdToLocate The element ID for fallback
     * @return The signature location, or null if not found
     * @throws IOException If PDF processing fails
     */
    private SignatureFieldLocation findSignatureLocation(byte[] pdfBytes, String marker, 
                                                          String htmlContent, String elementIdToLocate) throws IOException {
        if (StringUtil.isEmpty(elementIdToLocate)) {
            return null;
        }
        
        SignatureFieldLocation fieldLocation = findExactLocation(pdfBytes, marker);
        if (fieldLocation == null) {
            fieldLocation = estimateElementLocation(htmlContent, elementIdToLocate);
        }
        return fieldLocation;
    }

    /**
     * Adds a watermark text to all pages of the PDF.
     *
     * @param pdfBytes The PDF to watermark
     * @param watermarkText The watermark text
     * @return The watermarked PDF
     * @throws IOException If watermarking fails
     */
    private byte[] addWatermark(byte[] pdfBytes, String watermarkText) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDType1Font font = PDType1Font.HELVETICA_BOLD;
            float fontSize = AppConstants.WATERMARK_FONT_SIZE;

            for (PDPage page : document.getPages()) {
                PDRectangle mediaBox = page.getMediaBox();
                float width = mediaBox.getWidth();
                float height = mediaBox.getHeight();
                float textWidth = font.getStringWidth(watermarkText) / 1000 * fontSize;
                float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                float x = (width - textWidth) / 2;
                float y = (height - textHeight) / 2;

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, 
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                    gs.setNonStrokingAlphaConstant(AppConstants.WATERMARK_ALPHA);
                    contentStream.setGraphicsStateParameters(gs);
                    contentStream.setNonStrokingColor(200, 200, 200);
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    
                    org.apache.pdfbox.util.Matrix matrix = org.apache.pdfbox.util.Matrix.getRotateInstance(
                            Math.toRadians(45), x + textWidth / 2, y + textHeight / 2);
                    matrix.translate(-textWidth / 2, -textHeight / 2);
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
     * Adds page numbers to the footer of each page in "X of Y" format.
     *
     * @param pdfBytes The PDF to add page numbers to
     * @return The PDF with page numbers
     * @throws IOException If page numbering fails
     */
    private byte[] addPageNumbers(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int numberOfPages = document.getNumberOfPages();
            for (int i = 0; i < numberOfPages; i++) {
                PDPage page = document.getPage(i);
                PDRectangle mediaBox = page.getMediaBox();
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, AppConstants.FOOTER_FONT_SIZE);
                    String text = (i + 1) + " of " + numberOfPages;
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * AppConstants.FOOTER_FONT_SIZE;
                    float x = (mediaBox.getWidth() - textWidth) / 2;
                    float y = AppConstants.FOOTER_MARGIN;
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
     * Finds the exact location of the injected marker in the PDF.
     *
     * @param pdfBytes The PDF to search
     * @param marker The marker text to find
     * @return The exact signature location, or null if not found
     * @throws IOException If PDF processing fails
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
                                float x = pos.getXDirAdj();
                                float y = pageHeight - pos.getYDirAdj();
                                loc[0] = SignatureFieldLocation.builder()
                                        .page(currentPage)
                                        .x(x)
                                        .y(y)
                                        .width(pdfConfig.getSignatureWidth())
                                        .height(pdfConfig.getSignatureHeight())
                                        .build();
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
     * Loads Chinese fonts (SimSun) for PDF rendering.
     *
     * @param builder The PDF renderer builder
     * @throws IOException If font loading fails
     */
    private void loadChineseFonts(PdfRendererBuilder builder) throws IOException {
        ClassPathResource fontResource = new ClassPathResource(AppConstants.FONTS_PATH + "simsun.ttc");
        if (fontResource.exists()) {
            File fontFile = fontResource.getFile();
            builder.useFont(fontFile, AppConstants.FONT_SIMSUN);
        } else {
            File fontFile = new File("src/main/resources/" + AppConstants.FONTS_PATH + "simsun.ttc");
            if (fontFile.exists()) {
                builder.useFont(fontFile, AppConstants.FONT_SIMSUN);
            }
        }
    }

    /**
     * Estimates the location of an HTML element using heuristic methods.
     * Used as fallback when exact marker detection fails.
     *
     * @param htmlContent The HTML content
     * @param elementId The element ID to locate
     * @return The estimated signature location, or null if not found
     */
    private SignatureFieldLocation estimateElementLocation(String htmlContent, String elementId) {
        int elementIndex = htmlContent.indexOf("id=\"" + elementId + "\"");
        if (elementIndex == -1) {
            elementIndex = htmlContent.indexOf("id='" + elementId + "'");
        }

        if (elementIndex != -1) {
            String contentBefore = htmlContent.substring(0, elementIndex);

            int blockElements = StringUtil.countOccurrences(contentBefore, "<div")
                    + StringUtil.countOccurrences(contentBefore, "<p>")
                    + StringUtil.countOccurrences(contentBefore, "<h1")
                    + StringUtil.countOccurrences(contentBefore, "<h2")
                    + StringUtil.countOccurrences(contentBefore, "<h3")
                    + StringUtil.countOccurrences(contentBefore, "<table")
                    + StringUtil.countOccurrences(contentBefore, "<tr")
                    + StringUtil.countOccurrences(contentBefore, "<br")
                    + StringUtil.countOccurrences(contentBefore, "<li");

            String textContent = contentBefore.replaceAll("<[^>]*>", "");
            int charCount = textContent.length();

            float usableHeight = AppConstants.PAGE_A4_HEIGHT - (2 * AppConstants.DEFAULT_MARGIN);
            float estimatedTextHeight = (charCount / (float) AppConstants.CHARS_PER_LINE_ESTIMATE) * AppConstants.LINE_HEIGHT_PT;
            float estimatedBlockHeight = blockElements * AppConstants.BLOCK_ELEMENT_HEIGHT_PT;
            float totalContentHeight = estimatedTextHeight + estimatedBlockHeight;
            float yOnPage = usableHeight - (totalContentHeight % usableHeight);
            int pageNum = (int) (totalContentHeight / usableHeight) + 1;

            return SignatureFieldLocation.builder()
                    .page(Math.max(1, pageNum))
                    .x(AppConstants.DEFAULT_MARGIN)
                    .y(Math.max(AppConstants.DEFAULT_MARGIN, yOnPage))
                    .width(pdfConfig.getSignatureWidth())
                    .height(pdfConfig.getSignatureHeight())
                    .build();
        }

        return null;
    }
}
