package com.example.pdfsignerpro.constants;

public final class AppConstants {
    
    private AppConstants() {}
    
    public static final String SIGNATURE_AREA_ID_DEFAULT = "signature-area";
    public static final String FONT_SIMSUN = "SimSun";
    public static final String FONT_NOTO_SANS_SC = "NotoSansSC";
    public static final String TEMPLATES_PATH = "templates/";
    public static final String FONTS_PATH = "fonts/";
    public static final String SIGNATURES_PATH = "signatures/";
    
    public static final float PAGE_A4_WIDTH = 595f;
    public static final float PAGE_A4_HEIGHT = 842f;
    public static final float DEFAULT_MARGIN = 50f;
    public static final float SIGNATURE_WIDTH_DEFAULT = 200f;
    public static final float SIGNATURE_HEIGHT_DEFAULT = 50f;
    public static final float WATERMARK_FONT_SIZE = 60f;
    public static final float WATERMARK_ALPHA = 0.2f;
    public static final float FOOTER_MARGIN = 20f;
    public static final float FOOTER_FONT_SIZE = 10f;
    
    public static final int ENCRYPTION_KEY_LENGTH = 128;
    public static final int CHARS_PER_LINE_ESTIMATE = 50;
    public static final int LINE_HEIGHT_PT = 15;
    public static final int BLOCK_ELEMENT_HEIGHT_PT = 15;
    
    public static final String BASE64_DATA_PREFIX = "data:";
    public static final String BASE64_SEPARATOR = ",";
}
