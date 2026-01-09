package com.huawei.codearts.tool.impl;

import com.huawei.codearts.tool.DeveloperTool;
import com.huawei.codearts.tool.ToolParameter;
import com.huawei.codearts.tool.ToolResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 续签评估系统工具
 * 根据职级、年龄、绩效、团队业务潜力等因素评估员工续签可能性
 */
@Component
public class ContractRenewalEvaluatorTool implements DeveloperTool {

    @Override
    public String getId() {
        return "contract-renewal-evaluator";
    }

    @Override
    public String getName() {
        return "续签评估系统";
    }

    @Override
    public String getDescription() {
        return "根据职级、年龄、绩效、团队业务潜力等因素评估员工续签可能性";
    }

    @Override
    public String getCategory() {
        return "评估工具";
    }

    @Override
    public ToolResult execute(Map<String, String> params) {
        try {
            // 获取输入参数
            int level = Integer.parseInt(params.getOrDefault("level", "15"));
            int age = Integer.parseInt(params.getOrDefault("age", "30"));
            String perf2022 = params.getOrDefault("perf2022", "B");
            String perf2023 = params.getOrDefault("perf2023", "B");
            String perf2024 = params.getOrDefault("perf2024", "B");
            String perf2025 = params.getOrDefault("perf2025", "B");
            int teamPotential = Integer.parseInt(params.getOrDefault("teamPotential", "60"));
            int otherFactors = Integer.parseInt(params.getOrDefault("otherFactors", "60"));

            // 验证职级范围
            if (level < 13 || level > 22) {
                return ToolResult.error("职级必须在13~22之间");
            }

            // 验证年龄范围
            if (age < 20 || age > 60) {
                return ToolResult.error("年龄必须在20~60之间");
            }

            // 计算各因素得分
            Map<String, Object> details = new HashMap<>();

            // 1. 职级和年龄评估（30%）
            int levelAgeScore = calculateLevelAgeScore(level, age);
            details.put("职级", level);
            details.put("年龄", age);
            details.put("职级年龄得分", levelAgeScore);

            // 2. 绩效评估（40%）
            int performanceScore = calculatePerformanceScore(perf2022, perf2023, perf2024, perf2025);
            details.put("2022绩效", perf2022);
            details.put("2023绩效", perf2023);
            details.put("2024绩效", perf2024);
            details.put("2025绩效", perf2025);
            details.put("绩效评估得分", performanceScore);

            // 3. 团队业务发展潜力（20%）
            details.put("团队业务发展潜力得分", teamPotential);

            // 4. 其它因素（10%）
            details.put("其它因素得分", otherFactors);

            // 计算总分
            double totalScore = (levelAgeScore * 0.3) + (performanceScore * 0.4)
                              + (teamPotential * 0.2) + (otherFactors * 0.1);

            // 四舍五入到两位小数
            totalScore = Math.round(totalScore * 100.0) / 100.0;

            // 判断续签结果
            String renewalResult = evaluateRenewalResult(totalScore);
            String color = getResultColor(totalScore);

            details.put("总分", totalScore);
            details.put("续签评估", renewalResult);
            details.put("颜色标识", color);

            // 生成建议
            String advice = generateAdvice(totalScore, levelAgeScore, performanceScore, teamPotential, otherFactors);
            details.put("建议", advice);

            Map<String, Object> data = new HashMap<>();
            data.put("result", formatResult(details));
            data.put("score", totalScore);
            data.put("result", renewalResult);
            data.put("details", details);

            return ToolResult.success("评估完成", data);
        } catch (NumberFormatException e) {
            return ToolResult.error("参数格式错误: 请确保数字字段输入正确");
        } catch (Exception e) {
            return ToolResult.error("评估失败: " + e.getMessage());
        }
    }

    /**
     * 计算职级和年龄得分
     */
    private int calculateLevelAgeScore(int level, int age) {
        if (level <= 18) {
            // 18级及以下: 职级 * 2 >= 年龄
            int threshold = level * 2;
            if (age <= threshold) {
                return 100;
            } else {
                // 每超1岁扣20分
                int excess = age - threshold;
                int score = 100 - (excess * 20);
                return Math.max(0, score);
            }
        } else {
            // 19级及以上: 年龄门槛放宽至44~45岁
            if (age <= 44) {
                return 100;
            } else if (age == 45) {
                return 80;
            } else {
                // 46岁及以上，每超1岁扣20分
                int excess = age - 45;
                int score = 80 - (excess * 20);
                return Math.max(0, score);
            }
        }
    }

    /**
     * 计算绩效评估得分
     */
    private int calculatePerformanceScore(String... performances) {
        int total = 0;
        int count = 0;

        for (String perf : performances) {
            int score = getPerformanceScore(perf);
            total += score;
            count++;
        }

        return count > 0 ? total / count : 0;
    }

    /**
     * 获取单个绩效得分
     */
    private int getPerformanceScore(String perf) {
        if (perf == null || perf.trim().isEmpty()) {
            return 60; // 默认B
        }

        switch (perf.trim().toUpperCase()) {
            case "A":
                return 100;
            case "B+":
                return 80;
            case "B":
                return 60;
            case "C":
            case "D":
                return 0;
            default:
                return 60; // 默认B
        }
    }

