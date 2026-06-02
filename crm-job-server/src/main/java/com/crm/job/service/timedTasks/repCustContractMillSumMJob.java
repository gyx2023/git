package com.crm.job.service.timedTasks;

import com.crm.common.utils.FieldAnnotaionUtil;
import com.crm.common.utils.SFTPUtil;
import com.crm.job.base.BaseJob;
import com.crm.system.domain.dao.RepCustContractMillSumM;
import com.crm.system.service.IRepCustContractMillSumMService;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 政企运营平台合同收入报表定时任务
 * 对方每月8日12点提供，我们13点获取
 */
@Slf4j
@Component
public class repCustContractMillSumMJob extends BaseJob {

    /**
     * 下载的文件名前缀
     */
    private static final String FTP_DOWNLOAD_NAME = "REP_CUST_CONTRACT_BILL_SUM_M_";

    @Resource
    private IRepCustContractMillSumMService repCustContractMillSumMService;

    @PostConstruct
    public void initStaticParam() {
        super.initStaticParam("rep_cust_contract_mill_sum_m");
        log.info("政企运营平台合同收入报表需求接口ftp信息初始化完成");
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
        XxlJobHelper.log("开始下载账期为 {} 的合同收入报表数据", yyyyMM);

        SFTPUtil ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        try {
            ftpUtil.login();
            String fileNamePrefix = FTP_DOWNLOAD_NAME + yyyyMM;
            String saveFile = fileNamePrefix + FTP_DOWNLOAD_END_FIX;
            String ftDirectory1 = ftDirectory + yyyyMM;
            String localFile = localPath + File.separator + saveFile;
            log.info("合同收入报表需求接口ftp开始下载文件:" + ftDirectory1 + File.separator + saveFile);
            XxlJobHelper.log("FTP目录：{}，本地文件：{}", ftDirectory1, localFile);
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
                XxlJobHelper.log("共计处理 {} 条数据", count);
            }
        } catch (Exception e) {
            log.error("合同收入报表数据导入异常", e);
            XxlJobHelper.log("合同收入报表数据导入异常:{}", e.getMessage());
            throw new RuntimeException("合同收入报表数据导入异常", e);
        }
        log.info("政企运营平台合同收入报表接口执行完成");
    }
}
