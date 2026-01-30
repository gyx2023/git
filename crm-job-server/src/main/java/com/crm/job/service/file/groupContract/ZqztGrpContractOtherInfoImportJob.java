package com.crm.job.service.file.groupContract;

import com.crm.system.domain.GrpContractOtherInfo;
import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.service.IGrpContractOtherInfoService;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 集团合同扩展信息表导入定时任务<br>
 * 从Oracle库的grp_contract_other_info表查询数据，然后添加到MySQL主数据源的zqzt_grp_contract_other_info表<br>
 * 入库逻辑：根据infoVal作为user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录，如存在则正常入库（状态=1有效），同时写入服务号码；否则入库时状态默认=99（迁移失效）<br>
 *
 * @author 万超凡
 */
@Slf4j
@Component
public class ZqztGrpContractOtherInfoImportJob {

    @Resource
    private IZqztGrpContractOtherInfoService zqztGrpContractOtherInfoService;

    @Resource
    private IGrpContractOtherInfoService grpContractOtherInfoService; // Oracle数据源的服务

    /**
     * 集团合同扩展信息导入任务<br>
     * 在xxl-job调度中心配置Job为：zqztGrpContractOtherInfoImportJob
     */
    @XxlJob("zqztGrpContractOtherInfoImportJob")
    public void execute() {
        log.info("开始执行集团合同扩展信息导入任务");
        XxlJobHelper.log("开始执行集团合同扩展信息导入任务");

        try {
            // 1. 清空目标表数据
            XxlJobHelper.log("清空目标表zqzt_grp_contract_other_info数据");
            zqztGrpContractOtherInfoService.importAllClear(null);

            // 2. 查询grp_contract_other_info表的所有数据
            XxlJobHelper.log("查询grp_contract_other_info表数据");
            List<GrpContractOtherInfo> oracleDataList = grpContractOtherInfoService.selectAllGrpContractOtherInfo();
            XxlJobHelper.log("查询到数据条数：" + oracleDataList.size());

            if (oracleDataList == null || oracleDataList.isEmpty()) {
                String warnMsg = "数据源数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 3. 遍历数据，转换并插入到主数据源的zqzt_grp_contract_other_info表
            int successCount = 0;
            int totalCount = oracleDataList.size();

            // 转换数据列表
            List<ZqztGrpContractOtherInfo> targetDataList = convertToTargetDataList(oracleDataList);

            // 批量插入数据
            if (!targetDataList.isEmpty()) {
                XxlJobHelper.log("开始批量插入数据，数量：" + targetDataList.size());
                successCount = zqztGrpContractOtherInfoService.importAll(targetDataList);
            }

            XxlJobHelper.log("数据同步完成，总计处理: " + totalCount + " 条，成功: " + successCount + " 条");
            log.info("数据同步完成，总计处理: {} 条，成功: {} 条", totalCount, successCount);

            String successMsg = String.format("集团合同扩展信息导入任务执行成功，导入记录数：%d", successCount);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            String errorMsg = "集团合同扩展信息导入任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 将Oracle数据转换为目标数据列表，并应用业务逻辑
     */
    private List<ZqztGrpContractOtherInfo> convertToTargetDataList(List<GrpContractOtherInfo> oracleDataList) {
        return oracleDataList.stream()
            .map(this::convertSingleRecord)
            .collect(Collectors.toList());
    }

    /**
     * 转换单条记录
     */
    private ZqztGrpContractOtherInfo convertSingleRecord(GrpContractOtherInfo oracleData) {
        ZqztGrpContractOtherInfo targetData = new ZqztGrpContractOtherInfo();

        // 复制基础字段
        targetData.setContractId(oracleData.getContractId());
        targetData.setInfoTag(oracleData.getInfoTag());
        targetData.setInfoVal(oracleData.getInfoVal());
        targetData.setInfoVal2(oracleData.getInfoVal2());

        return targetData;
    }
}
