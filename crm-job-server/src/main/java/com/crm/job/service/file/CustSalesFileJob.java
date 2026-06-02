package com.crm.job.service.file;

import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.FtpBroadbandMarketResult;
import com.crm.system.mapper.FtpBroadbandMarketResultMapper;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CustSalesFileJob {

    private static final String RESULT_FILE_NAME = "TC_USER_BROADBAND_MARKETING_RESULT_M_";

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private FtpBroadbandMarketResultMapper ftpBroadbandMarketResultMapper;

    @XxlJob(value = "resultSalesFileJob")
    public void resultSalesFileJob() {
        // 1. 获取账期（支持任务传参，默认上个月）
        String jobParam = XxlJobHelper.getJobParam();
        String yyyyMM;
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            yyyyMM = jobParam;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            yyyyMM = new SimpleDateFormat("yyyyMM").format(calendar.getTime());
        }

        // 2. 获取参数并下载文件
        Map<String, String> param = sysParamService.findValueMapsByType("broadband_market");
        String resultFileDirectory = param.get("ftp_result_directory") + yyyyMM;
        String resultFileName = RESULT_FILE_NAME + yyyyMM + ".txt";
        String localFilePath = MapUtils.getString(param, "ftp_local") + File.separator + resultFileName;

        SftpWebUtil ftpUtil = new SftpWebUtil(
            param.get("ftp_username"),
            param.get("ftp_password"),
            param.get("ftp_host"),
            MapUtils.getIntValue(param, "ftp_port")
        );

        boolean downloadSuccess = false;
        try {
            log.info("宽带专项营销下载结果文件开始");
            ftpUtil.login();
            ftpUtil.download(resultFileDirectory, resultFileName, localFilePath);
            log.info("宽带专项营销下载结果文件成功");
            downloadSuccess = true;
        } catch (Exception e) {
            log.error("宽带专项营销文件下载失败", e);
        } finally {
            ftpUtil.logout();
        }

        // 3. 下载失败直接退出
        if (!downloadSuccess) {
            log.error("文件下载失败，终止后续数据入库流程");
            return;
        }

        // 4. 清理历史数据
        log.info("开始清理{}账期历史数据", yyyyMM);
        ftpBroadbandMarketResultMapper.deleteByStatCycle(yyyyMM);
        log.info("{}账期历史数据清理完成，开始数据入库", yyyyMM);

        // 5. 文件解析入库
        File file = new File(localFilePath);
        if (!file.exists()) {
            log.error("宽带专项营销业务受理结果文件不存在");
            return;
        }

        log.info("开始入库宽带专项营销业务受理结果数据");
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(isr)) {

            List<FtpBroadbandMarketResult> dataList = new ArrayList<>();
            String currentLine;
            int count = 0;

            while ((currentLine = bufferedReader.readLine()) != null) {
                count++;
                String[] data = currentLine.split("\\x1b", -1);

                FtpBroadbandMarketResult result = new FtpBroadbandMarketResult();
                result.setStatCycle(data[0]);
                result.setMarketingType(data[1]);
                result.setMarketingName(data[2]);
                result.setUserMarketingId(data[3]);
                result.setUserId(data[4]);
                result.setSvcNum(data[5]);
                result.setIsSucc(data[6]);
                result.setCreateDate(data[7]);
                result.setStartDate(data[8]);
                result.setEndDate(data[9]);
                result.setOrderProductCode(data[10]);
                result.setOrderProductName(data[11]);
                result.setOrderDiscntCode(data[12]);
                result.setOrderDiscntName(data[13]);
                result.setNewSpeed(data[14]);

                dataList.add(result);

                // 每1000条批量插入
                if (count % 1000 == 0) {
                    log.info("---------->开始入库第{}至{}行数据", count - 999, count);
                    ftpBroadbandMarketResultMapper.insertBatch(dataList);
                    log.info("---------->入库第{}至{}行数据成功", count - 999, count);
                    dataList.clear();
                }
            }

            // 插入剩余数据
            if (!dataList.isEmpty()) {
                ftpBroadbandMarketResultMapper.insertBatch(dataList);
                log.info("---------->入库最后{}行数据成功", dataList.size());
            }

            log.info("入库宽带专项营销业务受理结果数据完成，共入库{}条数据", count);

        } catch (UnsupportedEncodingException e) {
            log.error("文件编码异常", e);
        } catch (IOException e) {
            log.error("文件读取异常", e);
        }
    }
}
