package com.huawei.codearts.tool.impl;

import com.huawei.codearts.tool.*;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base64 encoder/decoder tool
 */
@Component
public class Base64Tool implements DeveloperTool {

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public String getName() {
        return "Base64 编解码工具";
    }

    @Override
    public String getDescription() {
        return "支持Base64编码和解码，支持文本和URL安全编码";
    }

    @Override
    public String getCategory() {
        return "编码转换";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            String operation = params.get("operation");
            String input = params.get("input");
            String variant = params.getOrDefault("variant", "standard");

            if (input == null || input.isEmpty()) {
                return ToolResult.error("输入内容不能为空");
            }

            String result;
            switch (operation) {
                case "encode":
                    result = encode(input, variant);
                    break;
                case "decode":
                    result = decode(input, variant);
                    break;
                default:
                    return ToolResult.error("不支持的操作: " + operation);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("operation", operation);
            data.put("variant", variant);

            return ToolResult.success("操作成功", data);

        } catch (IllegalArgumentException e) {
            return ToolResult.error("解码失败: " + e.getMessage());
        } catch (Exception e) {
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }

    private String encode(String input, String variant) {
        switch (variant) {
            case "url":
                return Base64.getUrlEncoder().encodeToString(input.getBytes());
            case "mime":
                return Base64.getMimeEncoder().encodeToString(input.getBytes());
            default:
                return Base64.getEncoder().encodeToString(input.getBytes());
        }
    }

    private String decode(String input, String variant) {
        byte[] decoded;
        switch (variant) {
            case "url":
                decoded = Base64.getUrlDecoder().decode(input);
                break;
            case "mime":
                decoded = Base64.getMimeDecoder().decode(input);
                break;
            default:
                decoded = Base64.getDecoder().decode(input);
        }
        return new String(decoded);
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("operation", new ToolParameter(
                "operation",
                "string",
                "操作类型: encode(编码) 或 decode(解码)",
                true,
                "encode"
        ));

        schema.put("input", new ToolParameter(
                "input",
                "string",
                "需要处理的文本内容",
                true,
                null
        ));

        schema.put("variant", new ToolParameter(
                "variant",
                "string",
                "编码变体: standard(标准), url(URL安全), mime(MIME)",
                false,
                "standard"
        ));

        return schema;
    }
}
