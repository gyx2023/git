package com.crm.job.service.file.groupContract;

import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 集团合同扩展信息表导入定时任务<br>
 * 从经分侧获取全量数据并导入到zqzt_grp_contract_other_info表<br>
 * 入库逻辑：根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录，<br>
 * 如存在则正常入库（状态=1有效），同时写入服务号码；否则入库时状态默认=99（迁移失效）<br>
 *
 * @author 万超凡
 */
@Slf4j
@Component
public class ZqztGrpContractOtherInfoImportJob {

    @Resource
    private IZqztGrpContractOtherInfoService zqztGrpContractOtherInfoService;

    /**
     * 集团合同扩展信息导入任务<br>
     * 在xxl-job调度中心配置Job为：zqztGrpContractOtherInfoImportJob
     */
    @XxlJob("zqztGrpContractOtherInfoImportJob")
    public void execute() {
        log.info("开始执行集团合同扩展信息导入任务");
        XxlJobHelper.log("开始执行集团合同扩展信息导入任务");

        try {
            // 从经分侧获取全量集团合同扩展信息数据
            // TODO: 等待经分提供详细接口规范后实现此方法
            List<ZqztGrpContractOtherInfo> dataList = getGroupContractOtherInfoFromJf();

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "集团合同扩展信息导入数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            log.info("开始批量导入集团合同扩展信息，数据量: {}", dataList.size());

            // 执行全量数据入库操作（包含业务逻辑处理）
            int insertCount = zqztGrpContractOtherInfoService.importAllGroupContractOtherInfoWithBusinessLogic(dataList);

            String successMsg = String.format("集团合同扩展信息导入任务执行成功，导入记录数：%d", insertCount);
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
     * 从经分侧获取集团合同扩展信息全量数据
     * TODO: 等待经分提供详细接口规范后实现此方法
     *
     * @return 集团合同扩展信息列表
     */
    private List<ZqztGrpContractOtherInfo> getGroupContractOtherInfoFromJf() {
        // TODO: 实现调用经分接口获取数据的逻辑
        // 这里暂时返回空列表，等待经分接口提供后完善
        log.info("调用经分接口获取集团合同扩展信息数据");

        // 示例：调用经分接口的伪代码，等待实际接口后替换
        // return jfApiService.getFullGroupContractOtherInfo();

        return null; // 暂时返回null，实际实现时返回从经分获取的数据
    }
}
