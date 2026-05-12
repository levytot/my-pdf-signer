package com.example.pdfsignerpro.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GenerateRequest {

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 1. 核心业务数据节点 (支持多层级嵌套)
     * 使用 Object 支持更灵活的嵌套结构，例如 customer.address.city
     */
    private Map<String, Object> data;

    /**
     * 2. 循环渲染的动态列表数据节点
     * 优化为 List<Map<String, Object>>，在 Thymeleaf 中可以直接通过 item.fieldName 访问，更加直观和通用
     * 例如: "orderItems": [ {"name": "商品A", "price": 100}, {"name": "商品B", "price": 200} ]
     */
    private Map<String, List<Map<String, Object>>> listData;

    /**
     * 3. 动态HTML段落拼接节点
     * 用于传递直接渲染的HTML字符串
     */
    private Map<String, String> htmlSnippets;

    /**
     * 5. 模板动态配置节点
     * 用于控制模板的显示/隐藏逻辑、动态样式(颜色、字体大小)等
     */
    private Map<String, Object> config;

    /**
     * 6. PDF 文档操作 (可选)
     */
    private PdfOperations operations;

    @Data
    public class PdfOperations {
        private String password;
        private String watermark;
    }
}
