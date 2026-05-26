package com.crm.job.service.timedTasks;

import com.crm.common.utils.FieldAnnotaionUtil;
import com.crm.common.utils.SFTPUtil;
import com.crm.system.domain.RepCustContractMillSumM;
import com.crm.system.service.IRepCustContractMillSumMService;
import com.crm.system.service.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 政企运营平台合同收入报表定时任务
 * 对方每月8日12点提供，我们13点获取
 */
@Slf4j
@Component
public class repCustContractMillSumMJob {

    /**
     * 下载的文件名前缀
     */
    private static final String ftpDownloadName = "REP_CUST_CONTRACT_BILL_SUM_M_";

    /**
     * 下载的文件名扩展名
     */
    private static final String ftpDownloadEndFix = ".txt";

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private IRepCustContractMillSumMService repCustContractMillSumMService;

    static String ftpUsername;
    static String ftpPassword;
    static String ftpHost;
    static String ftpPort;
    static String ftDirectory;
    static String localPath;

    @PostConstruct
    public void initStaticParam() {
        try {
            Map<String, String> param = sysParamService.findValueMapsByType("rep_cust_contract_mill_sum_m");
            //用户名
            ftpUsername = param.get("ftp_username");
            //密码
            ftpPassword = param.get("ftp_password");
            //ip地址
            ftpHost = param.get("ftp_host");
            //端口号
            ftpPort = param.get("ftp_port");
            //文件存放目录
            ftDirectory = param.get("ftp_directory");
            //本地下载目录
            localPath = param.get("ftp_local");
            log.info("政企运营平台合同收入报表需求接口ftp信息初始化完成");
        } catch (Exception e) {
            log.error("政企运营平台合同收入报表需求接口ftp信息初始化失败", e);
        }
    }

    /**
     * 定时任务入口
     */
    @XxlJob(value = "repCustContractMillSumMJob")
    public void download() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("开始执行合同收入报表定时任务");
        log.info("开始执行合同收入报表定时任务");

        try {
            // 计算上个月的年月
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);
            String year = String.valueOf(cal.get(Calendar.YEAR));
            int month = cal.get(Calendar.MONTH) + 1;
            String months;
            if (month < 10) {
                months = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
            } else {
                months = String.valueOf(cal.get(Calendar.MONTH) + 1);
            }

            String yyyyMM = year + months;
            String jobparam = XxlJobHelper.getJobParam();
            if (jobparam != null && !jobparam.trim().isEmpty()) {
                yyyyMM = jobparam;
            }

            start(yyyyMM);

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("合同收入报表定时任务执行完成,耗时:{}ms", costTime);
            XxlJobHelper.log("合同收入报表定时任务执行完成,耗时:{}ms", costTime);
        } catch (Exception e) {
            log.error("合同收入报表定时任务执行异常", e);
            XxlJobHelper.log("合同收入报表定时任务执行异常:{}", e.getMessage());
        }
    }

    /**
     * 执行文件下载和数据导入
     *
     * @param yyyyMM 账期年月
     */
    public void start(String yyyyMM) {
        SFTPUtil ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        try {
            ftpUtil.login();
            String fileNamePrefix = ftpDownloadName + yyyyMM;
            String saveFile = fileNamePrefix + ftpDownloadEndFix;
            String ftDirectory1 = ftDirectory + "/" + yyyyMM;
            String localFile = localPath + File.separator + saveFile;
            log.info("合同收入报表需求接口ftp开始下载文件:" + ftDirectory1 + File.separator + saveFile);
            ftpUtil.download(ftDirectory1, saveFile, localFile);
            log.info("合同收入报表需求接口ftp下载文件完成:" + localFile);

            File file = new File(localFile);
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                int count = 0;
                String currentLine;
                List<RepCustContractMillSumM> dataList = new ArrayList<>();
                while ((currentLine = bufferedReader.readLine()) != null) {
                    count++;
                    RepCustContractMillSumM data = new RepCustContractMillSumM();
                    FieldAnnotaionUtil.setFiledValFromValuesByOrder(data, currentLine.split("\\x1b", -1), 3);
                    data.setCreateDate(new Date());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
                    Date paymentDays = simpleDateFormat.parse(yyyyMM);
                    data.setPaymentDays(paymentDays);
                    dataList.add(data);
                    if (count % 100 == 0) {
                        repCustContractMillSumMService.insertBatch(dataList);
                        dataList = new ArrayList<>();
                    }
                }
                if (!dataList.isEmpty()) {
                    repCustContractMillSumMService.insertBatch(dataList);
                }
                bufferedReader.close();
                reader.close();
            }
        } catch (Exception e) {
            log.error("合同收入报表数据导入异常", e);
            throw new RuntimeException("合同收入报表数据导入异常", e);
        }
        log.info("政企运营平台合同收入报表接口执行完成");
    }
}
