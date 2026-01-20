package com.crm.job.service.sync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.GrpCustInuse;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.domain.grp.GrpGroupInfoDo; // 使用MySQL数据源的实体类
import com.crm.system.mapper.GrpCustInuseMapper; // 使用现有的Oracle数据源Mapper
import com.crm.system.mapper.ZqztGrpCustInuseMapper; // 使用现有的主数据源Mapper
import com.crm.system.mapper.grp.GrpGroupInfoMapper; // 使用MySQL数据源的Mapper
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
    private GrpGroupInfoMapper grpGroupInfoMapper; // 用于查询MySQL数据源的grp_group_info表

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

        // 3. HQ_GRP_CODE_A - 通过GRP_ID_A查询MySQL数据源的grp_group_info表的grp_code字段
        if (oracleData.getGrpIdA() != null) {
            String hqGrpCodeA = queryGrpCodeByGrpId(Long.valueOf(oracleData.getGrpIdA()));
            targetData.setHqGrpCodeA(hqGrpCodeA);
        }

        // 4. HQ_GRP_CODE_B - 通过GRP_ID_B查询MySQL数据源的grp_group_info表的grp_code字段
        if (oracleData.getGrpIdB() != null) {
            String hqGrpCodeB = queryGrpCodeByGrpId(Long.valueOf(oracleData.getGrpIdB()));
            targetData.setHqGrpCodeB(hqGrpCodeB);
        }

        return targetData;
    }

    /**
     * 根据GRP_ID查询grp_code字段值
     */
    private String queryGrpCodeByGrpId(Long grpId) {
        try {
            // 使用MySQL主数据源查询grp_group_info表
            LambdaQueryWrapper<GrpGroupInfoDo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GrpGroupInfoDo::getGrpId, grpId);

            GrpGroupInfoDo groupInfo = grpGroupInfoMapper.selectOne(queryWrapper);
            if (groupInfo != null) {
                return groupInfo.getGrpCode(); // 返回grp_code字段值
            }
        } catch (Exception e) {
            log.warn("查询grp_group_info表时发生错误，grpId: {}, 错误: {}", grpId, e.getMessage());
        }

        // 如果没找到，返回空字符串或根据业务需求返回默认值
        return "";
    }
}
