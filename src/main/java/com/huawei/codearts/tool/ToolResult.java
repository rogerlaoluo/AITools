package com.huawei.codearts.tool;

/**
 * Result of tool execution
 */
public class ToolResult {

    private boolean success;
    private String message;
    private Object data;
    private String error;

    public ToolResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ToolResult(boolean success, String message, Object data, String error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data, null);
    }

    public static ToolResult error(String error) {
        return new ToolResult(false, "Execution failed", null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
