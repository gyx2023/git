package com.crm.job.service.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.GrpCustInuseUserlist;
import com.crm.system.domain.ZqztGrpCustInuseUserlist;
import com.crm.system.domain.SysArea; // 使用sys_area表的实体类
import com.crm.system.mapper.GrpCustInuseUserlistMapper;
import com.crm.system.mapper.ZqztGrpCustInuseUserlistMapper;
import com.crm.system.mapper.SysAreaMapper; // 使用sys_area表的Mapper
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 政企实际使用客户类型用户清单数据同步任务
 * 从Oracle库的grp_cust_inuse_userlist表查询数据，然后添加到MySQL主数据源的zqzt_grp_cust_inuse_userlist表
 * 
 * @author crm
 * @date 2026-01-20
 */
@Slf4j
@Component
public class GrpCustInuseUserlistSyncJob {

    @Resource
    private GrpCustInuseUserlistMapper grpCustInuseUserlistMapper; // Oracle数据源的Mapper

    @Resource
    private ZqztGrpCustInuseUserlistMapper zqztGrpCustInuseUserlistMapper; // 主数据源的Mapper

    @Resource
    private SysAreaMapper sysAreaMapper; // 用于查询sys_area表

    /**
     * 同步任务入口
     * 从Oracle数据源查询grp_cust_inuse_userlist表数据，然后添加到MySQL主数据源的zqzt_grp_cust_inuse_userlist表
     * 并填充新增字段：update_time(当前时间)、is_valid(默认为1)
     */
    @XxlJob("grpCustInuseUserlistSyncJobHandler")
    public void syncGrpCustInuseUserlistData() {
        log.info("开始执行政企实际使用客户类型用户清单数据同步任务");

        try {
            // 1. 清空目标表数据（可选，根据业务需求决定）
            XxlJobHelper.log("清空目标表zqzt_grp_cust_inuse_userlist数据");
            zqztGrpCustInuseUserlistMapper.delete(null);

            // 2. 从Oracle数据源查询grp_cust_inuse_userlist表的所有数据
            XxlJobHelper.log("从Oracle数据源查询grp_cust_inuse_userlist表数据");
            List<GrpCustInuseUserlist> oracleDataList = grpCustInuseUserlistMapper.selectList(null);
            XxlJobHelper.log("查询到Oracle数据条数：" + oracleDataList.size());

            // 3. 遍历数据，转换并插入到主数据源的zqzt_grp_cust_inuse_userlist表
            int successCount = 0;
            int totalCount = oracleDataList.size();

            for (int i = 0; i < oracleDataList.size(); i++) {
                GrpCustInuseUserlist oracleData = oracleDataList.get(i);

                try {
                    // 转换数据并填充新增字段
                    ZqztGrpCustInuseUserlist targetData = convertToTargetData(oracleData);

                    // 插入到目标表
                    zqztGrpCustInuseUserlistMapper.insert(targetData);
                    successCount++;

                    // 每100条记录输出一次进度
                    if ((i + 1) % 100 == 0) {
                        XxlJobHelper.log("已处理：" + (i + 1) + "/" + totalCount + " 条记录");
                    }
                } catch (Exception e) {
                    XxlJobHelper.log("处理第 " + (i + 1) + " 条记录时发生错误: " + e.getMessage());
                    log.error("处理数据时发生错误", e);
                }
            }

            XxlJobHelper.log("数据同步完成，总计处理: " + totalCount + " 条，成功: " + successCount + " 条");
            log.info("数据同步完成，总计处理: {} 条，成功: {} 条", totalCount, successCount);

        } catch (Exception e) {
            log.error("政企实际使用客户类型用户清单数据同步任务执行失败", e);
            XxlJobHelper.log("政企实际使用客户类型用户清单数据同步任务执行失败: " + e.getMessage());
            XxlJobHelper.handleFail("任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 将Oracle数据转换为目标数据，并填充新增字段
     */
    private ZqztGrpCustInuseUserlist convertToTargetData(GrpCustInuseUserlist oracleData) {
        ZqztGrpCustInuseUserlist targetData = new ZqztGrpCustInuseUserlist();

        // 复制基础字段
        BeanUtils.copyProperties(oracleData, targetData);

        // 设置新增字段
        // 1. update_time - 当前时间
        targetData.setUpdateTime(new Date());

        // 2. is_valid - 默认为1（有效）
        targetData.setIsValid("1");

        // 3. region_id - 通过region_id值查询sys_area表的bss_code进行匹配，获取name_字段值
        if (targetData.getRegionId() != null && !targetData.getRegionId().isEmpty()) {
            String regionName = queryAreaNameByBssCode(targetData.getRegionId());
            targetData.setRegionId(regionName); // 将name_字段值写入region_id字段
        }

        // 4. county_id - 通过county_id值查询sys_area表的bss_code进行匹配，获取name_字段值
        if (targetData.getCountyId() != null && !targetData.getCountyId().isEmpty()) {
            String countyName = queryAreaNameByBssCode(targetData.getCountyId());
            targetData.setCountyId(countyName); // 将name_字段值写入county_id字段
        }

        return targetData;
    }

    /**
     * 根据BSS_CODE查询sys_area表的name_字段值
     */
    private String queryAreaNameByBssCode(String bssCode) {
        try {
            // 使用MySQL主数据源查询sys_area表
            LambdaQueryWrapper<SysArea> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysArea::getBssCode, bssCode);

            SysArea sysArea = sysAreaMapper.selectOne(queryWrapper);
            if (sysArea != null) {
                return sysArea.getName(); // 返回name_字段值
            }
        } catch (Exception e) {
            log.warn("查询sys_area表时发生错误，bssCode: {}, 错误: {}", bssCode, e.getMessage());
        }

        // 如果没找到，返回原值或根据业务需求返回默认值
        return bssCode;
    }
}