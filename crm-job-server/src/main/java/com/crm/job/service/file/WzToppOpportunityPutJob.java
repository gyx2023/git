package com.crm.job.service.file;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.dto.OpportunityDTO;
import com.crm.common.enums.RedisKeyConstants;
import com.crm.job.utils.FilePath;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.jcraft.jsch.SftpException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 温州商机数据推送
 *
 * @author yn
 * date 2021/5/26 10:21
 */
@Slf4j
@Component
public class WzToppOpportunityPutJob {

    @Resource
    private ISysParamService sysParamService;
    @Resource
    private IToppOpportunityService opportunityService;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 每日凌晨1点
     */
    @XxlJob(value = "WzToppOpportunityPutJob")
    public void start() {
        String redisKey = RedisKeyConstants.WZ_TOPP_OPPORTUNITY_PUT_TASK;
        RLock lock = redissonClient.getLock(redisKey);
        try {
            // 加分布式锁，防止多节点重复执行
            lock.lock(RedisKeyConstants.MAX_LOCK_TIME, TimeUnit.SECONDS);
            //获取昨天的日期
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date d = cal.getTime();
            SimpleDateFormat sp = new SimpleDateFormat("yyyyMMdd");
            String yesterday = sp.format(d);

            String jobparam = XxlJobHelper.getJobParam();
            if (ObjectUtil.isNotEmpty(jobparam)) {
                yesterday = jobparam;
            }
            creatFile(yesterday);
        } catch (Exception e) {
            log.error("【温州商机数据推送】任务执行异常", e);
            throw new RuntimeException("同步任务异常", e);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("【温州商机数据推送】分布式锁已释放");
            }
        }
    }
    public void creatFile(String yesterday) {
        log.info("商机推送Job 开始  >>>>>>>>>>>> : ");
        String filePath = FilePath.rootPath;
        String fileName = yesterday + "_toppOpportunity.txt";

        // ====================== 自动创建目录 ======================
        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean mkdirSuccess = dir.mkdirs();
            log.info("创建目录结果：{}，路径：{}", mkdirSuccess, filePath);
        }

        // ====================== 生成文件（UTF-8 无乱码） ======================
        File file = new File(filePath, fileName);
        List<OpportunityDTO> list = opportunityService.getToppOpportunityPut("3303");//温州

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
        )) {
            for (OpportunityDTO dto : list) {
                StringBuilder content = new StringBuilder();
                content.append(dto.getOppCode()).append("&")
                    .append(dto.getOppName()).append("&")
                    .append(dto.getOppType()).append("&")
                    .append(dto.getExpectContractMoney()).append("&")
                    .append(dto.getContractMonth()).append("&")
                    .append(dto.getCustNo()).append("&")
                    .append(dto.getCustName()).append("&")
                    .append(dto.getCreateTime()).append("&")
                    .append(dto.getOppoState()).append("&")
                    .append(dto.getAreaName()).append("&")
                    .append(dto.getOrgName()).append("&")
                    .append(dto.getCustManagerName()).append("&")
                    .append(dto.getCustManagerTel());

                writer.write(content.toString());
                writer.newLine();
            }
            log.info("商机文件写入完成，条数：{}，文件：{}", list.size(), file.getAbsolutePath());


        } catch (IOException e) {
            log.error("商机文件写入失败", e);
            return; // 写入失败直接退出，不执行上传
        }

        // ====================== FTP 上传 ======================
        Map<String, String> param = sysParamService.findValueMapsByType("wz_cust_signin_opportunity_contract_put");
        String ftpUsername = param.get("ftp_username");
        String ftpPassword = param.get("ftp_password");
        String ftpHost = param.get("ftp_host");
        String ftpPort = param.get("ftp_port");

        // 自动关闭流：try-with-resources
        try (InputStream input = new FileInputStream(file)) {
            log.info("开始上传文件到FTP：{}", fileName);
            putFile(ftpUsername, ftpPassword, ftpHost, ftpPort, fileName, input);
            log.info("FTP上传成功：{}", fileName);

            // 上传成功后再删除文件
            if (file.exists()) {
                boolean deleteSuccess = file.delete();
                log.info("文件删除结果：{}，文件：{}", deleteSuccess, file.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("FTP上传或文件操作失败：{}", fileName, e);
            // 上传失败，不删除文件，方便排查
        }
    }

    public void putFile(String ftpUsername, String ftpPassword, String ftpHost, String ftpPort, String fileName, InputStream input) {
        SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        ftpUtil.login();
        try {
            ftpUtil.upload("/", "upload", fileName, input);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        ftpUtil.logout();// 释放连接
        log.info("商机推送Job 结束  >>>>>>>>>>>> : ");
    }
}