    /**
     * 评估续签结果
     */
    private String evaluateRenewalResult(double totalScore) {
        if (totalScore >= 90) {
            return "一定续签";
        } else if (totalScore >= 70) {
            return "大概率续签";
        } else if (totalScore >= 60) {
            return "可能续签";
        } else {
            return "不续签";
        }
    }

    /**
     * 获取结果颜色标识
     */
    private String getResultColor(double totalScore) {
        if (totalScore >= 90) {
            return "green";
        } else if (totalScore >= 70) {
            return "blue";
        } else if (totalScore >= 60) {
            return "orange";
        } else {
            return "red";
        }
    }

    /**
     * 生成建议
     */
    private String generateAdvice(double totalScore, int levelAgeScore, int performanceScore,
                                  int teamPotential, int otherFactors) {
        StringBuilder advice = new StringBuilder();

        if (totalScore >= 90) {
            advice.append("表现优秀，续签可能性很高。继续保持良好的工作状态。");
        } else if (totalScore >= 70) {
            advice.append("续签可能性较大。");
            if (performanceScore < 80) {
                advice.append(" 建议提升绩效表现，争取获得A或B+评价。");
            }
            if (levelAgeScore < 80) {
                advice.append(" 注意年龄与职级的匹配性问题。");
            }
        } else if (totalScore >= 60) {
            advice.append("续签存在一定不确定性。");
            if (performanceScore < 70) {
                advice.append(" 绩效需要提升，建议设定明确目标并积极争取。");
            }
            if (teamPotential < 70) {
                advice.append(" 关注团队业务发展，考虑调整到更有发展潜力的项目。");
            }
            if (otherFactors < 70) {
                advice.append(" 建议积累核心项目经验或争取荣誉，增强个人竞争力。");
            }
        } else {
            advice.append("续签风险较高。建议全面分析自身情况，制定改进计划。");
            if (performanceScore < 60) {
                advice.append(" 重点关注绩效提升。");
            }
        }

        return advice.toString();
    }

    /**
     * 格式化结果输出
     */
    private String formatResult(Map<String, Object> details) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 续签评估报告 ===\n\n");

        sb.append("【基本信息】\n");
        sb.append("职级: ").append(details.get("职级")).append("级\n");
        sb.append("年龄: ").append(details.get("年龄")).append("岁\n\n");

        sb.append("【评分详情】\n");
        sb.append("1. 职级年龄得分: ").append(details.get("职级年龄得分")).append("分 (权重30%)\n");
        sb.append("2. 绩效评估得分: ").append(details.get("绩效评估得分")).append("分 (权重40%)\n");
        sb.append("   - 2022: ").append(details.get("2022绩效")).append("\n");
        sb.append("   - 2023: ").append(details.get("2023绩效")).append("\n");
        sb.append("   - 2024: ").append(details.get("2024绩效")).append("\n");
        sb.append("   - 2025: ").append(details.get("2025绩效")).append("\n");
        sb.append("3. 团队业务发展潜力: ").append(details.get("团队业务发展潜力得分")).append("分 (权重20%)\n");
        sb.append("4. 其它因素: ").append(details.get("其它因素得分")).append("分 (权重10%)\n\n");

        sb.append("【评估结果】\n");
        sb.append("总分: ").append(details.get("总分")).append("分\n");
        sb.append("续签评估: ").append(details.get("续签评估")).append("\n\n");

        sb.append("【建议】\n");
        sb.append(details.get("建议"));

        return sb.toString();
    }

    @Override
    public Map<String, ToolParameter> getParameterSchema() {
        Map<String, ToolParameter> schema = new LinkedHashMap<>();

        schema.put("level", new ToolParameter(
                "level",
                "number",
                "职级 (13-22)",
                true,
                "15"
        ));

        schema.put("age", new ToolParameter(
                "age",
                "number",
                "年龄 (20-60)",
                true,
                "30"
        ));

        schema.put("perf2022", new ToolParameter(
                "perf2022",
                "string",
                "2022年绩效 (A/B+/B/C/D)",
                false,
                "B"
        ));

        schema.put("perf2023", new ToolParameter(
                "perf2023",
                "string",
                "2023年绩效 (A/B+/B/C/D)",
                false,
                "B"
        ));

        schema.put("perf2024", new ToolParameter(
                "perf2024",
                "string",
                "2024年绩效 (A/B+/B/C/D)",
                false,
                "B"
        ));

        schema.put("perf2025", new ToolParameter(
                "perf2025",
                "string",
                "2025年绩效 (A/B+/B/C/D)",
                false,
                "B"
        ));

        schema.put("teamPotential", new ToolParameter(
                "teamPotential",
                "number",
                "团队业务发展潜力 (100/80/60/40)",
                false,
                "60"
        ));

        schema.put("otherFactors", new ToolParameter(
                "otherFactors",
                "number",
                "其它因素得分 (0-100)",
                false,
                "60"
        ));

        return schema;
    }
}
