package com.crm.system.service.stats;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

/**
 * 统计服务接口（升级预警专用）
 * <p>
 * 迁移自老系统 StatsService，提供宽带升级预警和红名单升级预警的统计查询功能
 * </p>
 *
 * @author crm
 * @date 2026-06-16
 */
public interface IStatsService {

    /**
     * 构建宽带升级预警客户列表查询SQL
     *
     * @param codeInfo   BSS编码或MDM组织编码
     * @param period     账期，格式：yyyy-MM
     * @param orderFlag  订单标识：1=区县，2=市本级
     * @return SQL语句
     */
    String buildWideCustInfoStatsSql(String codeInfo, String period, String orderFlag);

    /**
     * 查询宽带升级预警客户列表
     *
     * @param sql      SQL语句
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<Map<String, Object>> queryWideCustInfoStats(String sql, Integer pageNum, Integer pageSize);

    /**
     * 获取宽带升级预警话术统计数据
     *
     * @param codeInfo   BSS编码或MDM组织编码
     * @param period     账期，格式：yyyy-MM
     * @param orderFlag  订单标识：1=区县，2=市本级
     * @return 话术统计数据
     */
    Map<String, Object> getWideUpSpeechcraftStats(String codeInfo, String period, String orderFlag);

    /**
     * 构建红名单升级预警客户列表查询SQL
     *
     * @param codeInfo   BSS编码或MDM组织编码
     * @param period     账期，格式：yyyy-MM
     * @param orderFlag  订单标识：1=区县，2=市本级
     * @return SQL语句
     */
    String buildHmdWarnCustSql(String codeInfo, String period, String orderFlag);

    /**
     * 查询红名单升级预警客户列表
     *
     * @param sql      SQL语句
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<Map<String, Object>> queryHmdWarnCust(String sql, Integer pageNum, Integer pageSize);

    /**
     * 获取红名单升级预警话术统计数据
     *
     * @param codeInfo   BSS编码或MDM组织编码
     * @param period     账期，格式：yyyy-MM
     * @param orderFlag  订单标识：1=区县，2=市本级
     * @return 话术统计数据
     */
    Map<String, Object> getHmdUpSpeechcraftStats(String codeInfo, String period, String orderFlag);

    /**
     * 根据codeInfo和orderFlag获取区域名称
     *
     * @param codeInfo   BSS编码或MDM组织编码
     * @param orderFlag  订单标识：1=区县，2=市本级
     * @return 区域名称
     */
    String getCodeName(String codeInfo, String orderFlag);
}
