package com.huawei.codearts.tool;

import java.util.Map;

/**
 * Interface for developer tools in the tools market
 */
public interface DeveloperTool {

    /**
     * Get the unique identifier for this tool
     */
    String getId();

    /**
     * Get the display name of this tool
     */
    String getName();

    /**
     * Get the description of this tool
     */
    String getDescription();

    /**
     * Get the category of this tool
     */
    String getCategory();

    /**
     * Execute the tool with given parameters
     *
     * @param params input parameters for the tool
     * @return execution result
     */
    ToolResult execute(Map<String, String> params);

    /**
     * Get the parameter schema for this tool
     * Describes what parameters this tool accepts
     */
    Map<String, ToolParameter> getParameterSchema();
}
