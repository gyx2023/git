package com.crm.job.service.timedTasks;

import com.crm.job.utils.SftpUtil;
import com.crm.job.utils.export.SummaryHolder;
import com.crm.job.utils.export.TextFileExportContext;
import com.crm.job.utils.export.TextFileExporter;
import com.crm.system.domain.dto.OppoSupportInfoDTO;
import com.crm.system.service.IOppoSupportInfoService;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 商机支撑信息同步给外围系统
 */
@Slf4j
@Component
public class OppoSupportSyncJob {

    private static final String FILE_NAME_PRE = "OPP_SUPPORT_INFO_";
    private static final String FILE_NAME_FIX = ".txt";
    private static final String SPLIT_STR = "&&";

    @Resource
    private IOppoSupportInfoService oppoSupportInfoService;

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ISysParamService sysParamService;

    /**
     * 每日凌晨6:00执行 - 商机支撑信息同步给外围系统
     */
    @XxlJob(value = "oppoSupportInfoSyncJob")
    public void doSync() {
        long startTime = System.currentTimeMillis();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        XxlJobHelper.log("开始执行商机支撑信息同步给外围系统Job");
        log.info("开始执行商机支撑信息同步给外围系统Job");

        // 查询远程目录（opp_support_info_file 的 ftp_directory）
        Map<String, String> param = sysParamService.findValueMapsByType("opp_support_info_file");
        String ftDirectory = param != null ? param.get("ftp_directory") : "";

        TextFileExportContext ctx = new TextFileExportContext();
        ctx.setFileName(FILE_NAME_PRE + dateStr + FILE_NAME_FIX);
        ctx.setFilePath(System.getProperty("java.io.tmpdir"));
        ctx.setExportDate(dateStr);

        new TextFileExporter<TextFileExportContext>() {
            @Override
            protected void writeBody(TextFileExportContext ctx, BufferedWriter writer) throws IOException {
                List<OppoSupportInfoDTO> list = oppoSupportInfoService.selectOppoSupportList();
                if (list != null && !list.isEmpty()) {
                    for (OppoSupportInfoDTO info : list) {
                        writer.write(info.getOppCode() + SPLIT_STR
                                + info.getOaAccount() + SPLIT_STR
                                + info.getName() + SPLIT_STR
                                + info.getNameCh() + "\r\n");
                    }
                    ctx.setTotalRows(list.size());
                    log.info("商机支撑信息同步 共生成 {} 条数据", list.size());
                    XxlJobHelper.log("共生成 {} 条数据", list.size());
                } else {
                    log.warn("商机支撑信息数据为空");
                    XxlJobHelper.log("商机支撑信息数据为空");
                }
            }

            @Override
            protected void finishWriteFileInternal(TextFileExportContext ctx, SummaryHolder summary) {
                String remotePath = ftDirectory + "/" + ctx.getFileName();
                sftpUtil.uploadFile("opp_support_info_file", ctx.getLocalFile().getAbsolutePath(), remotePath);
                log.info("文件上传成功: {}", ctx.getFileName());
                XxlJobHelper.log("文件上传成功:{}", ctx.getFileName());
            }
        }.execute(ctx);

        long costTime = System.currentTimeMillis() - startTime;
        log.info("商机支撑信息同步给外围系统Job执行完成,耗时:{}ms", costTime);
        XxlJobHelper.log("商机支撑信息同步给外围系统Job执行完成,耗时:{}ms", costTime);
    }
}
