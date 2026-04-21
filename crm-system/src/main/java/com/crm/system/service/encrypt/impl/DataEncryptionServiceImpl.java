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
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    private final SqlSessionFactory sqlSessionFactory;

    @Override
    public Map<String, Object> executeEncryptionByTableAndField(String tableName, String fieldName) {
        log.info("按表名和字段名执行加密任务，表名：{}，字段：{}", tableName, fieldName);

        // 查询该表该字段的配置
        LambdaQueryWrapper<SysFieldEncryptConfigDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("LOWER(table_name) = LOWER({0})", tableName);

        if (StringUtils.isNotBlank(fieldName)) {
            queryWrapper.apply("LOWER(field_name) = LOWER({0})", fieldName);
        }

        // 只查询状态不为 1(执行中) 和 2(已完成) 的配置
        queryWrapper.notIn(SysFieldEncryptConfigDo::getStatus, "1", "2");

        List<SysFieldEncryptConfigVo> configs = configMapper.selectVoList(queryWrapper);

        if (CollUtil.isEmpty(configs)) {
            String errorMsg = StringUtils.isNotBlank(fieldName)
                ? String.format("未找到表 [%s] 字段 [%s] 的加密配置", tableName, fieldName)
                : String.format("未找到表 [%s] 的加密配置", tableName);
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 直接处理该表的加密（configs 已经是同一个表的配置列表）
        return processTableEncryption(tableName, configs);
    }

    /**
     * 处理单个表的加密（多字段合并更新）
     */
    private Map<String, Object> processTableEncryption(String tableName, List<SysFieldEncryptConfigVo> configs) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        // 获取所有需要加密的字段
        List<String> fieldNames = configs.stream()
            .map(SysFieldEncryptConfigVo::getFieldName)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(fieldNames)) {
            log.warn("表 [{}] 没有有效的字段配置", tableName);
            result.put("success", false);
            result.put("message", "没有有效的字段配置");
            return result;
        }

        // 取第一条配置的 primary_key（同表的不同字段配置，primary_key 应该一致）
        String primaryKey = configs.get(0).getPrimaryKey();
        if (StringUtils.isBlank(primaryKey)) {
            throw new IllegalArgumentException(String.format(
                "表 [%s] 的配置中 primary_key 不能为空", tableName
            ));
        }

        // 校验主键唯一性
        validatePrimaryKeyUniqueness(tableName, primaryKey);

        // 获取批次大小和线程数
        int batchSize = Optional.ofNullable(configs.get(0).getBatchSize()).orElse(10000);
        int threadCount = Optional.ofNullable(configs.get(0).getThreadCount()).orElse(4);
        // 限制最大线程数为 8，避免过度并发
        threadCount = Math.min(threadCount, 8);

        log.info("========== 开始处理表 [{}] ==========", tableName);
        log.info("字段列表: {}, 主键: {}, 批次大小: {}, 线程数: {}", fieldNames, primaryKey, batchSize, threadCount);

        try {
            // 更新所有相关配置的状态为执行中
            updateConfigsStatus(configs, "1");

            // 查询总数据量
            long totalCount = getTotalCountForMultipleFields(tableName, fieldNames);
            if (totalCount == 0) {
                log.info("表 [{}] 中没有需要加密的数据", tableName);
                updateConfigsStatus(configs, "2", 0L, 0L);
                result.put("success", true);
                result.put("totalCount", 0);
                result.put("encryptedCount", 0);
                result.put("message", "没有需要加密的数据");
                return result;
            }

            log.info("表 [{}] 查询到总数据量: {}", tableName, totalCount);

            // 根据线程数决定使用多线程还是单线程
            int totalUpdated;
            if (threadCount > 1) {
                // 多线程处理
                totalUpdated = processWithMultiThreads(tableName, fieldNames, primaryKey, batchSize, totalCount, threadCount);
            } else {
                // 单线程处理（原有逻辑）
                totalUpdated = processWithSqlSessionBatch(tableName, fieldNames, primaryKey, batchSize, totalCount);
            }

            long costTime = System.currentTimeMillis() - startTime;

            // 更新所有配置的状态为已完成
            updateConfigsStatus(configs, "2", (long) totalUpdated, totalCount);

            result.put("success", true);
            result.put("totalCount", totalCount);
            result.put("encryptedCount", totalUpdated);
            result.put("costTime", costTime);
            result.put("message", String.format("加密完成: 总数%d, 成功%d, 耗时%dms", totalCount, totalUpdated, costTime));

            log.info("========== 表 [{}] 处理完成 ==========", tableName);
            log.info("总数: {}, 成功: {}, 耗时: {}ms", totalCount, totalUpdated, costTime);

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("========== 表 [{}] 处理失败 ==========", tableName, e);

            // 更新所有配置的状态为失败
            updateConfigsStatus(configs, "3", null, null);

            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("costTime", costTime);

            throw new RuntimeException(String.format("表 [%s] 加密任务执行失败", tableName), e);
        }

        return result;
    }

    /**
     * 多线程批量处理（按主键范围分片）
     */
    private int processWithMultiThreads(String tableName, List<String> fieldNames,
                                         String primaryKey, int batchSize, long totalCount, int threadCount) {
        log.info("启动多线程处理，线程数: {}, 总数据量: {}", threadCount, totalCount);

        // 创建固定大小的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalUpdated = new AtomicLong(0);

        try {
            // 计算每个线程处理的数据量
            long chunkSize = (long) Math.ceil((double) totalCount / threadCount);
            log.info("数据分片：每线程处理约 {} 条数据", chunkSize);

            // 启动多个线程
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                final long offset = (long) i * chunkSize;
                final long limit = Math.min(chunkSize, totalCount - offset);

                if (limit <= 0) {
                    latch.countDown();
                    continue;
                }

                executorService.submit(() -> {
                    try {
                        log.info("线程 [{}] 开始处理，偏移量: {}, 数量: {}", threadIndex, offset, limit);
                        
                        // 每个线程使用独立的 SqlSession
                        int updated = processChunkWithSqlSession(tableName, fieldNames, primaryKey, batchSize, offset, (int) limit);
                        
                        totalUpdated.addAndGet(updated);
                        log.info("线程 [{}] 处理完成，更新: {} 条", threadIndex, updated);
                    } catch (Exception e) {
                        log.error("线程 [{}] 处理失败", threadIndex, e);
                        throw new RuntimeException("线程处理失败", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            latch.await();
            log.info("所有线程处理完成，总计更新: {} 条", totalUpdated.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("多线程处理被中断", e);
            throw new RuntimeException("多线程处理被中断", e);
        } finally {
            executorService.shutdown();
        }

        return (int) totalUpdated.get();
    }

    /**
     * 处理一个数据分片（使用 SqlSession BATCH 模式）
     */
    private int processChunkWithSqlSession(String tableName, List<String> fieldNames,
                                            String primaryKey, int batchSize, long startOffset, int totalLimit) {
        int totalUpdated = 0;
        int batchCommitSize = 50; // 每 50 个批次提交一次
        int processedLimit = 0;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            long totalBatches = (long) Math.ceil((double) totalLimit / batchSize);
            log.debug("分片批次数: {}", totalBatches);

            for (long i = 0; i < totalBatches; i++) {
                int offset = (int) (startOffset + i * batchSize);
                int currentBatchSize = Math.min(batchSize, totalLimit - processedLimit);

                if (currentBatchSize <= 0) {
                    break;
                }

                // 查询批次数据
                List<Map<String, Object>> records = queryBatchDataForMultipleFields(
                    tableName, fieldNames, primaryKey, offset, currentBatchSize
                );

                if (CollUtil.isEmpty(records)) {
                    break;
                }

                // 加密并批量更新
                int updated = encryptAndBatchUpdate(sqlSession, tableName, fieldNames, primaryKey, records);
                totalUpdated += updated;
                processedLimit += records.size();

                // 定期提交
                if ((i + 1) % batchCommitSize == 0 || i == totalBatches - 1) {
                    sqlSession.commit();
                    sqlSession.clearCache();
                    log.debug("分片已提交批次: {}/{}, 已处理: {} 条", i + 1, totalBatches, totalUpdated);
                }
            }
        }

        return totalUpdated;
    }

    /**
     * 使用 SqlSession BATCH 模式批量处理（单线程版本）
     */
    private int processWithSqlSessionBatch(String tableName, List<String> fieldNames,
                                            String primaryKey, int batchSize, long totalCount) {
        int totalUpdated = 0;
        int batchCommitSize = 100; // 每 100 个批次提交一次

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            long totalBatches = (long) Math.ceil((double) totalCount / batchSize);
            log.info("总批次数: {}, 每 {} 批次提交一次", totalBatches, batchCommitSize);

            for (long i = 0; i < totalBatches; i++) {
                int offset = (int) (i * batchSize);

                // 查询批次数据
                List<Map<String, Object>> records = queryBatchDataForMultipleFields(
                    tableName, fieldNames, primaryKey, offset, batchSize
                );

                if (CollUtil.isEmpty(records)) {
                    continue;
                }

                // 加密并批量更新
                int updated = encryptAndBatchUpdate(sqlSession, tableName, fieldNames, primaryKey, records);
                totalUpdated += updated;

                // 每 batchCommitSize 个批次提交一次
                if ((i + 1) % batchCommitSize == 0 || i == totalBatches - 1) {
                    sqlSession.commit();
                    sqlSession.clearCache();
                    log.info("已提交批次: {}/{}, 已处理: {} 条", i + 1, totalBatches, totalUpdated);
                }
            }
        }

        return totalUpdated;
    }

    /**
     * 加密并批量更新（使用原生 SQL + PreparedStatement）
     */
    private int encryptAndBatchUpdate(SqlSession sqlSession, String tableName, List<String> fieldNames,
                                       String primaryKey, List<Map<String, Object>> records) {
        // 构造动态 UPDATE SQL
        StringBuilder setClause = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            if (i > 0) setClause.append(", ");
            setClause.append(fieldNames.get(i)).append(" = ?");
        }

        String[] keys = primaryKey.split(",");
        StringBuilder whereClause = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) whereClause.append(" AND ");
            whereClause.append(keys[i].trim()).append(" = ?");
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s",
            tableName, setClause.toString(), whereClause.toString());

        int updatedCount = 0;

        try {
            // 获取 JDBC Connection
            Connection connection = sqlSession.getConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Map<String, Object> record : records) {
                    List<Object> params = new ArrayList<>();
                    boolean hasUpdate = false;

                    // 添加加密后的字段值
                    for (String fieldName : fieldNames) {
                        Object fieldValue = record.get(fieldName);

                        if (fieldValue == null || StrUtil.isBlank(fieldValue.toString())) {
                            params.add(null);  // NULL 会被 COALESCE 忽略，保持原值
                            continue;
                        }

                        String originalValue = fieldValue.toString();

                        // 跳过已加密数据
                        if (SensitiveFieldEncryptUtil.isEncrypted(originalValue)) {
                            params.add(null);  // 已加密的也传 NULL，保持原值
                            continue;
                        }

                        // 加密
                        try {
                            String encryptedValue = SensitiveFieldEncryptUtil.encrypt(originalValue);
                            params.add(encryptedValue);
                            hasUpdate = true;
                        } catch (Exception e) {
                            log.error("加密失败 - 表: {}, 字段: {}, 主键: {}, 原始值: {}, 错误: {}",
                                tableName, fieldName, record.get(primaryKey), originalValue, e.getMessage());
                            throw new RuntimeException(
                                String.format("加密失败 - 表: %s, 字段: %s", tableName, fieldName), e);
                        }
                    }

                    // 添加主键值
                    for (String key : keys) {
                        params.add(record.get(key.trim()));
                    }

                    if (hasUpdate) {
                        // 设置参数
                        for (int i = 0; i < params.size(); i++) {
                            ps.setObject(i + 1, params.get(i));
                        }

                        // 添加到批处理
                        ps.addBatch();
                        updatedCount++;
                    }
                }

                // 执行批处理
                if (updatedCount > 0) {
                    ps.executeBatch();
                }
            }
        } catch (SQLException e) {
            log.error("批量更新失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新数据库失败", e);
        }

        return updatedCount;
    }

    /**
     * 查询多字段的批次数据
     */
    private List<Map<String, Object>> queryBatchDataForMultipleFields(String tableName, List<String> fieldNames,
                                                                        String primaryKey, int offset, int limit) {
        // 构造查询字段
        StringBuilder selectFields = new StringBuilder(primaryKey);
        for (String field : fieldNames) {
            selectFields.append(", ").append(field);
        }

        // 构造 WHERE 条件（使用 OR 连接）
        StringBuilder whereClause = new StringBuilder("(");
        for (int i = 0; i < fieldNames.size(); i++) {
            if (i > 0) whereClause.append(" OR ");
            whereClause.append(fieldNames.get(i)).append(" IS NOT NULL AND ").append(fieldNames.get(i)).append(" != ''");
        }
        whereClause.append(")");

        try {
            return configMapper.selectBatchDataForMultipleFields(
                tableName, selectFields.toString(), whereClause.toString(), primaryKey, limit, offset
            );
        } catch (Exception e) {
            log.error("查询批次数据失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询多字段的总数据量
     */
    private long getTotalCountForMultipleFields(String tableName, List<String> fieldNames) {
        // 构造 WHERE 条件（使用 OR 连接）
        StringBuilder whereClause = new StringBuilder("(");
        for (int i = 0; i < fieldNames.size(); i++) {
            if (i > 0) whereClause.append(" OR ");
            whereClause.append(fieldNames.get(i)).append(" IS NOT NULL AND ").append(fieldNames.get(i)).append(" != ''");
        }
        whereClause.append(")");

        try {
            Long count = configMapper.countTotalDataForMultipleFields(tableName, whereClause.toString());
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("查询数据总量失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 校验定位字段的唯一性
     */
    private void validatePrimaryKeyUniqueness(String tableName, String primaryKey) {
        log.info("开始校验表 [{}] 的定位字段 [{}] 唯一性", tableName, primaryKey);

        try {
            List<Map<String, Object>> duplicates = configMapper.validatePrimaryKeyUniqueness(tableName, primaryKey);
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
     * 批量更新配置状态
     */
    private void updateConfigsStatus(List<SysFieldEncryptConfigVo> configs, String status) {
        updateConfigsStatus(configs, status, null, null);
    }

    /**
     * 批量更新配置状态和计数
     */
    private void updateConfigsStatus(List<SysFieldEncryptConfigVo> configs, String status,
                                      Long processedCount, Long totalCount) {
        for (SysFieldEncryptConfigVo config : configs) {
            LambdaUpdateWrapper<SysFieldEncryptConfigDo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysFieldEncryptConfigDo::getId, config.getId());
            updateWrapper.set(SysFieldEncryptConfigDo::getStatus, status);

            if (processedCount != null) {
                updateWrapper.set(SysFieldEncryptConfigDo::getProcessedCount, processedCount);
            }
            if (totalCount != null) {
                updateWrapper.set(SysFieldEncryptConfigDo::getTotalCount, totalCount);
            }

            configMapper.update(null, updateWrapper);
        }
    }
}
