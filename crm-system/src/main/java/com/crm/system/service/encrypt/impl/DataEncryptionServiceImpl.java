package com.crm.system.service.encrypt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.encrypt.SensitiveFieldEncryptUtil;
import com.crm.system.domain.encrypt.SysFieldEncryptConfigDo;
import com.crm.system.domain.vo.encrypt.SysFieldEncryptConfigVo;
import com.crm.system.mapper.encrypt.SysFieldEncryptConfigMapper;
import com.crm.system.service.encrypt.IDataEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;

/**
 * 数据加密执行Service业务层处理
 * @author tjx
 * @date 2026-04-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataEncryptionServiceImpl implements IDataEncryptionService {

    private final SysFieldEncryptConfigMapper configMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 最大线程数限制
     */
    private static final int MAX_THREAD_COUNT = 8;

    /**
     * 最小批次大小
     */
    private static final int MIN_BATCH_SIZE = 100;

    /**
     * 最大批次大小
     */
    private static final int MAX_BATCH_SIZE = 5000;

    /**
     * 批次处理超时时间（分钟）
     */
    private static final int BATCH_TIMEOUT_MINUTES = 5;

    /**
     * 线程池关闭等待时间（分钟）
     */
    private static final int SHUTDOWN_WAIT_MINUTES = 10;

    @Override
    public Map<String, Object> executeEncryption(Long configId) {
        SysFieldEncryptConfigVo config = configMapper.selectVoById(configId);
        if (config == null) {
            throw new IllegalArgumentException("加密配置不存在，ID: " + configId);
        }

        // 校验 primary_key 不能为空
        if (StringUtils.isBlank(config.getPrimaryKey())) {
            throw new IllegalArgumentException(String.format(
                "表 [%s] 的配置中 primary_key 不能为空，请指定能唯一标识行的字段（多个字段用逗号分隔）",
                config.getTableName()
            ));
        }

        // 校验定位字段的唯一性
        validatePrimaryKeyUniqueness(config);

        return processEncryption(config);
    }

    @Override
    public Map<String, Object> executeEncryptionByTableAndField(String tableName, String fieldName) {
        log.info("按表名和字段名执行加密任务，表名：{}，字段：{}", tableName, fieldName);
        
        // 根据表名和字段名查询配置（大小写不敏感）
        LambdaQueryWrapper<SysFieldEncryptConfigDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("LOWER(table_name) = LOWER({0})", tableName)
                   .apply("LOWER(field_name) = LOWER({0})", fieldName);
        
        SysFieldEncryptConfigVo config = configMapper.selectVoOne(queryWrapper);
        
        if (config == null) {
            String errorMsg = String.format(
                "未找到表 [%s] 字段 [%s] 的加密配置，请先在 sys_field_encrypt_config 表中添加配置",
                tableName, fieldName
            );
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        log.info("找到配置，ID：{}，主键字段：{}", config.getId(), config.getPrimaryKey());
        
        // 校验 primary_key 不能为空
        if (StringUtils.isBlank(config.getPrimaryKey())) {
            throw new IllegalArgumentException(String.format(
                "表 [%s] 的配置中 primary_key 不能为空，请指定能唯一标识行的字段（多个字段用逗号分隔）",
                config.getTableName()
            ));
        }
        
        // 校验定位字段的唯一性
        validatePrimaryKeyUniqueness(config);
        
        // 执行加密任务
        return processEncryption(config);
    }

    /**
     * 处理加密任务的核心方法
     */
    private Map<String, Object> processEncryption(SysFieldEncryptConfigVo config) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        String tableName = config.getTableName();
        String fieldName = config.getFieldName();
        int batchSize = Optional.ofNullable(config.getBatchSize()).orElse(1000);
        int threadCount = Math.min(Optional.ofNullable(config.getThreadCount()).orElse(4), MAX_THREAD_COUNT);

        // 校验和修正批次大小
        batchSize = Math.max(MIN_BATCH_SIZE, Math.min(batchSize, MAX_BATCH_SIZE));

        log.info("========== 开始执行数据加密任务 ==========");
        log.info("表名: {}, 字段: {}, 批次大小: {}, 线程数: {}", tableName, fieldName, batchSize, threadCount);

        try {
            // 更新状态为执行中
            updateConfigStatus(config.getId(), "1", null, null);

            // 查询总数据量
            long totalCount = getTotalCount(tableName, fieldName);
            if (totalCount == 0) {
                log.info("表中没有需要加密的数据");
                updateConfigStatus(config.getId(), "2", 0L, 0L);
                result.put("success", true);
                result.put("totalCount", 0);
                result.put("encryptedCount", 0);
                result.put("message", "没有需要加密的数据");
                return result;
            }

            log.info("查询到总数据量: {}", totalCount);

            // 更新总记录数
            updateConfigTotalCount(config.getId(), totalCount);

            // 创建线程池
            ExecutorService executorService = createThreadPool(threadCount);

            // 计算总批次数
            int totalBatches = (int) Math.ceil((double) totalCount / batchSize);
            log.info("总批次数: {}", totalBatches);

            // 提交所有批次任务
            List<Future<Integer>> futures = submitBatchTasks(executorService, config, tableName, fieldName, batchSize, totalBatches);

            // 收集处理结果
            ProcessResult processResult = collectResults(futures, totalBatches);

            // 关闭线程池
            shutdownThreadPool(executorService);

            long costTime = System.currentTimeMillis() - startTime;
            long encryptedCount = processResult.getSuccessCount();

            // 更新最终状态
            String finalStatus = processResult.hasFailure() ? "3" : "2";
            updateConfigStatus(config.getId(), finalStatus, encryptedCount, totalCount);

            // 构建返回结果
            result.put("success", !processResult.hasFailure());
            result.put("totalCount", totalCount);
            result.put("encryptedCount", encryptedCount);
            result.put("successBatches", processResult.getSuccessBatches());
            result.put("failedBatches", processResult.getFailedBatches());
            result.put("costTime", costTime);
            result.put("message", String.format("加密完成: 总数%d, 成功%d, 耗时%dms", totalCount, encryptedCount, costTime));

            log.info("========== 数据加密任务完成 ==========");
            log.info("总数: {}, 成功: {}, 成功批次: {}, 失败批次: {}, 耗时: {}ms",
                totalCount, encryptedCount, processResult.getSuccessBatches(),
                processResult.getFailedBatches(), costTime);

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("========== 数据加密任务执行失败 ==========", e);
            log.error("表名: {}, 字段: {}, 耗时: {}ms, 错误: {}", tableName, fieldName, costTime, e.getMessage());

            updateConfigStatus(config.getId(), "3", null, null);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("costTime", costTime);

            throw new RuntimeException("数据加密任务执行失败", e);
        }

        return result;
    }

    /**
     * 查询总数据量
     */
    private long getTotalCount(String tableName, String fieldName) {
        String sql = String.format(
            "SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL AND %s != ''",
            tableName, fieldName, fieldName
        );
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("查询数据总量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 创建线程池
     */
    private ExecutorService createThreadPool(int threadCount) {
        return Executors.newFixedThreadPool(threadCount, r -> {
            Thread thread = new Thread(r);
            thread.setName("encrypt-worker-" + thread.getName());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 提交批次任务
     */
    private List<Future<Integer>> submitBatchTasks(ExecutorService executorService,
                                                     SysFieldEncryptConfigVo config,
                                                     String tableName, String fieldName,
                                                     int batchSize, int totalBatches) {
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < totalBatches; i++) {
            final int offset = i * batchSize;
            final int currentBatchSize = batchSize;
            final int batchIndex = i + 1;

            Future<Integer> future = executorService.submit(() -> {
                log.debug("开始处理批次 {}/{}", batchIndex, totalBatches);
                return processBatchWithConfig(config, tableName, fieldName, offset, currentBatchSize, batchIndex);
            });
            futures.add(future);
        }

        return futures;
    }

    /**
     * 处理单个批次的数据
     * 每个批次有独立的事务控制，保证批次内数据一致性
     */
    @Transactional(rollbackFor = Exception.class)
    public int processBatch(String tableName, String fieldName, int offset, int limit, int batchIndex) {
        return processBatchWithConfig(null, tableName, fieldName, offset, limit, batchIndex);
    }

    /**
     * 处理单个批次的数据（支持配置主键）
     */
    @Transactional(rollbackFor = Exception.class)
    public int processBatchWithConfig(SysFieldEncryptConfigVo config, String tableName, String fieldName, int offset, int limit, int batchIndex) {
        try {
            String primaryKey = config.getPrimaryKey().trim();

            // 查询批次数据
            List<Map<String, Object>> records = queryBatchData(tableName, fieldName, primaryKey, offset, limit);

            if (CollUtil.isEmpty(records)) {
                log.debug("批次 {} 没有数据", batchIndex);
                return 0;
            }

            // 准备批量更新参数
            List<Object[]> updateParams = new ArrayList<>();

            for (Map<String, Object> record : records) {
                Object fieldValue = record.get(fieldName);

                if (fieldValue == null || StrUtil.isBlank(fieldValue.toString())) {
                    continue;
                }

                String originalValue = fieldValue.toString();

                // 检查是否已加密
                if (SensitiveFieldEncryptUtil.isEncrypted(originalValue)) {
                    log.debug("批次 {} 中存在已加密数据，跳过", batchIndex);
                    continue;
                }

                // 加密数据
                String encryptedValue = SensitiveFieldEncryptUtil.smartEncrypt(originalValue);

                // 构造更新参数数组：[encryptedValue, key1_value, key2_value, ...]
                List<Object> paramList = new ArrayList<>();
                paramList.add(encryptedValue);

                String[] keys = primaryKey.split(",");
                boolean allKeysPresent = true;
                for (String key : keys) {
                    Object val = record.get(key.trim());
                    if (val == null) {
                        allKeysPresent = false;
                        log.warn("批次 {} 中记录的定位字段 [{}] 值为空，跳过", batchIndex, key);
                        break;
                    }
                    paramList.add(val);
                }

                if (allKeysPresent) {
                    updateParams.add(paramList.toArray(new Object[0]));
                }
            }

            // 批量更新
            if (!updateParams.isEmpty()) {
                batchUpdate(tableName, fieldName, primaryKey, updateParams);
                log.info("批次 {} 处理完成，加密 {} 条记录", batchIndex, updateParams.size());
            }

            return updateParams.size();

        } catch (Exception e) {
            log.error("批次 {} 处理失败: {}", batchIndex, e.getMessage(), e);
            throw new RuntimeException("批次处理失败，事务将回滚", e);
        }
    }

    /**
     * 查询批次数据
     */
    private List<Map<String, Object>> queryBatchData(String tableName, String fieldName, String primaryKey, int offset, int limit) {
        String sql = String.format(
            "SELECT %s, %s FROM %s WHERE %s IS NOT NULL AND %s != '' LIMIT ? OFFSET ?",
            primaryKey, fieldName, tableName, fieldName, fieldName
        );

        try {
            return jdbcTemplate.queryForList(sql, limit, offset);
        } catch (Exception e) {
            log.error("查询批次数据失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量更新数据（通过循环调用 MyBatis 单条更新实现）
     */
    private void batchUpdate(String tableName, String fieldName, String primaryKey, List<Object[]> params) {
        if (CollUtil.isEmpty(params)) {
            return;
        }

        String[] keys = primaryKey.split(",");
        int successCount = 0;

        log.debug("开始执行 MyBatis 循环更新，共 {} 条记录", params.size());
        try {
            for (Object[] param : params) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("encryptedValue", param[0]);
                for (int i = 0; i < keys.length; i++) {
                    recordMap.put(keys[i].trim(), param[i + 1]);
                }

                configMapper.updateEncryptedField(tableName, fieldName, primaryKey, recordMap);
                successCount++;
            }
            log.info("循环更新全部完成，共 {} 条记录", successCount);
        } catch (Exception e) {
            log.error("更新失败，已成功 {} 条: {}", successCount, e.getMessage(), e);
            throw new RuntimeException("数据更新失败", e);
        }
    }

    /**
     * 校验定位字段（Primary Key）的唯一性
     * 逻辑：SELECT pk_fields, count(1) as cnt FROM table GROUP BY pk_fields HAVING cnt > 1 LIMIT 1
     * 如果查出数据，说明存在重复，不能作为唯一更新条件
     */
    private void validatePrimaryKeyUniqueness(SysFieldEncryptConfigVo config) {
        String tableName = config.getTableName();
        String primaryKey = config.getPrimaryKey();

        if (StringUtils.isBlank(primaryKey)) {
            throw new IllegalArgumentException(String.format(
                "表 [%s] 的配置中 primary_key 不能为空，请指定能唯一标识行的字段",
                tableName
            ));
        }

        // 构造校验 SQL：检查是否有重复数据
        String sql = String.format(
            "SELECT %s, count(1) as cnt FROM %s GROUP BY %s HAVING cnt > 1 LIMIT 1",
            primaryKey, tableName, primaryKey
        );

        log.info("开始校验表 [{}] 的定位字段 [{}] 唯一性", tableName, primaryKey);

        try {
            List<Map<String, Object>> duplicates = jdbcTemplate.queryForList(sql);
            if (!duplicates.isEmpty()) {
                String errorMsg = String.format(
                    "表 [%s] 的定位字段 [%s] 存在重复数据，无法作为唯一更新条件！重复示例：%s",
                    tableName, primaryKey, duplicates.get(0)
                );
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
            log.info("表 [{}] 的定位字段 [{}] 唯一性校验通过", tableName, primaryKey);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("校验定位字段唯一性时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("唯一性校验失败，请检查表名和字段名是否正确", e);
        }
    }

    /**
     * 收集处理结果
     */
    private ProcessResult collectResults(List<Future<Integer>> futures, int totalBatches) {
        ProcessResult result = new ProcessResult();
        int successBatches = 0;
        int failedBatches = 0;

        for (Future<Integer> future : futures) {
            try {
                Integer encryptedCount = future.get(BATCH_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                if (encryptedCount != null) {
                    result.addSuccessCount(encryptedCount);
                    successBatches++;
                }
            } catch (Exception e) {
                failedBatches++;
                log.error("批次处理失败: {}", e.getMessage(), e);
            }
        }

        result.setSuccessBatches(successBatches);
        result.setFailedBatches(failedBatches);

        return result;
    }

    /**
     * 关闭线程池
     */
    private void shutdownThreadPool(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(SHUTDOWN_WAIT_MINUTES, TimeUnit.MINUTES)) {
                log.warn("线程池未在指定时间内关闭，强制关闭");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 更新配置状态和计数
     */
    private void updateConfigStatus(Long configId, String status, Long processedCount, Long totalCount) {
        LambdaUpdateWrapper<SysFieldEncryptConfigDo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysFieldEncryptConfigDo::getId, configId);
        updateWrapper.set(SysFieldEncryptConfigDo::getStatus, status);

        if (processedCount != null) {
            updateWrapper.set(SysFieldEncryptConfigDo::getProcessedCount, processedCount);
        }
        if (totalCount != null) {
            updateWrapper.set(SysFieldEncryptConfigDo::getTotalCount, totalCount);
        }

        configMapper.update(null, updateWrapper);
    }

    /**
     * 更新总记录数
     */
    private void updateConfigTotalCount(Long configId, Long totalCount) {
        LambdaUpdateWrapper<SysFieldEncryptConfigDo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysFieldEncryptConfigDo::getId, configId);
        updateWrapper.set(SysFieldEncryptConfigDo::getTotalCount, totalCount);
        configMapper.update(null, updateWrapper);
    }

    /**
     * 内部类：处理结果
     */
    private static class ProcessResult {
        private int successCount = 0;
        private int successBatches = 0;
        private int failedBatches = 0;

        public void addSuccessCount(int count) {
            this.successCount += count;
        }

        public boolean hasFailure() {
            return failedBatches > 0;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getSuccessBatches() {
            return successBatches;
        }

        public void setSuccessBatches(int successBatches) {
            this.successBatches = successBatches;
        }

        public int getFailedBatches() {
            return failedBatches;
        }

        public void setFailedBatches(int failedBatches) {
            this.failedBatches = failedBatches;
        }
    }
}
