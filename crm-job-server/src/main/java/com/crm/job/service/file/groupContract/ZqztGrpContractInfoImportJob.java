package com.crm.job.service.file.groupContract;

import com.crm.system.domain.ZqztGrpContractInfo;
import com.crm.system.service.IZqztGrpContractInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 集团合同信息表导入定时任务<br>
 * 从经分侧获取全量数据并导入到zqzt_grp_contract_info表
 *
 * @author 万超凡
 */
@Slf4j
@Component
public class ZqztGrpContractInfoImportJob {

    @Resource
    private IZqztGrpContractInfoService zqztGrpContractInfoService;

    /**
     * 集团合同信息导入任务<br>
     * 在xxl-job调度中心配置JobHandler为：zqztGrpContractInfoImportJob
     */
    @XxlJob("zqztGrpContractInfoImportJob")
    public void execute() {
        log.info("开始执行集团合同信息导入任务");
        XxlJobHelper.log("开始执行集团合同信息导入任务");

        try {
            // 从经分侧获取全量集团合同信息数据
            // TODO: 等待经分提供详细接口规范后实现此方法
            List<ZqztGrpContractInfo> dataList = getGroupContractInfoFromJf();

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "集团合同信息导入数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            log.info("开始批量导入集团合同信息，数据量: {}", dataList.size());

            // 执行全量数据入库操作
            int insertCount = zqztGrpContractInfoService.importAllGroupContractInfo(dataList);

            String successMsg = String.format("集团合同信息导入任务执行成功，导入记录数：%d", insertCount);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            String errorMsg = "集团合同信息导入任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 从经分侧获取集团合同信息全量数据<br>
     * TODO: 等待经分提供详细接口规范后实现此方法
     *
     * @return 集团合同信息列表
     */
    private List<ZqztGrpContractInfo> getGroupContractInfoFromJf() {
        // TODO: 实现调用经分接口获取数据的逻辑
        // 这里暂时返回空列表，等待经分接口提供后完善
        log.info("调用经分接口获取集团合同信息数据");

        // 示例：调用经分接口的伪代码，等待实际接口后替换
        // return jfApiService.getFullGroupContractInfo();

        return null; // 暂时返回null，实际实现时返回从经分获取的数据
    }
}
