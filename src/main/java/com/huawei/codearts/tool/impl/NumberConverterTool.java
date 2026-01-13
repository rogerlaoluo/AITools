package com.huawei.codearts.tool.impl;

import com.huawei.codearts.tool.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Number base converter tool
 * Supports conversion between binary, octal, decimal, and hexadecimal
 */
@Component
public class NumberConverterTool implements DeveloperTool {

    @Override
    public String getId() {
        return "number-converter";
    }

    @Override
    public String getName() {
        return "进制转换工具";
    }

    @Override
    public String getDescription() {
        return "支持二进制、八进制、十进制、十六进制之间的相互转换";
    }

    @Override
    public String getCategory() {
        return "编码转换";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            String fromBase = params.get("fromBase");
            String toBase = params.get("toBase");
            String input = params.get("input");

            if (input == null || input.trim().isEmpty()) {
                return ToolResult.error("输入数字不能为空");
            }

            // Parse input based on source base
            long decimalValue;
            try {
                decimalValue = parseToDecimal(input.trim(), fromBase);
            } catch (NumberFormatException e) {
                return ToolResult.error("输入的数字格式不正确: " + e.getMessage());
            }

            // Convert to target base
            String result = convertFromDecimal(decimalValue, toBase);

            // Build result data
            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("decimalValue", decimalValue);
            data.put("fromBase", fromBase);
            data.put("toBase", toBase);

            // Also provide all base representations
            data.put("binary", convertFromDecimal(decimalValue, "bin"));
            data.put("octal", convertFromDecimal(decimalValue, "oct"));
            data.put("decimal", String.valueOf(decimalValue));
            data.put("hexadecimal", convertFromDecimal(decimalValue, "hex"));

            return ToolResult.success("转换成功", data);

        } catch (Exception e) {
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }

    private long parseToDecimal(String input, String base) {
        int radix;
        switch (base) {
            case "bin":
                radix = 2;
                break;
            case "oct":
                radix = 8;
                break;
            case "hex":
                radix = 16;
                break;
            case "dec":
                radix = 10;
                break;
            default:
                throw new IllegalArgumentException("不支持的进制: " + base);
        }
        return Long.parseLong(input, radix);
    }

    private String convertFromDecimal(long value, String base) {
        switch (base) {
            case "bin":
                return Long.toBinaryString(value);
            case "oct":
                return Long.toOctalString(value);
            case "hex":
                return Long.toHexString(value).toUpperCase();
            case "dec":
                return String.valueOf(value);
            default:
                throw new IllegalArgumentException("不支持的进制: " + base);
        }
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("fromBase", new ToolParameter(
                "fromBase",
                "string",
                "源进制: bin(二进制), oct(八进制), dec(十进制), hex(十六进制)",
                true,
                "dec"
        ));

        schema.put("toBase", new ToolParameter(
                "toBase",
                "string",
                "目标进制: bin(二进制), oct(八进制), dec(十进制), hex(十六进制)",
                true,
                "hex"
        ));

        schema.put("input", new ToolParameter(
                "input",
                "string",
                "需要转换的数字",
                true,
                null
        ));

        return schema;
    }
}
