package com.crm.job.service.timedTasks;

import com.crm.common.utils.SFTPUtil;
import com.crm.job.base.BaseJob;
import com.crm.system.domain.dto.OppoSupportInfoDTO;
import com.crm.system.service.IOppoSupportInfoService;
import com.jcraft.jsch.SftpException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 商机支撑信息同步给外围系统
 */
@Slf4j
@Component
public class OppoSupportSyncJob extends BaseJob {

    private static final String FILE_NAME_PRE = "OPP_SUPPORT_INFO_";
    private static final String FILE_NAME_FIX = ".txt";
    private static final String SPLIT_STR = "&&";

    @Resource
    private IOppoSupportInfoService oppoSupportInfoService;

    /**
     * 每日凌晨6:00执行 - 商机支撑信息同步给外围系统
     */
    @XxlJob(value = "oppoSupportInfoSyncJob")
    public void doSync() {
        long startTime = System.currentTimeMillis();
        // 从Redis缓存加载FTP配置参数（sys_param表），避免每次改数据库配置需要重启
        super.initStaticParam("opp_support_info_file");
        XxlJobHelper.log("开始执行商机支撑信息同步给外围系统Job");
        log.info("开始执行商机支撑信息同步给外围系统Job");

        String fileName = FILE_NAME_PRE + new SimpleDateFormat("yyyyMMdd").format(new Date()) + FILE_NAME_FIX;
        // 使用系统临时目录存放生成的文件
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir, fileName);

        BufferedWriter writer = null;
        FileInputStream fis = null;

        try {
            log.info("商机支撑信息同步给外围系统 开始生成文件 : fileName={}", fileName);
            XxlJobHelper.log("开始生成文件:{}", fileName);

            // 如果文件已存在，先删除
            if (file.exists()) {
                file.delete();
            }

            writer = new BufferedWriter(new FileWriter(file));

            // 查询商机支撑信息
            List<OppoSupportInfoDTO> list = oppoSupportInfoService.selectOppoSupportList();
            if (list != null && !list.isEmpty()) {
                for (OppoSupportInfoDTO info : list) {
                    String lineData = info.getOppCode() + SPLIT_STR
                            + info.getOaAccount() + SPLIT_STR
                            + info.getName() + SPLIT_STR
                            + info.getNameCh();
                    writer.write(lineData + "\r\n");
                }
                log.info("商机支撑信息同步 共生成 {} 条数据", list.size());
                XxlJobHelper.log("共生成 {} 条数据", list.size());
            } else {
                log.warn("商机支撑信息数据为空");
                XxlJobHelper.log("商机支撑信息数据为空");
            }

            writer.flush();
            writer.close();
            writer = null;

            log.info("商机支撑信息同步给外围系统 生成文件成功,开始推送文件");
            XxlJobHelper.log("文件生成成功,开始SFTP推送");

            SFTPUtil ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
            ftpUtil.login();

            try {
                fis = new FileInputStream(file);
                ftpUtil.upload("/", ftDirectory, fileName, fis);
            } catch (SftpException e) {
                log.error("SFTP上传文件异常", e);
                XxlJobHelper.log("SFTP上传文件异常:{}", e.getMessage());
                throw new RuntimeException("SFTP上传文件异常", e);
            } finally {
                ftpUtil.logout();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        log.warn("关闭文件流异常", e);
                    }
                }
            }

            log.info("商机支撑信息同步给外围系统文件{}推送成功", fileName);
            XxlJobHelper.log("文件{}推送成功", fileName);

        } catch (Exception e) {
            log.error("商机支撑信息同步给外围系统Job执行异常", e);
            XxlJobHelper.log("商机支撑信息同步给外围系统Job执行异常:{}", e.getMessage());
        } finally {
            // 关闭writer
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn("关闭文件写入流异常", e);
                }
            }
            // 删除临时文件
            if (file.exists()) {
                file.delete();
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("商机支撑信息同步给外围系统Job执行完成,耗时:{}ms", costTime);
        XxlJobHelper.log("商机支撑信息同步给外围系统Job执行完成,耗时:{}ms", costTime);
    }
}
