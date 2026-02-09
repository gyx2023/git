package com.crm.job.service.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.GrpCustInuse;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.domain.ZqGrpGroupInfo; // 使用zqzt_grp_group_info表的实体类
import com.crm.system.domain.SysArea; // 使用sys_area表的实体类
import com.crm.system.mapper.GrpCustInuseMapper; // 使用现有的Oracle数据源Mapper
import com.crm.system.mapper.ZqztGrpCustInuseMapper; // 使用现有的主数据源Mapper
import com.crm.system.mapper.ZqGrpGroupInfoMapper; // 使用zqzt_grp_group_info表的Mapper
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
 * 政企实际使用客户数据同步任务
 * 从Oracle库的grp_cust_inuse表查询数据，然后添加到MySQL主数据源的zqzt_grp_cust_inuse表
 *
 * @author crm
 * @date 2026-01-20
 */
@Slf4j
@Component
public class GrpCustInuseSyncJob {

    @Resource
    private GrpCustInuseMapper grpCustInuseMapper; // Oracle数据源的Mapper（使用现有的）

    @Resource
    private ZqztGrpCustInuseMapper zqztGrpCustInuseMapper; // 主数据源的Mapper（使用现有的）

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper; // 用于查询zqzt_grp_group_info表

    @Resource
    private SysAreaMapper sysAreaMapper; // 用于查询sys_area表

    /**
     * 同步任务入口
     * 从Oracle数据源查询grp_cust_inuse表数据，然后添加到MySQL主数据源的zqzt_grp_cust_inuse表
     * 并填充新增字段：updatetime(当前时间)、is_valid(默认为1)、HQ_GRP_CODE_A(通过GRP_ID_A查询)、HQ_GRP_CODE_B(通过GRP_ID_B查询)
     */
    @XxlJob("grpCustInuseSyncJobHandler")
    public void syncGrpCustInuseData() {
        log.info("开始执行政企实际使用客户数据同步任务");

        try {
            // 1. 清空目标表数据（可选，根据业务需求决定）
            XxlJobHelper.log("清空目标表zqzt_grp_cust_inuse数据");
            zqztGrpCustInuseMapper.delete(null);

            // 2. 从Oracle数据源查询grp_cust_inuse表的所有数据
            XxlJobHelper.log("从Oracle数据源查询grp_cust_inuse表数据");
            List<GrpCustInuse> oracleDataList = grpCustInuseMapper.selectList(null);
            XxlJobHelper.log("查询到Oracle数据条数：" + oracleDataList.size());

            // 3. 遍历数据，转换并插入到主数据源的zqzt_grp_cust_inuse表
            int successCount = 0;
            int totalCount = oracleDataList.size();

            for (int i = 0; i < oracleDataList.size(); i++) {
                GrpCustInuse oracleData = oracleDataList.get(i);

                try {
                    // 转换数据并填充新增字段
                    ZqztGrpCustInuse targetData = convertToTargetData(oracleData);

                    // 插入到目标表
                    zqztGrpCustInuseMapper.insert(targetData);
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
            log.error("政企实际使用客户数据同步任务执行失败", e);
            XxlJobHelper.log("政企实际使用客户数据同步任务执行失败: " + e.getMessage());
            XxlJobHelper.handleFail("任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 将Oracle数据转换为目标数据，并填充新增字段
     */
    private ZqztGrpCustInuse convertToTargetData(GrpCustInuse oracleData) {
        ZqztGrpCustInuse targetData = new ZqztGrpCustInuse();

        // 复制基础字段
        BeanUtils.copyProperties(oracleData, targetData);

        // 设置新增字段
        // 1. updatetime - 当前时间
        targetData.setUpdateTime(new Date());

        // 2. is_valid - 默认为1（有效）
        targetData.setIsValid("1");

        // 3. HQ_GRP_CODE_A - 通过GRP_ID_A查询zqzt_grp_group_info表的HQ_GRP_CODE字段
        if (oracleData.getGrpIdA() != null) {
            String hqGrpCodeA = queryHqGrpCodeByGrpId(Long.valueOf(oracleData.getGrpIdA()));
            targetData.setHqGrpCodeA(hqGrpCodeA);
        }

        // 4. HQ_GRP_CODE_B - 通过GRP_ID_B查询zqzt_grp_group_info表的HQ_GRP_CODE字段
        if (oracleData.getGrpIdB() != null) {
            String hqGrpCodeB = queryHqGrpCodeByGrpId(Long.valueOf(oracleData.getGrpIdB()));
            targetData.setHqGrpCodeB(hqGrpCodeB);
        }

        // 5. region_id - 通过region_id值查询sys_area表的bss_code进行匹配，获取id_字段值
        if (targetData.getRegionId() != null && !targetData.getRegionId().isEmpty()) {
            String regionId = queryAreaIdByBssCode(targetData.getRegionId());
            targetData.setRegionId(regionId); // 将id_字段值写入region_id字段
        }

        // 6. county_id - 通过county_id值查询sys_area表的bss_code进行匹配，获取id_字段值
        if (targetData.getCountyId() != null && !targetData.getCountyId().isEmpty()) {
            String countyId = queryAreaIdByBssCode(targetData.getCountyId());
            targetData.setCountyId(countyId); // 将id_字段值写入county_id字段
        }

        return targetData;
    }

    /**
     * 根据GRP_ID查询zqzt_grp_group_info表的HQ_GRP_CODE字段值
     */
    private String queryHqGrpCodeByGrpId(Long grpId) {
        try {
            // 使用MySQL主数据源查询zqzt_grp_group_info表
            LambdaQueryWrapper<ZqGrpGroupInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ZqGrpGroupInfo::getGrpId, grpId);

            ZqGrpGroupInfo groupInfo = zqGrpGroupInfoMapper.selectOne(queryWrapper);
            if (groupInfo != null) {
                return groupInfo.getHqGrpCode(); // 返回HQ_GRP_CODE字段值
            }
        } catch (Exception e) {
            log.warn("查询zqzt_grp_group_info表时发生错误，grpId: {}, 错误: {}", grpId, e.getMessage());
        }

        // 如果没找到，返回空字符串或根据业务需求返回默认值
        return "";
    }

    /**
     * 根据BSS_CODE查询sys_area表的id_字段值
     */
    private String queryAreaIdByBssCode(String bssCode) {
        try {
            // 使用MySQL主数据源查询sys_area表
            LambdaQueryWrapper<SysArea> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysArea::getBssCode, bssCode);

            SysArea sysArea = sysAreaMapper.selectOne(queryWrapper);
            if (sysArea != null) {
                return String.valueOf(sysArea.getId()); // 返回id_字段值
            }
        } catch (Exception e) {
            log.warn("查询sys_area表时发生错误，bssCode: {}, 错误: {}", bssCode, e.getMessage());
        }

        // 如果没找到，返回原值或根据业务需求返回默认值
        return bssCode;
    }
}
