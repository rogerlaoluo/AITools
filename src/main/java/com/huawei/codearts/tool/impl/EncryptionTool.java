package com.huawei.codearts.tool.impl;

import com.huawei.codearts.tool.DeveloperTool;
import com.huawei.codearts.tool.ToolParameter;
import com.huawei.codearts.tool.ToolResult;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * 在线加密解密工具
 * 支持多种加密算法：AES、DES、RSA、MD5、SHA等
 */
@Component
public class EncryptionTool implements DeveloperTool {

    @Override
    public String getId() {
        return "encryption-tool";
    }

    @Override
    public String getName() {
        return "在线加密解密";
    }

    @Override
    public String getDescription() {
        return "支持 AES、DES、MD5、SHA 等多种加密解密算法";
    }

    @Override
    public String getCategory() {
        return "编码转换";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            String algorithm = params.getOrDefault("algorithm", "AES");
            String operation = params.getOrDefault("operation", "encrypt");
            String input = params.get("input");
            String key = params.get("key");

            if (input == null || input.trim().isEmpty()) {
                return ToolResult.error("请输入需要处理的内容");
            }

            // 对于解密操作，检查密钥
            if ("decrypt".equals(operation) && ("AES".equals(algorithm) || "DES".equals(algorithm))) {
                if (key == null || key.trim().isEmpty()) {
                    return ToolResult.error("请输入解密密钥");
                }
            }

            Map<String, Object> data = new HashMap<>();

            switch (algorithm) {
                case "AES":
                    data = processAES(operation, input, key);
                    break;
                case "DES":
                    data = processDES(operation, input, key);
                    break;
                case "MD5":
                    data = processMD5(input);
                    break;
                case "SHA-256":
                    data = processSHA256(input);
                    break;
                case "SHA-512":
                    data = processSHA512(input);
                    break;
                default:
                    return ToolResult.error("不支持的算法: " + algorithm);
            }

            data.put("algorithm", algorithm);
            data.put("operation", operation);

            return ToolResult.success("操作成功", data);
        } catch (Exception e) {
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * AES 加密解密
     */
    private Map<String, Object> processAES(String operation, String input, String keyStr) throws Exception {
        if ("encrypt".equals(operation)) {
            // 加密
            if (keyStr == null || keyStr.isEmpty()) {
                keyStr = generateRandomKey(16);
            }

            // 确保 key 是 16/24/32 字节
            keyStr = padKey(keyStr, 16);

            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(encrypted);

            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("key", keyStr);
            data.put("info", "使用密钥: " + keyStr);
            return data;

        } else {
            // 解密
            keyStr = padKey(keyStr, 16);

            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(input));
            String result = new String(decrypted, StandardCharsets.UTF_8);

            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("info", "解密成功");
            return data;
        }
    }

    /**
     * DES 加密解密
     */
    private Map<String, Object> processDES(String operation, String input, String keyStr) throws Exception {
        if ("encrypt".equals(operation)) {
            if (keyStr == null || keyStr.isEmpty()) {
                keyStr = generateRandomKey(8);
            }

            // DES 密钥必须是 8 字节
            keyStr = padKey(keyStr, 8);

            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(encrypted);

            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("key", keyStr);
            data.put("info", "使用密钥: " + keyStr);
            return data;

        } else {
            keyStr = padKey(keyStr, 8);

            SecretKeySpec key = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(input));
            String result = new String(decrypted, StandardCharsets.UTF_8);

            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            data.put("info", "解密成功");
            return data;
        }
    }

    /**
     * MD5 哈希
     */
    private Map<String, Object> processMD5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String result = toHex(digest);

        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("info", "MD5 哈希值（不可逆）");
        return data;
    }

    /**
     * SHA-256 哈希
     */
    private Map<String, Object> processSHA256(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String result = toHex(digest);

        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("info", "SHA-256 哈希值（不可逆）");
        return data;
    }

    /**
     * SHA-512 哈希
     */
    private Map<String, Object> processSHA512(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String result = toHex(digest);

        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("info", "SHA-512 哈希值（不可逆）");
        return data;
    }

    /**
     * 生成随机密钥
     */
    private String generateRandomKey(int length) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[length];
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key).substring(0, length);
    }

    /**
     * 填充密钥到指定长度
     */
    private String padKey(String key, int length) {
        if (key.length() >= length) {
            return key.substring(0, length);
        }

        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < length) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("algorithm", new ToolParameter(
                "algorithm",
                "string",
                "算法: AES, DES, MD5, SHA-256, SHA-512",
                true,
                "AES"
        ));

        schema.put("operation", new ToolParameter(
                "operation",
                "string",
                "操作: encrypt(加密), decrypt(解密)",
                false,
                "encrypt"
        ));

        schema.put("input", new ToolParameter(
                "input",
                "string",
                "需要处理的内容",
                true,
                null
        ));

        schema.put("key", new ToolParameter(
                "key",
                "string",
                "密钥（AES/DES 加密解密时需要，留空则自动生成）",
                false,
                null
        ));

        return schema;
    }
}
