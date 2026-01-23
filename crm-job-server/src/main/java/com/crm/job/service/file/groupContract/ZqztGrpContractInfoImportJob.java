package com.crm.job.service.file.groupContract;

import com.crm.system.domain.GrpContractInfo;
import com.crm.system.domain.ZqztGrpContractInfo;
import com.crm.system.mapper.GrpContractInfoMapper;
import com.crm.system.mapper.ZqztGrpContractInfoMapper;
import com.crm.system.service.IZqztGrpContractInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 集团合同信息表导入定时任务<br>
 * 从Oracle库的grp_contract_info表查询数据，然后添加到MySQL主数据源的zqzt_grp_contract_info表
 *
 * @author 万超凡
 */
@Slf4j
@Component
public class ZqztGrpContractInfoImportJob {

    @Resource
    private IZqztGrpContractInfoService zqztGrpContractInfoService;

    @Resource
    private GrpContractInfoMapper grpContractInfoMapper; // Oracle数据源的Mapper

    @Resource
    private ZqztGrpContractInfoMapper zqztGrpContractInfoMapper; // MySQL数据源的Mapper

    /**
     * 集团合同信息导入任务<br>
     * 在xxl-job调度中心配置JobHandler为：zqztGrpContractInfoImportJob
     */
    @XxlJob("zqztGrpContractInfoImportJob")
    public void execute() {
        log.info("开始执行集团合同信息导入任务");

        try {
            // 1. 清空目标表数据
            XxlJobHelper.log("清空目标表zqzt_grp_contract_info数据");
            zqztGrpContractInfoMapper.delete(null);

            // 2. 从Oracle数据源查询grp_contract_info表的所有数据
            XxlJobHelper.log("从Oracle数据源查询grp_contract_info表数据");
            List<GrpContractInfo> oracleDataList = grpContractInfoMapper.selectList(null);
            XxlJobHelper.log("查询到Oracle数据条数：" + oracleDataList.size());

            // 3. 遍历数据，转换并插入到主数据源的zqzt_grp_contract_info表
            int successCount = 0;
            int totalCount = oracleDataList.size();

            for (int i = 0; i < oracleDataList.size(); i++) {
                GrpContractInfo oracleData = oracleDataList.get(i);

                try {
                    // 转换数据并填充新增字段
                    ZqztGrpContractInfo targetData = convertToTargetData(oracleData);

                    // 插入到目标表
                    zqztGrpContractInfoMapper.insert(targetData);
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

            String successMsg = String.format("集团合同信息导入任务执行成功，导入记录数：%d", successCount);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            String errorMsg = "集团合同信息导入任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 将Oracle数据转换为目标数据，并填充新增字段
     */
    private ZqztGrpContractInfo convertToTargetData(GrpContractInfo oracleData) {
        ZqztGrpContractInfo targetData = new ZqztGrpContractInfo();

        // 复制基础字段
        BeanUtils.copyProperties(oracleData, targetData);

        // 设置更新时间字段
        targetData.setUpdateTime(new Date());

        return targetData;
    }
}
