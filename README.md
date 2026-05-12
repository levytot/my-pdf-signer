# PDF Signer Pro

PDF Signer Pro is a robust Spring Boot application designed for dynamic PDF generation, manipulation, and digital signing. It leverages Thymeleaf for flexible HTML-to-PDF templating and Apache PDFBox / OpenHTMLtoPDF for advanced PDF operations.

## 🚀 Features

*   **Dynamic PDF Generation**: Convert HTML templates (Thymeleaf) to PDF with rich, dynamic data binding.
*   **Flexible Data Structure**: Support for nested business data, dynamic HTML snippets, and loop-rendered list data.
*   **Advanced PDF Operations**:
    *   **Watermarking**: Add custom text watermarks with configurable color, opacity, and font size.
    *   **Encryption**: Secure PDFs with password protection.
    *   **Metadata**: Set PDF document metadata (Title, Author, Subject).
*   **PDF Merging**: Combine multiple generated PDFs into a single document.
*   **Digital Signing**: Sign PDFs using Base64 encoded signature images at specific coordinates or dynamically located HTML elements.

## 🛠️ Tech Stack

*   **Framework**: Java 17+, Spring Boot
*   **PDF Generation**: OpenHTMLtoPDF, Apache PDFBox
*   **Templating**: Thymeleaf

## 📚 API Documentation

### 1. Generate PDF (Base64)
`POST /api/v1/documents/generate`
Generates a PDF based on a Thymeleaf template and returns it as a Base64 encoded string along with signature field locations.

### 2. Generate PDF (File Download)
`POST /api/v1/documents/generate/file`
Generates a PDF and returns it directly as a downloadable file (`application/pdf`).

#### Request Payload (`GenerateRequest`)

The generation endpoints accept a highly dynamic JSON payload:

```json
{
  "templateName": "AFVisualForce-v1.2",
  "data": {
    "customer": {
      "fullName": "陳大文",
      "lastName": "陳",
      "firstName": "大文",
      "isPassportVerified": true,
      "passportNumber": "A1234567",
      "mobileNumber": "98765432",
      "emailAddress": "chan.taiman@example.com",
      "billingAddress": "香港九龍旺角彌敦道123號"
    },
    "contract": {
      "formId": "APP-2023-001",
      "commitmentPeriod": "24",
      "estimatedEffectiveDate": "2023-11-01",
      "estimatedInstallationDate": "2023-10-25 14:00",
      "newHomePhoneNumber": "23456789",
      "portInInstallationDate": "N/A",
      "portInEffectiveDate": "N/A",
      "socketOption": "標準插座",
      "directoryListingOption": "不列入電話簿",
      "servicePlanName": "家居電話基本計劃",
      "monthlyFeeWithinCommitment": "HK$ 68",
      "monthlyFeeAfterCommitment": "HK$ 110",
      "billingOption": "電子賬單",
      "billingEmail": "chan.taiman@example.com",
      "paymentMethod": "信用卡自動轉賬",
      "accountNumber": "1234-5678-9012-3456",
      "applicationDate": "2023-10-15"
    },
    "sales": {
      "orderCreator": "李小明",
      "salesChannel": "直銷",
      "salesmanId": "S9876",
      "salesmanName": "李小明",
      "salesmanContact": "65432109"
    }
  },
  "htmlSnippets": {
    "customerConsent": "本人確認此申請書中有關安裝費豁免安排只適用於在此申請書日期前 90 日內的任何時間申請人並非為使用此申請書所載有之固網電話號碼作為家居電話服務之已登記客戶之人士(除非此申請書另有說明)。<br/><br/>本人向貴公司(服務供應商)申請有關服務。所有電訊服務(1010/csl流動通訊服務除外)均由HKT根據於服務指南提述之有關條款提供。於本申請書提述之流動通訊服務(如有)由香港移動通訊有限公司根據於服務指南提述之有關條款提供。所有於本申請書提述之非電訊服務(如有)(Now TV及新媒體服務除外)均由HKT根據於服務指南提述之有關條款提供。所有於本申請書提述之Now TV及新媒體服務(如有)均由電訊盈科媒體有限公司根據於服務指南提述之有關條款提供。The Club由Club HKT Limited根據於服務指南提述之有關條款提供。My HKT由HKT CSP Limited根據於服務指南提述之有關條款提供。本人同意受所有服務合約的條款及條件的約束。本人同意支付各項服務的費用。本人已滿18歲及本人提供的所有資料均是最新"
  },
  "listData": {
    "optionalServices": [
       {
        "serviceName": "簡易撥號 (10)<br/>額外簡易撥號 (10)<br/>提示電話<br/>拒接停示者<br/>來電顯示<br/>電話會議<br/>電話候接 來電顯示<br/>電話候接<br/>請勿干擾<br/>遙控轉駁 密碼: 9960<br/>音樂暂候",
        "feeWithin": "HK$ 0",
        "feeAfter": "HK$ 30"
      },
      {
        "serviceName": "IDD 服務<br/>啟動 IDD 服務<br/>防擅用 IDD 服務 密碼: 9962",
        "feeWithin": "HK$ 0",
        "feeAfter": "HK$ 0"
      }
    ]
  },
  "operations": {
    "password": "",
    "watermarkText": "PCCW HKT DEMO"
  }
}
```

### 3. Sign PDF
`POST /api/v1/documents/sign`
Signs an existing PDF document using a provided signature image.

### 4. Encrypt PDF
`POST /api/v1/documents/encrypt`
Applies password protection to an existing PDF document.

### 5. Generate and Merge PDFs
`POST /api/v1/documents/generate-and-merge`
Generates multiple PDFs from different templates and data sources, then merges them into a single continuous PDF document.

## 📝 Template Design Guidelines

When designing Thymeleaf HTML templates for this system:

1.  **Data Binding**: Use standard Thymeleaf syntax (`th:text="${customer.fullName}"`) to bind data from the `data` node.
2.  **List Rendering**: Use `th:each="item : ${listData.myList}"` to iterate over arrays provided in the `listData` node.
3.  **Dynamic HTML**: Use `th:utext="${htmlSnippets.mySnippet}"` to render raw HTML strings safely.
4.  **Dynamic Images**: Bind Base64 image strings using `th:src="${images.myLogo}"`.
5.  **Signature Anchors**: Define empty `<span>` or `<div>` elements with specific IDs (e.g., `<span id="signature-area"></span>`) to act as anchors for digital signatures. The system will automatically calculate their X/Y coordinates in the generated PDF.
