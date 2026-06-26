package com.crm.job.service.timedTasks;

import com.crm.common.utils.SFTPUtil;
import com.crm.job.base.BaseJob;
import com.crm.system.service.IHxProjectPutService;
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

/**
 * 将划小数据推送到工建系统
 */
@Slf4j
@Component
public class HxProjectPutJob extends BaseJob {

    /** 需求与合同关系文件列名 */
    private static final String[] REMAND_COLUMNS = {"remand_code", "oppo_code", "contract_no", "remandContract"};
    /** 合同信息文件列名 */
    private static final String[] CONTRACT_COLUMNS = {"contract_no", "contract_name", "startTime", "endTime"};

    private static final String REMAND_FILE_NAME = "remand_contract.TMP";
    private static final String REMAND_FILE_NAME_TXT = "remand_contract.txt";
    private static final String CONTRACT_FILE_NAME = "contract_info.TMP";
    private static final String CONTRACT_FILE_NAME_TXT = "contract_info.txt";
    private static final String FTP_BASE_PATH = "/oppocontract";

    @Resource
    private IHxProjectPutService hxProjectPutService;

    /**
     * 每月5号1点 - 将划小数据推送到工建系统
     */
    @XxlJob(value = "hxProjectPutJob")
    public void doPut() {
        long startTime = System.currentTimeMillis();
        //加载FTP配置参数
        super.initStaticParam("ftp_info");

        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
        XxlJobHelper.log("开始执行划小数据推送工建系统Job, 日期:{}", yyyyMMdd);
        log.info("开始执行划小数据推送工建系统Job, 日期:{}", yyyyMMdd);

        String tempDir = System.getProperty("java.io.tmpdir");
        String filePathLocal = tempDir + "/hx/" + yyyyMMdd;
        File remandFile = new File(filePathLocal + "/" + REMAND_FILE_NAME);
        File contractFile = new File(filePathLocal + "/" + CONTRACT_FILE_NAME);

        SFTPUtil ftpUtil = null;
        try {
            //登录SFTP
            ftpUtil = new SFTPUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
            ftpUtil.login();
            log.info("SFTP登录成功, 可以开始上传文件");
            XxlJobHelper.log("SFTP登录成功, 可以开始上传文件");

            //生成需求与合同关系文件
            List<Map<String, Object>> remandContract = hxProjectPutService.getRemandContract();
            createFile(remandFile, remandContract, REMAND_COLUMNS);
            log.info("需求与合同关系文件生成成功, 共{}条", remandContract != null ? remandContract.size() : 0);
            XxlJobHelper.log("需求与合同关系文件生成成功, 共{}条", remandContract != null ? remandContract.size() : 0);

            //上传需求与合同关系文件
            try (FileInputStream fis = new FileInputStream(remandFile)) {
                ftpUtil.upload(ftDirectory, yyyyMMdd, REMAND_FILE_NAME, fis);
                ftpUtil.rename(ftDirectory + "/" + yyyyMMdd, REMAND_FILE_NAME,REMAND_FILE_NAME_TXT);
                log.info("文件上传成功: {}", REMAND_FILE_NAME);
                XxlJobHelper.log("文件上传成功: {}", REMAND_FILE_NAME);
            }

            //生成合同信息文件
            List<Map<String, Object>> contractInfo = hxProjectPutService.getContractInfo();
            createFile(contractFile, contractInfo, CONTRACT_COLUMNS);
            log.info("合同信息文件生成成功, 共{}条", contractInfo != null ? contractInfo.size() : 0);
            XxlJobHelper.log("合同信息文件生成成功, 共{}条", contractInfo != null ? contractInfo.size() : 0);

            //上传合同信息文件
            try (FileInputStream fis = new FileInputStream(contractFile)) {
                ftpUtil.upload(ftDirectory, yyyyMMdd, CONTRACT_FILE_NAME, fis);
                ftpUtil.rename(ftDirectory + "/" + yyyyMMdd, CONTRACT_FILE_NAME,CONTRACT_FILE_NAME_TXT);
                log.info("文件上传成功: {}", CONTRACT_FILE_NAME);
                XxlJobHelper.log("文件上传成功: {}", CONTRACT_FILE_NAME);
            }

            log.info("划小数据推送工建系统Job执行完成");
            XxlJobHelper.log("划小数据推送工建系统Job执行完成");
        } catch (Exception e) {
            log.error("划小数据推送工建系统Job执行异常", e);
            XxlJobHelper.log("划小数据推送工建系统Job执行异常:{}", e.getMessage());
        } finally {
            // 释放SFTP连接
            if (ftpUtil != null) {
                ftpUtil.logout();
            }
            // 删除本地临时文件
            File dirFile = new File(tempDir + "/hx");
            deleteDir(dirFile);
        }

        long costTime = System.currentTimeMillis() - startTime;
        log.info("划小数据推送工建系统Job执行完成, 耗时:{}ms", costTime);
        XxlJobHelper.log("划小数据推送工建系统Job执行完成, 耗时:{}ms", costTime);
    }

    /**
     * 本地创建Tab分隔的文件
     *
     * @param file 目标文件
     * @param dataList 数据列表
     * @param columns 列名数组（保证输出顺序）
     */
    private void createFile(File file, List<Map<String, Object>> dataList, String[] columns) {
        BufferedWriter bw = null;
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }

            bw = new BufferedWriter(new FileWriter(file, true));

            if (dataList != null && !dataList.isEmpty()) {
                for (Map<String, Object> row : dataList) {
                    for (int j = 0; j < columns.length; j++) {
                        Object val = row.get(columns[j]);
                        bw.write(val != null ? val.toString() : "");
                        if (j != columns.length - 1) {
                            bw.write('\t');
                        }
                    }
                    bw.write('\n');
                }
            }
            bw.flush();
        } catch (Exception e) {
            log.error("创建文件异常, file={}", file.getAbsolutePath(), e);
            throw new RuntimeException("创建文件异常", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    log.warn("关闭BufferedWriter异常", e);
                }
            }
        }
    }

    /**
     * 递归删除目录及文件
     *
     * @param dir 目录
     */
    private void deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDir(f);
                }
            }
        }
        dir.delete();
    }
}
