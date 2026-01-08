package com.huawei.codearts.tool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for all developer tools in the market
 */
@Component
public class ToolRegistry {

    private final Map<String, DeveloperTool> tools = new ConcurrentHashMap<>();

    @Autowired
    public ToolRegistry(List<DeveloperTool> toolList) {
        // Auto-register all DeveloperTool beans
        for (DeveloperTool tool : toolList) {
            registerTool(tool);
        }
    }

    /**
     * Register a new tool
     */
    public void registerTool(DeveloperTool tool) {
        tools.put(tool.getId(), tool);
    }

    /**
     * Get a tool by ID
     */
    public DeveloperTool getTool(String toolId) {
        return tools.get(toolId);
    }

    /**
     * Get all registered tools
     */
    public Collection<DeveloperTool> getAllTools() {
        return tools.values();
    }

    /**
     * Get tools by category
     */
    public List<DeveloperTool> getToolsByCategory(String category) {
        return tools.values().stream()
                .filter(tool -> tool.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    /**
     * Check if a tool exists
     */
    public boolean hasTool(String toolId) {
        return tools.containsKey(toolId);
    }

    /**
     * Get all categories
     */
    public Set<String> getCategories() {
        return tools.values().stream()
                .map(DeveloperTool::getCategory)
                .collect(Collectors.toSet());
    }
}
