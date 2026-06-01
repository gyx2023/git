package com.crm.job.service.timedTasks;

import com.crm.common.utils.FieldAnnotaionUtil;
import com.crm.common.utils.SFTPUtil;
import com.crm.common.utils.StringUtils;
import com.crm.job.base.BaseJob;
import com.crm.system.domain.RepGrpCustBillSumWarning;
import com.crm.system.service.IRepGrpCustBillSumWarningService;
import com.crm.system.service.ISysParamService;
import com.crm.system.utils.Constants;
import com.jcraft.jsch.SftpException;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 集团客户账单汇总预警数据下载Job
 */
@Slf4j
@Component
public class RepGrpCustBillSumJob extends BaseJob {
    private static final String FTP_DOWNLOAD_NAME = "REP_GRP_CUST_BILL_SUM_M_";

    @Resource
    private IRepGrpCustBillSumWarningService repGrpCustBillSumWarningService;

    @PostConstruct
    public void initStaticParam() {
        super.initStaticParam("tc_user_common_warning");
        log.info("集团客户账单汇总预警FTP参数初始化完成");
    }

    /**
     * 集团客户账单汇总预警数据下载定时任务
     */
    @XxlJob(value = "repGrpCustBillSumJob")
    public void download() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("开始执行集团客户账单汇总预警数据下载Job");
        log.info("开始执行集团客户账单汇总预警数据下载Job");

        try {
            String yyyyMM;
            Calendar calendar = Calendar.getInstance();
            // 获取上一个月
            calendar.add(Calendar.MONTH, -2);
            yyyyMM = new SimpleDateFormat(Constants.YEAR_MONTH_PATTERN).format(calendar.getTime());

            // 支持通过XXL-JOB参数传入账期
            String jobparam = XxlJobHelper.getJobParam();
            if (StringUtils.isNotEmpty(jobparam)) {
                yyyyMM = jobparam;
            }

            download(yyyyMM);

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("集团客户账单汇总预警数据下载Job执行完成,耗时:{}ms", costTime);
            XxlJobHelper.log("集团客户账单汇总预警数据下载Job执行完成,耗时:{}ms", costTime);

        } catch (Exception e) {
            log.error("集团客户账单汇总预警数据下载Job执行异常", e);
            XxlJobHelper.log("集团客户账单汇总预警数据下载Job执行异常:{}", e.getMessage());
        }
    }

    /**
     * 下载并处理集团客户账单汇总预警数据
     *
     * @param yyyyMM 账期（格式：yyyyMM）
     */
    public void download(String yyyyMM) {
        log.info("下载波动预警数据 开始  download>>>>>>>>>>>> : ");
        XxlJobHelper.log("开始下载账期为 {} 的波动预警数据", yyyyMM);

        SFTPUtil ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        ftpUtil.login();

        Date date;
        try {
            date = new SimpleDateFormat("yyyyMM").parse(yyyyMM);
        } catch (Exception e) {
            log.error("日期解析失败，账期：{}", yyyyMM, e);
            XxlJobHelper.log("日期解析失败，账期：{}", yyyyMM);
            return;
        }

        String ftpDirectory = ftDirectory + yyyyMM;
        String saveFile = FTP_DOWNLOAD_NAME + yyyyMM + FTP_DOWNLOAD_END_FIX;
        String localFile = localPath + File.separator + saveFile;
        log.info("ftDirectory----" + ftpDirectory + ",localFile----" + localFile + ",saveFile----" + saveFile);
        XxlJobHelper.log("FTP目录：{}，本地文件：{}", ftpDirectory, localFile);

        try {
            // 下载文件
            ftpUtil.download(ftpDirectory, saveFile, localFile);
            // 释放连接
            ftpUtil.logout();

            File file = new File(localFile);
            if (file.exists()) {
                FileReader reader = null;
                BufferedReader bufferedReader = null;
                try {
                    reader = new FileReader(file);
                    bufferedReader = new BufferedReader(reader);
                    int count = 0;
                    String currentLine;
                    List<RepGrpCustBillSumWarning> dataList = new ArrayList<>();

                    while ((currentLine = bufferedReader.readLine()) != null) {
                        count++;
                        RepGrpCustBillSumWarning data = new RepGrpCustBillSumWarning();
                        // 使用分隔符\x1b分割字段，从第2个字段开始映射
                        FieldAnnotaionUtil.setFiledValFromValuesByOrder(data, currentLine.split("\\x1b", -1), 2);
                        data.setCreateDate(date);
                        dataList.add(data);

                        // 每100条批量插入一次
                        if (count % 100 == 0) {
                            this.repGrpCustBillSumWarningService.insertBatch(dataList);
                            log.info("已批量插入 {} 条数据", count);
                            dataList = new ArrayList<>();
                        }
                    }

                    // 插入剩余数据
                    if (!dataList.isEmpty()) {
                        this.repGrpCustBillSumWarningService.insertBatch(dataList);
                        log.info("最后批量插入 {} 条数据", dataList.size());
                    }

                    log.info("共计处理 {} 条数据", count);
                    XxlJobHelper.log("共计处理 {} 条数据", count);

                } catch (Exception e) {
                    log.error("处理文件数据异常", e);
                    XxlJobHelper.log("处理文件数据异常:{}", e.getMessage());
                } finally {
                    // 关闭流
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                }

                // 删除临时文件
                file.delete();
            } else {
                log.error("name: 下载波动预警数据接口 error: {}文件未找到!", localFile);
                XxlJobHelper.log("文件未找到：{}", localFile);
            }
        } catch (SftpException e) {
            log.error("SFTP下载异常", e);
            XxlJobHelper.log("SFTP下载异常:{}", e.getMessage());
        } catch (FileNotFoundException e) {
            log.error("文件未找到异常", e);
            XxlJobHelper.log("文件未找到异常:{}", e.getMessage());
        } catch (Exception e) {
            log.error("下载波动预警数据异常", e);
            XxlJobHelper.log("下载波动预警数据异常:{}", e.getMessage());
        }

        log.info("下载波动预警数据 完成  download>>>>>>>>>>>> : ");
    }
}
