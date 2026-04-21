package com.crm.job.service.encrypt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.crm.system.service.encrypt.IDataEncryptionService;
import com.crm.system.service.encrypt.ISysFieldEncryptConfigService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 敏感字段加密定时任务
 * 用于批量加密历史数据中的敏感字段
 *
 * @author tjx
 * @date 2026-04-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveFieldEncryptionJob {

    private final ISysFieldEncryptConfigService configService;
    private final IDataEncryptionService dataEncryptionService;

    /**
     * XXL-Job任务标识
     */
    private static final String JOB_HANDLER_NAME = "sensitiveFieldEncryptionJob";

    /**
     * 执行敏感字段加密任务
     *
     * 任务参数说明（JSON格式）：
     * {
     *   "tableName": "user",           // 表名（必填）
     *   "fieldName": "phone"           // 字段名（可选，不传则加密该表所有配置的字段）
     * }
     *
     * 示例：
     * 1. 加密单个字段：{"tableName":"user","fieldName":"phone"}
     * 2. 加密整表：{"tableName":"user"}
     * 3. 无参数：加密所有状态不为 1 和 2 的配置
     */
    @XxlJob(JOB_HANDLER_NAME)
    public void execute() {
        long startTime = System.currentTimeMillis();
        String jobParam = XxlJobHelper.getJobParam();

        log.info("========== 【敏感字段加密任务】开始执行 ==========");
        log.info("任务参数：{}", jobParam);
        log.info("开始时间：{}", DateUtil.now());

        try {
            // 解析任务参数
            EncryptionTaskParams params = parseTaskParams(jobParam);

            // 执行加密任务
            if (params.getTableName() != null) {
                // 按表名（和字段名）执行
                executeByTableAndField(params.getTableName(), params.getFieldName());
            } else {
                // 执行所有待处理的配置
                executeAllPendingConfigs();
            }

            long costTime = System.currentTimeMillis() - startTime;
            String successMsg = String.format("【敏感字段加密任务】执行完成，耗时：%dms", costTime);
            log.info(successMsg);
            log.info("========== 【敏感字段加密任务】执行结束 ==========");
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("【敏感字段加密任务】执行失败，耗时：%dms，异常信息：%s",
                costTime, e.getMessage());
            log.error(errorMsg, e);
            log.info("========== 【敏感字段加密任务】执行失败 ==========");
            XxlJobHelper.handleFail(errorMsg);
            throw new RuntimeException("敏感字段加密任务执行异常", e);
        }
    }

    /**
     * 按表名和字段名执行
     */
    private void executeByTableAndField(String tableName, String fieldName) {
        log.info("按表名和字段名执行加密任务，表名：{}，字段：{}", tableName, fieldName);

        Map<String, Object> result = dataEncryptionService.executeEncryptionByTableAndField(tableName, fieldName);
        logExecutionResult(result);
    }

    /**
     * 执行所有待处理的配置
     */
    private void executeAllPendingConfigs() {
        log.info("执行所有待处理的加密配置");

        // 第一步：查询所有需要处理的表名（去重）
        List<String> tableNames = getDistinctTableNames();

        if (CollUtil.isEmpty(tableNames)) {
            log.info("没有待处理的加密配置");
            XxlJobHelper.handleSuccess("没有待处理的加密配置");
            return;
        }

        log.info("查询到 {} 个表需要处理", tableNames.size());

        int successCount = 0;
        int failCount = 0;

        // 第二步：遍历每个表名
        for (String tableName : tableNames) {
            try {
                log.info("========== 开始处理表: {} ==========", tableName);

                // 按表名执行（会自动处理该表下所有配置的字段）
                Map<String, Object> result = dataEncryptionService.executeEncryptionByTableAndField(tableName, null);

                if (Boolean.TRUE.equals(result.get("success"))) {
                    successCount++;
                    log.info("表 [{}] 处理成功：{}", tableName, result.get("message"));
                } else {
                    failCount++;
                    log.error("表 [{}] 处理失败：{}", tableName, result.get("error"));
                }

            } catch (Exception e) {
                failCount++;
                log.error("表 [{}] 处理异常：{}", tableName, e.getMessage(), e);
            }
        }

        log.info("所有表处理完成：成功={}, 失败={}", successCount, failCount);
    }

    /**
     * 获取需要处理的表名列表（去重）
     * SELECT TABLE_NAME FROM SYS_FIELD_ENCRYPT_CONFIG WHERE STATUS IN ('0','3') GROUP BY TABLE_NAME
     */
    private List<String> getDistinctTableNames() {
        return configService.getDistinctTableNamesWithPendingStatus();
    }

    /**
     * 解析任务参数
     * 支持 JSON 格式：{"tableName":"user","fieldName":"phone"}
     */
    private EncryptionTaskParams parseTaskParams(String jobParam) {
        EncryptionTaskParams params = new EncryptionTaskParams();

        if (jobParam == null || jobParam.trim().isEmpty()) {
            log.info("任务参数为空，将执行所有待处理的配置");
            return params;
        }

        try {
            // 尝试解析为 JSON 格式
            if (jobParam.trim().startsWith("{")) {
                JSONObject jsonObject = JSONUtil.parseObj(jobParam);

                if (jsonObject.containsKey("tableName")) {
                    params.setTableName(jsonObject.getStr("tableName"));
                }
                if (jsonObject.containsKey("fieldName")) {
                    params.setFieldName(jsonObject.getStr("fieldName"));
                }

                log.info("解析 JSON 参数成功：{}", jsonObject);
            } else {
                // 兼容旧的 key=value 格式
                String[] pairs = jobParam.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String value = kv[1].trim();

                        switch (key) {
                            case "tableName":
                                params.setTableName(value);
                                break;
                            case "fieldName":
                                params.setFieldName(value);
                                break;
                            default:
                                log.warn("未知的参数键：{}", key);
                        }
                    }
                }
                log.info("解析 key=value 参数成功");
            }
        } catch (Exception e) {
            log.error("解析任务参数失败：{}", e.getMessage(), e);
            throw new IllegalArgumentException("任务参数格式错误，请使用 JSON 格式：{\"tableName\":\"user\",\"fieldName\":\"phone\"}");
        }

        return params;
    }

    /**
     * 记录执行结果
     */
    private void logExecutionResult(Map<String, Object> result) {
        log.info("执行结果：");
        log.info("  - 成功：{}", result.get("success"));
        log.info("  - 总记录数：{}", result.get("totalCount"));
        log.info("  - 加密记录数：{}", result.get("encryptedCount"));
        log.info("  - 成功批次：{}", result.get("successBatches"));
        log.info("  - 失败批次：{}", result.get("failedBatches"));
        log.info("  - 耗时：{}ms", result.get("costTime"));
        log.info("  - 消息：{}", result.get("message"));
    }
}
