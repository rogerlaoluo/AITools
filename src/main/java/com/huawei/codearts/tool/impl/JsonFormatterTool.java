package com.huawei.codearts.tool.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.huawei.codearts.tool.DeveloperTool;
import com.huawei.codearts.tool.ToolParameter;
import com.huawei.codearts.tool.ToolResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON 格式化工具
 * 支持格式化、压缩、验证 JSON
 */
@Component
public class JsonFormatterTool implements DeveloperTool {

    private final ObjectMapper objectMapper;

    public JsonFormatterTool() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String getId() {
        return "json-formatter";
    }

    @Override
    public String getName() {
        return "JSON 格式化工具";
    }

    @Override
    public String getDescription() {
        return "支持 JSON 格式化美化、压缩和语法验证";
    }

    @Override
    public String getCategory() {
        return "数据处理";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            String operation = params.get("operation");
            String input = params.get("input");

            if (input == null || input.trim().isEmpty()) {
                return ToolResult.error("请输入需要处理的 JSON 内容");
            }

            switch (operation) {
                case "format":
                    return formatJson(input);
                case "compress":
                    return compressJson(input);
                case "validate":
                    return validateJson(input);
                default:
                    return ToolResult.error("不支持的操作: " + operation);
            }
        } catch (Exception e) {
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 格式化 JSON
     */
    private ToolResult formatJson(String input) {
        try {
            // 解析 JSON 以验证格式
            Object jsonObject = objectMapper.readValue(input, Object.class);

            // 格式化输出
            String formatted = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);

            Map<String, Object> data = new HashMap<>();
            data.put("result", formatted);
            data.put("operation", "format");
            data.put("valid", true);

            return ToolResult.success("格式化成功", data);
        } catch (JsonProcessingException e) {
            return ToolResult.error("JSON 格式错误: " + e.getMessage());
        }
    }

    /**
     * 压缩 JSON（去除空格和换行）
     */
    private ToolResult compressJson(String input) {
        try {
            // 解析 JSON 以验证格式
            Object jsonObject = objectMapper.readValue(input, Object.class);

            // 压缩输出
            ObjectMapper compactMapper = new ObjectMapper();
            String compressed = compactMapper.writeValueAsString(jsonObject);

            Map<String, Object> data = new HashMap<>();
            data.put("result", compressed);
            data.put("operation", "compress");
            data.put("valid", true);
            data.put("originalLength", input.length());
            data.put("compressedLength", compressed.length());

            return ToolResult.success("压缩成功", data);
        } catch (JsonProcessingException e) {
            return ToolResult.error("JSON 格式错误: " + e.getMessage());
        }
    }

    /**
     * 验证 JSON
     */
    private ToolResult validateJson(String input) {
        try {
            // 尝试解析 JSON
            Object jsonObject = objectMapper.readValue(input, Object.class);

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("message", "JSON 格式正确");
            data.put("type", jsonObject instanceof Map ? "对象" : jsonObject instanceof java.util.List ? "数组" : "值");

            return ToolResult.success("验证通过", data);
        } catch (JsonProcessingException e) {
            Map<String, Object> data = new HashMap<>();
            data.put("valid", false);
            data.put("error", e.getMessage());

            return new ToolResult(false, "JSON 格式错误: " + e.getMessage(), data);
        }
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("operation", new ToolParameter(
                "operation",
                "string",
                "操作类型: format(格式化), compress(压缩), validate(验证)",
                true,
                "format"
        ));

        schema.put("input", new ToolParameter(
                "input",
                "string",
                "需要处理的 JSON 内容",
                true,
                null
        ));

        return schema;
    }
}
