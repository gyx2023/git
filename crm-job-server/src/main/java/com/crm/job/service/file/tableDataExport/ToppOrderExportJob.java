package com.crm.job.service.file.tableDataExport;

import com.crm.system.domain.topp.ToppOrder;
import com.crm.system.service.topp.IToppOrderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.crm.job.utils.SftpUtil;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 订单信息导出定时任务
 * 直接查询数据库并导出数据到txt文件，仅导出满足以下条件的数据：
 * 1. 订单类型为support_order
 * 2. 与tcomm_task表关联的订单
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppOrderExportJob {

    @Resource
    private IToppOrderService toppOrderService;

    @Resource
    private SftpUtil sftpUtil;

    /**
     * 订单信息导出任务
     * 在xxl-job调度中心配置JobHandler为：toppOrderExportJobHandler
     */
    @XxlJob("toppOrderExportJobHandler")
    public void execute() {
        log.info("开始执行订单信息导出任务");
        XxlJobHelper.log("开始执行订单信息导出任务");

        try {
            // 查询满足条件的数据：
            // 1. 订单类型为support_order
            // 2. 与tcomm_task表关联的订单
            List<ToppOrder> dataList = toppOrderService.selectSupportOrders();

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "订单信息导出数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prefix = "topp_order_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                for (ToppOrder record : dataList) {
                    StringBuilder line = new StringBuilder();
                    line.append(escapeNull(record.getId())).append("|");
                    line.append(escapeNull(record.getOppoId())).append("|");
                    line.append(escapeNull(record.getCustId())).append("|");
                    line.append(escapeNull(record.getCreator())).append("|");
                    line.append(escapeNull(record.getSysFormId())).append("|");
                    line.append(escapeNull(record.getAreaId())).append("|");
                    line.append(escapeNull(record.getOrgId())).append("|");
                    line.append(escapeNull(record.getOrderNumber())).append("|");
                    line.append(escapeNull(record.getOrderName())).append("|");
                    line.append(escapeNull(record.getOrderType())).append("|");
                    line.append(escapeNull(record.getPiId())).append("|");
                    line.append(escapeNull(record.getContractId())).append("|");
                    line.append(escapeNull(record.getPhase())).append("|");
                    line.append(escapeNull(record.getCustManagerId())).append("|");
                    line.append(escapeNull(record.getActors())).append("|");
                    line.append(escapeNull(record.getStatus())).append("|");
                    line.append(escapeNull(record.getActorsInfo())).append("|");
                    line.append(escapeNull(record.getPhaseName())).append("|");
                    line.append(escapeNull(record.getWhiteList())).append("|");
                    line.append(escapeNull(record.getOppPhase())).append("|");
                    line.append(escapeNull(record.getClueId())).append("|");
                    line.append(escapeNull(record.getInfoSendId())).append("|");
                    line.append(escapeNull(record.getTaskId())).append("|");
                    line.append(escapeNull(record.getCustName())).append("|");
                    line.append(escapeNull(record.getAccNbr())).append("|");
                    line.append(escapeNull(record.getInfoId())).append("|");
                    line.append(escapeNull(record.getBusinessAcceptanceId())).append("|");
                    line.append(escapeNull(record.getPhaseOrderId())).append("|");
                    line.append(escapeNull(record.getEndReason())).append("|");
                    line.append(escapeNull(record.getWarningLevel())).append("|");
                    line.append(escapeNull(record.getCurInnetFlag())).append("|");
                    line.append(escapeNull(record.getXwyOrderNo())).append("|");
                    line.append(escapeNull(record.getBatchFlag())).append("|");
                    line.append(escapeNull(record.getXwyOrderResult())).append("|");
                    line.append(escapeNull(record.getXwyCount())).append("|");
                    line.append(escapeNull(record.getBusinessDep())).append("|");
                    line.append(escapeNull(record.getBuildingNum())).append("|");
                    line.append(escapeNull(record.getAdoptDays())).append("|");
                    line.append(escapeNull(record.getBusinessType())).append("|");
                    line.append(escapeNull(record.getBusinessTwoType())).append("|");
                    line.append(escapeNull(record.getOrderSource())).append("|");
                    line.append(escapeNull(record.getWideResult())).append("|");
                    line.append(escapeNull(record.getOrderCategory())).append("|");
                    line.append(escapeNull(record.getThirdCheckResult()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                String errorMsg = "订单信息导出文件创建失败";
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = prefix + ".txt";
            try {
                sftpUtil.uploadFile("topp_tcomm_sys_import",tempFile.getAbsolutePath(), remoteFilePath, dateStr);
                String successMsg = String.format("订单信息导出任务执行成功，导出记录数：%d，文件路径：%s", dataList.size(), remoteFilePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } catch (Exception e) {
                String errorMsg = "文件上传到SFTP服务器失败: " + e.getMessage();
                log.error(errorMsg, e);
                XxlJobHelper.handleFail(errorMsg);
            } finally {
                // 删除临时文件
                tempFile.delete();
            }

        } catch (Exception e) {
            String errorMsg = "订单信息导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 处理null值，如果为null则返回空字符串
     *
     * @param value 对象值
     * @return 字符串表示
     */
    private String escapeNull(Object value) {
        return value == null ? "" : value.toString();
    }
}
