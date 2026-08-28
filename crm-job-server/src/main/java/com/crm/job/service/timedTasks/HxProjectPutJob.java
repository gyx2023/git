package com.crm.job.service.timedTasks;

import com.crm.job.base.BaseJob;
import com.crm.job.utils.SftpUtil;
import com.crm.job.utils.export.TextFileExportContext;
import com.crm.job.utils.export.TextFileExporter;
import com.crm.job.utils.export.SummaryHolder;
import com.crm.system.domain.dao.sys.SysOperLog;
import com.crm.system.service.IHxProjectPutService;
import com.crm.system.service.sys.ISysOperLogService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 将划小数据推送到工建系统
 */
@Slf4j
@Component
public class HxProjectPutJob extends BaseJob {

    private static final String[] REMAND_COLUMNS = {"remand_code", "oppo_code", "contract_no", "remandContract"};
    private static final String[] CONTRACT_COLUMNS = {"contract_no", "contract_name", "startTime", "endTime"};

    private static final String REMAND_FILE_NAME = "remand_contract.TMP";
    private static final String REMAND_FILE_NAME_TXT = "remand_contract.TXT";
    private static final String CONTRACT_FILE_NAME = "contract_info.TMP";
    private static final String CONTRACT_FILE_NAME_TXT = "contract_info.TXT";

    @Resource
    private IHxProjectPutService hxProjectPutService;

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ISysOperLogService sysOperLogService;

    @XxlJob(value = "hxProjectPutJob")
    public void doPut() {
        long startTime = System.currentTimeMillis();
        super.initStaticParam("ftp_info");

        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
        XxlJobHelper.log("开始执行划小数据推送工建系统Job, 日期:{}", yyyyMMdd);
        log.info("开始执行划小数据推送工建系统Job, 日期:{}", yyyyMMdd);

        final String configType = "ftp_info";
        try {
            // 导出需求与合同关系文件
            exportFile(configType, yyyyMMdd, REMAND_FILE_NAME, REMAND_FILE_NAME_TXT, REMAND_COLUMNS,
                    () -> hxProjectPutService.getRemandContract());

            // 导出合同信息文件
            exportFile(configType, yyyyMMdd, CONTRACT_FILE_NAME, CONTRACT_FILE_NAME_TXT, CONTRACT_COLUMNS,
                    () -> hxProjectPutService.getContractInfo());

            log.info("划小数据推送工建系统Job执行完成");
            XxlJobHelper.log("划小数据推送工建系统Job执行完成");
        } catch (Exception e) {
            log.error("划小数据推送工建系统Job执行异常", e);
            XxlJobHelper.log("划小数据推送工建系统Job执行异常:{}", e.getMessage());
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("划小数据推送工建系统Job执行完成, 耗时:{}ms", costTime);
        XxlJobHelper.log("划小数据推送工建系统Job执行完成, 耗时:{}ms", costTime);
    }

    private void exportFile(String configType, String yyyyMMdd, String tmpName, String txtName,
                            String[] columns, Supplier<List<Map<String, Object>>> dataSupplier) {
        TextFileExportContext ctx = new TextFileExportContext();
        ctx.setFileName(tmpName);
        ctx.setFilePath(System.getProperty("java.io.tmpdir") + "/hx/" + yyyyMMdd);

        new TextFileExporter<TextFileExportContext>() {
            @Override
            protected void writeBody(TextFileExportContext ctx, BufferedWriter writer) throws IOException {
                List<Map<String, Object>> dataList = dataSupplier.get();
                writeMapList(writer, dataList, columns);
                ctx.setTotalRows(dataList != null ? dataList.size() : 0);
            }

            @Override
            protected void finishWriteFileInternal(TextFileExportContext ctx, SummaryHolder summary) {
                String remoteDir = ftDirectory + "/" + yyyyMMdd;
                sftpUtil.uploadFile(configType, ctx.getLocalFile().getAbsolutePath(), tmpName, yyyyMMdd);
                sftpUtil.rename(configType, remoteDir + "/" + tmpName, remoteDir + "/" + txtName);
                XxlJobHelper.log("文件上传成功: {}", tmpName);
            }

            @Override
            protected void recordOperLog(TextFileExportContext ctx, long startTime, String errorMsg) {
                try {
                    SysOperLog operLog = new SysOperLog();
                    operLog.setTitle("划小数据推送工建系统-" + tmpName);
                    operLog.setRequestMethod("JOB");
                    operLog.setOperatorType(2);
                    operLog.setOperName("system");
                    operLog.setOperParam("fileName=" + ctx.getFileName() + ", date=" + yyyyMMdd);
                    operLog.setStatus(errorMsg == null ? 0 : 1);
                    operLog.setJsonResult("导出行数=" + ctx.getTotalRows());
                    operLog.setErrorMsg(errorMsg);
                    operLog.setRequestTime(System.currentTimeMillis() - startTime);
                    sysOperLogService.insertOperlog(operLog);
                } catch (Exception e) {
                    log.error("记录操作日志异常, 文件: {}", tmpName, e);
                }
            }
        }.execute(ctx);
    }

    private void writeMapList(BufferedWriter w, List<Map<String, Object>> dataList, String[] columns) throws IOException {
        if (dataList == null) return;
        for (Map<String, Object> row : dataList) {
            for (int j = 0; j < columns.length; j++) {
                Object val = row.get(columns[j]);
                w.write(val != null ? val.toString() : "");
                if (j != columns.length - 1) w.write('\t');
            }
            w.write('\n');
        }
    }
}
