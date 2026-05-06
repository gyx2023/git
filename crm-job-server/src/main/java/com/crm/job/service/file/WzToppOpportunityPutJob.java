package com.crm.job.service.file;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.dto.OpportunityDTO;
import com.crm.job.utils.FilePath;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.jcraft.jsch.SftpException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商机数据推送Job
 *
 * @author yn
 * date 2021/5/26 10:21
 */
@Component
public class WzToppOpportunityPutJob {

    private static final Log log = LogFactory.getLog(WzToppOpportunityPutJob.class);

    @Resource
    ISysParamService sysParamService;
    @Resource
    protected IToppOpportunityService opportunityService;

    /**
     * 每日凌晨1点
     */
    @XxlJob(value = "WzToppOpportunityPutJob")
    public void start() {
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
    }

    public void creatFile(String yesterday) {
        log.info("商机推送Job 开始  >>>>>>>>>>>> : ");
        String filePath = FilePath.rootPath;
        String fileName = yesterday + "_toppOpportunity.txt";
        List<OpportunityDTO> list = opportunityService.getToppOpportunityPut("3303");//温州

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "/" + fileName, true))) {
            for (OpportunityDTO dto : list) {
                StringBuilder content = new StringBuilder();

                // 按顺序拼接字段，用 & 分隔
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

                // 写入一行
                writer.write(content.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> param = sysParamService.findValueMapsByType("wz_cust_signin_opportunity_contract_put");
        //用户名
        String ftpUsername = param.get("ftp_username");
        //密码
        String ftpPassword = param.get("ftp_password");
        //ip地址
        String ftpHost = param.get("ftp_host");
        //端口号
        String ftpPort = param.get("ftp_port");

        InputStream input = null;
        try {
            File file = new File(filePath + "/" + fileName);
            input = new FileInputStream(file);
            putFile(ftpUsername, ftpPassword, ftpHost, ftpPort, fileName, input);
            //删除文件
            if (file.exists()) {
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
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
