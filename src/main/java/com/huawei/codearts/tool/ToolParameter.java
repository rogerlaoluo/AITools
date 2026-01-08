package com.huawei.codearts.tool;

/**
 * Parameter definition for tools
 */
public class ToolParameter {

    private String name;
    private String type;
    private String description;
    private boolean required;
    private String defaultValue;

    public ToolParameter(String name, String type, String description, boolean required, String defaultValue) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
