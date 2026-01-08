package com.huawei.codearts.controller;

import com.huawei.codearts.tool.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Developer Tools Market API Controller
 */
@RestController
@RequestMapping("/api/tools")
public class ToolMarketController {

    @Autowired
    private ToolRegistry toolRegistry;

    /**
     * Get all available tools
     */
    @GetMapping
    public Map<String, Object> getAllTools() {
        List<Map<String, Object>> tools = toolRegistry.getAllTools().stream()
                .map(this::toolToMap)
                .collect(Collectors.toList());

        return Map.of(
                "success", true,
                "data", tools,
                "total", tools.size()
        );
    }

    /**
     * Get tool by ID
     */
    @GetMapping("/{toolId}")
    public Map<String, Object> getTool(@PathVariable String toolId) {
        DeveloperTool tool = toolRegistry.getTool(toolId);

        if (tool == null) {
            return Map.of(
                    "success", false,
                    "error", "Tool not found: " + toolId
            );
        }

        return Map.of(
                "success", true,
                "data", toolToMap(tool)
        );
    }

    /**
     * Get tool categories
     */
    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        return Map.of(
                "success", true,
                "data", toolRegistry.getCategories()
        );
    }

    /**
     * Execute a tool
     */
    @PostMapping("/{toolId}/execute")
    public Map<String, Object> executeTool(
            @PathVariable String toolId,
            @RequestBody Map<String, String> params) {

        DeveloperTool tool = toolRegistry.getTool(toolId);

        if (tool == null) {
            return Map.of(
                    "success", false,
                    "error", "Tool not found: " + toolId
            );
        }

        ToolResult result = tool.execute(params);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        if (result.getData() != null) {
            response.put("data", result.getData());
        }

        if (result.getError() != null) {
            response.put("error", result.getError());
        }

        return response;
    }

    /**
     * Get tool parameter schema
     */
    @GetMapping("/{toolId}/schema")
    public Map<String, Object> getToolSchema(@PathVariable String toolId) {
        DeveloperTool tool = toolRegistry.getTool(toolId);

        if (tool == null) {
            return Map.of(
                    "success", false,
                    "error", "Tool not found: " + toolId
            );
        }

        return Map.of(
                "success", true,
                "data", tool.getParameterSchema()
        );
    }

    private Map<String, Object> toolToMap(DeveloperTool tool) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tool.getId());
        map.put("name", tool.getName());
        map.put("description", tool.getDescription());
        map.put("category", tool.getCategory());
        map.put("parameters", tool.getParameterSchema());
        return map;
    }
}
