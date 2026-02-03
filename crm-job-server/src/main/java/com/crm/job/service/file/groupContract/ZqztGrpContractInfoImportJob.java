package com.crm.job.service.file.groupContract;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    private GrpContractInfoMapper grpContractInfoMapper;

    @Resource
    private ZqztGrpContractInfoMapper zqztGrpContractInfoMapper;

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

            // 2. 分页查询Oracle数据源grp_contract_info表的数据
            int pageSize = 1000;
            long currentPage = 0;
            int totalProcessed = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                Page<GrpContractInfo> page = new Page<>(currentPage + 1, pageSize);
                Page<GrpContractInfo> resultPage = grpContractInfoMapper.selectPage(page, Wrappers.emptyWrapper());

                List<GrpContractInfo> oracleDataList = resultPage.getRecords();

                if (oracleDataList == null || oracleDataList.isEmpty()) {
                    break;
                }

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
                        totalProcessed++;

                        // 每100条记录输出一次进度
                        if ((i + 1) % 100 == 0) {
                            XxlJobHelper.log("已处理第" + (currentPage + 1) + "页：" + (i + 1) + "/" + totalCount + " 条记录");
                        }
                    } catch (Exception e) {
                        XxlJobHelper.log("处理第 " + (i + 1) + " 条记录时发生错误: " + e.getMessage());
                        log.error("处理数据时发生错误", e);
                    }
                }

                XxlJobHelper.log("第" + (currentPage + 1) + "页数据处理完成，成功: " + successCount + " 条");

                currentPage++;

                // 检查是否还有更多数据
                if (oracleDataList.size() < pageSize) {
                    hasMoreData = false;
                }
            }

            XxlJobHelper.log("数据同步完成，总计处理: " + totalProcessed + " 条，成功: " + totalProcessed + " 条");
            log.info("数据同步完成，总计处理: {} 条，成功: {} 条", totalProcessed, totalProcessed);

            String successMsg = String.format("集团合同信息导入任务执行成功，导入记录数：%d", totalProcessed);
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

        // 特殊字段映射
        targetData.setErpContractId(oracleData.getString2());
        targetData.setInuseContractId(oracleData.getString15());

        return targetData;
    }
}
