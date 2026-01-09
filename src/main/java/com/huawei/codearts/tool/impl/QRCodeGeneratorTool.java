package com.huawei.codearts.tool.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.huawei.codearts.tool.DeveloperTool;
import com.huawei.codearts.tool.ToolParameter;
import com.huawei.codearts.tool.ToolResult;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 二维码生成器工具
 * 支持生成不同尺寸的二维码图片
 */
@Component
public class QRCodeGeneratorTool implements DeveloperTool {

    @Override
    public String getId() {
        return "qrcode-generator";
    }

    @Override
    public String getName() {
        return "二维码生成器";
    }

    @Override
    public String getDescription() {
        return "快速生成二维码图片，支持自定义尺寸和纠错级别";
    }

    @Override
    public String getCategory() {
        return "图像生成";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            String text = params.get("text");
            String sizeStr = params.get("size");
            String errorCorrection = params.get("errorCorrection");

            if (text == null || text.trim().isEmpty()) {
                return ToolResult.error("请输入需要生成二维码的文本内容");
            }

            // 解析参数
            int size = 300;
            try {
                if (sizeStr != null && !sizeStr.isEmpty()) {
                    size = Integer.parseInt(sizeStr);
                    // 限制尺寸范围
                    size = Math.max(100, Math.min(1000, size));
                }
            } catch (NumberFormatException e) {
                size = 300;
            }

            // 设置纠错级别
            Map<EncodeHintType, Object> hints = new HashMap<>();
            if (errorCorrection != null) {
                switch (errorCorrection.toUpperCase()) {
                    case "L":
                        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L);
                        break;
                    case "M":
                        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
                        break;
                    case "Q":
                        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q);
                        break;
                    case "H":
                        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
                        break;
                    default:
                        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
                }
            } else {
                hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
            }

            // 设置字符编码
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            // 生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

            // 转换为图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // 转换为 Base64
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> data = new HashMap<>();
            data.put("result", "data:image/png;base64," + base64Image);
            data.put("size", size);
            data.put("errorCorrection", errorCorrection != null ? errorCorrection.toUpperCase() : "M");
            data.put("textLength", text.length());

            return ToolResult.success("二维码生成成功", data);
        } catch (Exception e) {
            return ToolResult.error("生成二维码失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("text", new ToolParameter(
                "text",
                "string",
                "需要生成二维码的文本内容或URL",
                true,
                null
        ));

        schema.put("size", new ToolParameter(
                "size",
                "number",
                "二维码图片尺寸 (100-1000像素)",
                false,
                "300"
        ));

        schema.put("errorCorrection", new ToolParameter(
                "errorCorrection",
                "string",
                "纠错级别: L(7%), M(15%), Q(25%), H(30%)",
                false,
                "M"
        ));

        return schema;
    }
}
