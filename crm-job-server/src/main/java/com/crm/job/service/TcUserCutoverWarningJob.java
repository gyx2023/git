package com.crm.job.service;

import com.crm.common.utils.StringUtils;
import com.crm.job.base.BaseJob;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.dao.FtpTcUserCutOverWarningDo;
import com.crm.system.service.IFtpTcUserCutOverWarningService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户级割接预警通用接口
 *
 * @author Saber
 */
@Slf4j
@Component
public class TcUserCutoverWarningJob extends BaseJob {

    private static final String FTP_DOWNLOAD_NAME = "TC_USER_CUTOVER_CIRCUIT_";
    private static final String FIELD_SEPARATOR = "\\x1b";
    private static final int BATCH_SIZE = 100;
    private static final String DATE_FORMAT = "yyyyMMdd";
    /** 文件中字段个数，与 FtpTcUserCutOverWarningDo 中非DB额外字段(非id_/serv_manager_id/create_date)数量一致 */
    private static final int EXPECTED_FIELD_COUNT = 20;

    @Resource
    private IFtpTcUserCutOverWarningService ftpTcUserCutOverWarningService;

    @PostConstruct
    public void init() {
        initStaticParam("dwd_cust_base_info");
    }

    /**
     * 文件下发时间:8点 (D1文件)
     */
    @XxlJob("tcUserCutoverWarningJob1")
    public void download() {
        String yyyyMMdd = getDate();
        download(yyyyMMdd, "1");
    }

    /**
     * 文件下发时间:12点半 (D2文件)
     */
    @XxlJob("tcUserCutoverWarningJob2")
    public void download1() {
        String yyyyMMdd = getDate();
        download(yyyyMMdd, "2");
    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yyyyMMdd = new SimpleDateFormat(DATE_FORMAT).format(calendar.getTime());
        String jobParam = XxlJobHelper.getJobParam();
        if (StringUtils.isNotEmpty(jobParam)) {
            yyyyMMdd = jobParam;
        }
        return yyyyMMdd;
    }

    public void download(String yyyyMMdd, String fileIndex) {
        log.info("用户级割接预警接口同步 开始  download>>>>>>>>>>>> : ");

        Map<String, String> param = sysParamService.findValueMapsByType("tc_user_common_warning");
        String ftDirectory = param.get("ftp_directory");
        ftDirectory += yyyyMMdd;
        String saveFile = FTP_DOWNLOAD_NAME + "D" + fileIndex + "_" + yyyyMMdd + FTP_DOWNLOAD_END_FIX;
        String localFile = localPath + File.separator + saveFile;

        System.out.println("ftDirectory----" + ftDirectory);
        System.out.println("localFile----" + localFile);
        System.out.println("saveFile----" + saveFile);

        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT).parse(yyyyMMdd);
        } catch (ParseException e) {
            log.error("日期解析失败: {}", yyyyMMdd, e);
            return;
        }

        try (SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort))) {
            ftpUtil.login();
            ftpUtil.download(ftDirectory, saveFile, localFile);

            System.out.println("localFile====" + localFile);
            File file = new File(localFile);
            if (file.exists()) {
                try (FileReader reader = new FileReader(file);
                     BufferedReader bufferedReader = new BufferedReader(reader)) {

                    int count = 0;
                    String currentLine;
                    List<FtpTcUserCutOverWarningDo> dataList = new ArrayList<>(BATCH_SIZE);

                    while ((currentLine = bufferedReader.readLine()) != null) {
                        count++;
                        FtpTcUserCutOverWarningDo data = parseLine(currentLine, date);
                        dataList.add(data);

                        if (count % BATCH_SIZE == 0) {
                            ftpTcUserCutOverWarningService.insertBatch(dataList);
                            dataList = new ArrayList<>(BATCH_SIZE);
                        }
                    }

                    if (!dataList.isEmpty()) {
                        ftpTcUserCutOverWarningService.insertBatch(dataList);
                    }

                    log.info("处理完成, 总行数: {}", count);
                }
                file.delete();
            } else {
                StringBuffer logs = new StringBuffer();
                logs.append("name: 用户级割接预警接口 ").append("\r\n");
                logs.append("error: ").append(localFile).append("文件未找到!");
                log.info(logs.toString());
            }
        } catch (Exception e) {
            log.error("下载或解析异常", e);
        }

        log.info("用户级割接预警接口同步 完成  download>>>>>>>>>>>> : ");
    }

    /**
     * 解析一行数据，字段顺序与 yypt_web FtpTcUserCutOverWarning 实体声明顺序一致
     */
    private FtpTcUserCutOverWarningDo parseLine(String line, Date createDate) {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        FtpTcUserCutOverWarningDo data = new FtpTcUserCutOverWarningDo();

        if (fields.length != EXPECTED_FIELD_COUNT) {
            log.warn("字段数不匹配: 期望{}个, 实际{}个", EXPECTED_FIELD_COUNT, fields.length);
            data.setServManagerId("");
            data.setCreateDate(createDate);
            return data;
        }

        data.setCutoverId(getField(fields, 0));
        data.setCutoverNo(getField(fields, 1));
        data.setCutoverArea(getField(fields, 2));
        data.setCircuitName(getField(fields, 3));
        data.setBusinessId(getField(fields, 4));
        data.setBssNumber(getField(fields, 5));
        data.setCustomer(getField(fields, 6));
        data.setACompany(getField(fields, 7));
        data.setACompanyAddr(getField(fields, 8));
        data.setZCompany(getField(fields, 9));
        data.setZCompanyAddr(getField(fields, 10));

        String custGrpIdStr = getField(fields, 11);
        if (StringUtils.isNotEmpty(custGrpIdStr)) {
            try {
                data.setCustGrpId(Integer.valueOf(custGrpIdStr));
            } catch (NumberFormatException ignored) {
            }
        }

        data.setGrpCode(getField(fields, 12));
        data.setGrpName(getField(fields, 13));
        data.setMainOrderFlowNo(getField(fields, 14));
        data.setOrderFlowNo(getField(fields, 15));
        data.setZbBuildId(getField(fields, 16));
        data.setBuildingName(getField(fields, 17));
        data.setBreakState(getField(fields, 18));
        data.setImpactBiz(getField(fields, 19));

        data.setServManagerId("");
        data.setCreateDate(createDate);

        return data;
    }

    private String getField(String[] fields, int index) {
        return index < fields.length ? fields[index].trim() : "";
    }
}
