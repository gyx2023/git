package com.crm.system.service.encrypt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.encrypt.SensitiveFieldEncryptUtil;
import com.crm.system.domain.dao.SysFieldEncryptConfigDo;
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
import java.util.concurrent.atomic.AtomicReference;
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

        // 只查询状态为 0(待执行) 的配置
        queryWrapper.eq(SysFieldEncryptConfigDo::getStatus, "0");

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
            // 更新所有相关配置的状态为执行中（同时更新 create_time）
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

            // 更新所有配置的状态为已完成（同时更新 update_time 和 remark）
            updateConfigsStatus(configs, "2", (long) totalUpdated, totalCount, costTime);

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

            // 更新所有配置的状态为失败（同时更新 update_time 和 remark）
            updateConfigsStatus(configs, "3", null, null, costTime);

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
        // 用于跟踪线程异常
        AtomicReference<Exception> exceptionRef = new AtomicReference<>();

        try {
            // 先查询最小和最大主键值
            Map<String, Object> minMaxResult = getMinMaxPrimaryKey(tableName, primaryKey);
            if (minMaxResult == null || minMaxResult.get("minId") == null || minMaxResult.get("maxId") == null) {
                log.warn("表 [{}] 没有数据需要处理", tableName);
                return 0;
            }

            Object minId = minMaxResult.get("minId");
            Object maxId = minMaxResult.get("maxId");
            log.info("主键范围: [{}, {}]", minId, maxId);

            // 计算每个线程处理的主键范围
            long rangeSize = calculateRangeSize(minId, maxId, threadCount);
            log.info("数据分片：每线程处理约 {} 的主键范围", rangeSize);

            // 启动多个线程
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                final Object startId = calculateStartId(minId, rangeSize, i);
                final Object endId = calculateEndId(minId, maxId, rangeSize, i);

                log.info("线程 [{}] 分配范围: [{}, {}]", threadIndex, startId, endId);

                executorService.submit(() -> {
                    try {
                        log.info("线程 [{}] 开始处理，范围: [{}, {}]", threadIndex, startId, endId);

                        // 每个线程使用独立的 SqlSession，按主键范围处理
                        int updated = processChunkWithKeyRange(tableName, fieldNames, primaryKey, batchSize, startId, endId);

                        totalUpdated.addAndGet(updated);
                        log.info("线程 [{}] 处理完成，更新: {} 条", threadIndex, updated);
                    } catch (Exception e) {
                        log.error("线程 [{}] 处理失败", threadIndex, e);
                        // 记录第一个异常
                        exceptionRef.compareAndSet(null, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            latch.await();

            // 检查是否有线程异常
            Exception exception = exceptionRef.get();
            if (exception != null) {
                log.error("多线程处理中存在线程失败，已更新: {} 条", totalUpdated.get());
                throw new RuntimeException("多线程处理失败，部分线程异常", exception);
            }

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
     * 处理一个主键范围分片
     */
    private int processChunkWithKeyRange(String tableName, List<String> fieldNames,
                                          String primaryKey, int batchSize, Object startId, Object endId) {
        int totalUpdated = 0;
        int batchCommitSize = 1;
        int processedLimit = 0;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            // 构造 UPDATE SQL（只构造一次）
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

            // 获取 JDBC Connection 和 PreparedStatement（只创建一次）
            Connection connection = sqlSession.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                Object lastId = null; // 游标分页：初始为 null，查询第一条记录
                int batchIndex = 0;
                boolean hasMoreData = true;

                while (hasMoreData) {
                    long batchStartTime = System.currentTimeMillis();

                    // 查询批次数据（在主键范围内，使用游标分页）
                    long queryStartTime = System.currentTimeMillis();
                    log.debug("查询批次: {}, startId: {}, lastId: {}, endId: {}, limit: {}", batchIndex + 1, startId, lastId, endId, batchSize);
                    List<Map<String, Object>> records = queryBatchDataInRange(
                        tableName, fieldNames, primaryKey, startId, lastId, endId, batchSize
                    );
                    long queryCost = System.currentTimeMillis() - queryStartTime;

                    if (CollUtil.isEmpty(records)) {
                        log.info("线程分片处理完成，没有更多数据，batchIndex: {}", batchIndex);
                        break;
                    }

                    // 加密并添加到批处理（复用 PreparedStatement）
                    long encryptStartTime = System.currentTimeMillis();
                    int updated = encryptRecordsToBatch(ps, fieldNames, keys, records);
                    long encryptCost = System.currentTimeMillis() - encryptStartTime;

                    totalUpdated += updated;
                    processedLimit += records.size();

                    // 更新游标：记录本批次最后一个主键值
                    Object lastRecordId = records.get(records.size() - 1).get(keys[0].trim());
                    lastId = lastRecordId;

                    long batchCost = System.currentTimeMillis() - batchStartTime;
                    batchIndex++;
                    log.info("批次 [{}] 完成: 查询={}ms, 加密更新={}ms, 总耗时={}ms, 已处理={}条",
                        batchIndex, queryCost, encryptCost, batchCost, totalUpdated);

                    // 定期提交
                    if (batchIndex % batchCommitSize == 0) {
                        long commitStartTime = System.currentTimeMillis();
                        ps.executeBatch();
                        sqlSession.commit();
                        sqlSession.clearCache();
                        long commitCost = System.currentTimeMillis() - commitStartTime;
                        log.info("分片已提交批次: {}, 已处理: {} 条, 提交耗时: {}ms", batchIndex, totalUpdated, commitCost);
                    }

                    // 如果本批次数据少于 batchSize，说明已经是最后一批
                    if (records.size() < batchSize) {
                        hasMoreData = false;
                        log.info("检测到最后一批数据，records.size: {} < batchSize: {}", records.size(), batchSize);
                    }
                }

                // 提交剩余数据
                if (totalUpdated > 0) {
                    ps.executeBatch();
                    sqlSession.commit();
                    sqlSession.clearCache();
                    log.info("分片最终提交，总计处理: {} 条", totalUpdated);
                }
            } catch (SQLException e) {
                log.error("批量更新失败: {}", e.getMessage(), e);
                throw new RuntimeException("批量更新数据库失败", e);
            }
        }

        return totalUpdated;
    }

    /**
     * 查询指定主键范围内的批次数据
     */
    private List<Map<String, Object>> queryBatchDataInRange(String tableName, List<String> fieldNames,
                                                              String primaryKey, Object startId, Object lastId, Object endId, int limit) {
        // 构造查询字段
        StringBuilder selectFields = new StringBuilder(primaryKey);
        for (String field : fieldNames) {
            selectFields.append(", ").append(field);
        }

        try {
            return configMapper.selectBatchDataInRange(
                tableName, selectFields.toString(), primaryKey, startId, endId, lastId, limit
            );
        } catch (Exception e) {
            log.error("查询批次数据失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询主键的最小值和最大值
     */
    private Map<String, Object> getMinMaxPrimaryKey(String tableName, String primaryKey) {
        try {
            return configMapper.getMinMaxPrimaryKey(tableName, primaryKey);
        } catch (Exception e) {
            log.error("查询主键范围失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算主键范围大小
     */
    private long calculateRangeSize(Object minId, Object maxId, int threadCount) {
        if (minId instanceof Number && maxId instanceof Number) {
            long min = ((Number) minId).longValue();
            long max = ((Number) maxId).longValue();
            return (long) Math.ceil((double) (max - min + 1) / threadCount);
        }
        // 对于非数字类型的主键，返回一个默认值
        return 10000;
    }

    /**
     * 计算线程的起始ID
     */
    private Object calculateStartId(Object minId, long rangeSize, int threadIndex) {
        if (minId instanceof Number) {
            return ((Number) minId).longValue() + rangeSize * threadIndex;
        }
        return minId;
    }

    /**
     * 计算线程的结束ID
     */
    private Object calculateEndId(Object minId, Object maxId, long rangeSize, int threadIndex) {
        if (minId instanceof Number && maxId instanceof Number) {
            long calculated = ((Number) minId).longValue() + rangeSize * (threadIndex + 1) - 1;
            return Math.min(calculated, ((Number) maxId).longValue());
        }
        return maxId;
    }

    /**
     * 比较两个ID的大小
     * @return -1: id1 < id2, 0: id1 == id2, 1: id1 > id2
     */
    private int compareIds(Object id1, Object id2) {
        if (id1 == null && id2 == null) return 0;
        if (id1 == null) return -1;
        if (id2 == null) return 1;

        if (id1 instanceof Comparable && id1.getClass().equals(id2.getClass())) {
            return ((Comparable) id1).compareTo(id2);
        }

        // 默认转为字符串比较
        return id1.toString().compareTo(id2.toString());
    }

    /**
     * 处理一个数据分片（使用 SqlSession BATCH 模式）
     */
    private int processChunkWithSqlSession(String tableName, List<String> fieldNames,
                                            String primaryKey, int batchSize, long startOffset, int totalLimit) {
        int totalUpdated = 0;
        int batchCommitSize = 1;
        int processedLimit = 0;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            long totalBatches = (long) Math.ceil((double) totalLimit / batchSize);
            log.info("分片批次数: {}, 每 {} 批次提交一次", totalBatches, batchCommitSize);

            // 构造 UPDATE SQL（只构造一次）
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

            // 获取 JDBC Connection 和 PreparedStatement（只创建一次）
            Connection connection = sqlSession.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                Object lastId = null; // 游标分页：上一批次的最后一个主键值

                for (long i = 0; i < totalBatches; i++) {
                    long batchStartTime = System.currentTimeMillis();
                    int currentBatchSize = Math.min(batchSize, totalLimit - processedLimit);

                    if (currentBatchSize <= 0) {
                        break;
                    }

                    // 查询批次数据（使用游标分页）
                    long queryStartTime = System.currentTimeMillis();
                    log.debug("查询批次: {}, lastId: {}, limit: {}", i + 1, lastId, currentBatchSize);
                    List<Map<String, Object>> records = queryBatchDataForMultipleFields(
                        tableName, fieldNames, primaryKey, lastId, currentBatchSize
                    );
                    long queryCost = System.currentTimeMillis() - queryStartTime;

                    if (CollUtil.isEmpty(records)) {
                        break;
                    }

                    // 加密并添加到批处理（复用 PreparedStatement）
                    long encryptStartTime = System.currentTimeMillis();
                    int updated = encryptRecordsToBatch(ps, fieldNames, keys, records);
                    long encryptCost = System.currentTimeMillis() - encryptStartTime;

                    totalUpdated += updated;
                    processedLimit += records.size();

                    // 更新游标：记录本批次最后一个主键值
                    Object lastRecordId = records.get(records.size() - 1).get(keys[0].trim());
                    lastId = lastRecordId;

                    long batchCost = System.currentTimeMillis() - batchStartTime;
                    log.info("批次 [{}/{}] 完成: 查询={}ms, 加密更新={}ms, 总耗时={}ms, 已处理={}条",
                        i + 1, totalBatches, queryCost, encryptCost, batchCost, totalUpdated);

                    // 定期提交（更频繁地提交，避免连接超时）
                    if ((i + 1) % batchCommitSize == 0 || i == totalBatches - 1) {
                        long commitStartTime = System.currentTimeMillis();
                        // 先执行批处理
                        ps.executeBatch();
                        // 再提交事务
                        sqlSession.commit();
                        sqlSession.clearCache();
                        long commitCost = System.currentTimeMillis() - commitStartTime;
                        log.info("分片已提交批次: {}/{}, 已处理: {} 条, 提交耗时: {}ms", i + 1, totalBatches, totalUpdated, commitCost);
                    }
                }
            } catch (SQLException e) {
                log.error("批量更新失败: {}", e.getMessage(), e);
                throw new RuntimeException("批量更新数据库失败", e);
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
        int batchCommitSize = 5; // 每 5 个批次提交一次
        int processedLimit = 0;

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            long totalBatches = (long) Math.ceil((double) totalCount / batchSize);
            log.info("总批次数: {}, 每 {} 批次提交一次", totalBatches, batchCommitSize);

            // 构造 UPDATE SQL（只构造一次）
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

            // 获取 JDBC Connection 和 PreparedStatement（只创建一次）
            Connection connection = sqlSession.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                Object lastId = null; // 游标分页：上一批次的最后一个主键值

                for (long i = 0; i < totalBatches; i++) {
                    int currentBatchSize = Math.min(batchSize, (int) (totalCount - processedLimit));

                    if (currentBatchSize <= 0) {
                        break;
                    }

                    // 查询批次数据（使用游标分页）
                    List<Map<String, Object>> records = queryBatchDataForMultipleFields(
                        tableName, fieldNames, primaryKey, lastId, currentBatchSize
                    );

                    if (CollUtil.isEmpty(records)) {
                        continue;
                    }

                    // 加密并添加到批处理（复用 PreparedStatement）
                    int updated = encryptRecordsToBatch(ps, fieldNames, keys, records);
                    totalUpdated += updated;
                    processedLimit += records.size();

                    // 更新游标：记录本批次最后一个主键值
                    Object lastRecordId = records.get(records.size() - 1).get(keys[0].trim());
                    lastId = lastRecordId;

                    // 每 batchCommitSize 个批次提交一次
                    if ((i + 1) % batchCommitSize == 0 || i == totalBatches - 1) {
                        // 先执行批处理
                        ps.executeBatch();
                        // 再提交事务
                        sqlSession.commit();
                        sqlSession.clearCache();
                        log.info("已提交批次: {}/{}, 已处理: {} 条", i + 1, totalBatches, totalUpdated);
                    }
                }
            } catch (SQLException e) {
                log.error("批量更新失败: {}", e.getMessage(), e);
                throw new RuntimeException("批量更新数据库失败", e);
            }
        }

        return totalUpdated;
    }

    /**
     * 加密记录并添加到批处理（复用 PreparedStatement）
     */
    private int encryptRecordsToBatch(PreparedStatement ps, List<String> fieldNames,
                                       String[] keys, List<Map<String, Object>> records) throws SQLException {
        int updatedCount = 0;

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

                // 直接加密（不再判断是否已加密）
                try {
                    String encryptedValue = SensitiveFieldEncryptUtil.encrypt(originalValue);
                    params.add(encryptedValue);
                    hasUpdate = true;
                } catch (Exception e) {
                    log.error("加密失败 - 表: {}, 字段: {}, 主键: {}, 原始值: {}, 错误: {}",
                        record.get(keys[0].trim()), fieldName, record.get(keys[0].trim()), originalValue, e.getMessage());
                    throw new RuntimeException(
                        String.format("加密失败 - 字段: %s", fieldName), e);
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

        return updatedCount;
    }

    /**
     * 查询多字段的批次数据（使用游标分页，提升查询性能）
     */
    private List<Map<String, Object>> queryBatchDataForMultipleFields(String tableName, List<String> fieldNames,
                                                                        String primaryKey, Object lastId, int limit) {
        // 构造查询字段
        StringBuilder selectFields = new StringBuilder(primaryKey);
        for (String field : fieldNames) {
            selectFields.append(", ").append(field);
        }

        try {
            return configMapper.selectBatchDataForMultipleFieldsWithoutWhere(
                tableName, selectFields.toString(), primaryKey, limit, lastId
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
        try {
            Long count = configMapper.countTotalDataWithoutWhere(tableName);
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
        updateConfigsStatus(configs, status, null, null, null);
    }

    /**
     * 批量更新配置状态和计数（同时更新时间字段）
     */
    private void updateConfigsStatus(List<SysFieldEncryptConfigVo> configs, String status,
                                      Long processedCount, Long totalCount) {
        updateConfigsStatus(configs, status, processedCount, totalCount, null);
    }

    /**
     * 批量更新配置状态、计数和执行时长（同时更新时间字段和remark）
     */
    private void updateConfigsStatus(List<SysFieldEncryptConfigVo> configs, String status,
                                      Long processedCount, Long totalCount, Long costTime) {
        Date now = new Date();
        for (SysFieldEncryptConfigVo config : configs) {
            LambdaUpdateWrapper<SysFieldEncryptConfigDo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysFieldEncryptConfigDo::getId, config.getId());
            updateWrapper.set(SysFieldEncryptConfigDo::getStatus, status);

            // 状态为 1(执行中) 时，更新 create_time
            if ("1".equals(status)) {
                updateWrapper.set(SysFieldEncryptConfigDo::getCreateTime, now);
            }
            // 状态为 2(已完成) 或 3(失败) 时，更新 update_time 和 remark
            else if ("2".equals(status) || "3".equals(status)) {
                updateWrapper.set(SysFieldEncryptConfigDo::getUpdateTime, now);
                // 将执行时长（毫秒）更新到 remark 字段
                if (costTime != null) {
                    updateWrapper.set(SysFieldEncryptConfigDo::getRemark, costTime + "ms");
                }
            }

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
