package com.crm.job.service.file;

import com.crm.common.utils.StringUtils;
import com.crm.job.base.AbstractFtpImportJob;
import com.crm.system.domain.dao.FtpBroadbandMarketResult;
import com.crm.system.mapper.FtpBroadbandMarketResultMapper;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销业务受理结果文件导入
 * <p>
 * 从 SFTP 下载结果文件（TC_USER_BROADBAND_MARKETING_RESULT_M_yyyyMM.txt）并入库到 ftp_broadband_market_result 表
 * <p>
 * XXL-Job 参数：只传 yyyyMM 账期（如 202607），不传则默认上月
 *
 * @author crm
 */
@Slf4j
@Component
public class ResultSalesFileJob extends AbstractFtpImportJob<FtpBroadbandMarketResult> {

    private static final String RESULT_FILE_NAME = "TC_USER_BROADBAND_MARKETING_RESULT_M_";

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private FtpBroadbandMarketResultMapper ftpBroadbandMarketResultMapper;

    // ==================== AbstractFtpImportJob 实现 ====================

    @Override
    protected String getJobName() { return "resultSalesFileJob"; }

    @Override
    protected String getConfigType() { return "JF_COMMON_FTP"; }

    @Override
    protected String getFileDateType() { return FILE_DATE_TYPE_MONTHLY; }

    @Override
    protected String getSeparator() { return "\\x1b"; }

    @Override
    protected String getFileName(String dateStr) {
        return RESULT_FILE_NAME + dateStr + ".txt";
    }

    @Override
    protected FtpBroadbandMarketResult parseRow(String[] data) {
        FtpBroadbandMarketResult result = new FtpBroadbandMarketResult();
        if (StringUtils.isNotEmpty(data[0])) result.setStatCycle(data[0]);
        if (StringUtils.isNotEmpty(data[1])) result.setMarketingType(data[1]);
        if (StringUtils.isNotEmpty(data[2])) result.setMarketingName(data[2]);
        if (StringUtils.isNotEmpty(data[3])) result.setUserMarketingId(data[3]);
        if (StringUtils.isNotEmpty(data[4])) result.setUserId(data[4]);
        if (StringUtils.isNotEmpty(data[5])) result.setSvcNum(data[5]);
        if (StringUtils.isNotEmpty(data[6])) result.setIsSucc(data[6]);
        if (StringUtils.isNotEmpty(data[7])) result.setCreateDate(data[7]);
        if (StringUtils.isNotEmpty(data[8])) result.setStartDate(data[8]);
        if (StringUtils.isNotEmpty(data[9])) result.setEndDate(data[9]);
        if (StringUtils.isNotEmpty(data[10])) result.setOrderProductCode(data[10]);
        if (StringUtils.isNotEmpty(data[11])) result.setOrderProductName(data[11]);
        if (StringUtils.isNotEmpty(data[12])) result.setOrderDiscntCode(data[12]);
        if (StringUtils.isNotEmpty(data[13])) result.setOrderDiscntName(data[13]);
        if (StringUtils.isNotEmpty(data[14])) result.setNewSpeed(data[14]);
        return result;
    }

    @Override
    protected int doInsert(List<FtpBroadbandMarketResult> batch) {
        ftpBroadbandMarketResultMapper.insertBatch(batch);
        return batch.size();
    }

    @Override
    protected int getDefaultDaysBefore() { return 30; } // 默认上月

    @Override
    protected int getBatchSize() { return 1000; }

    @Override
    protected void beforeImport() {
        log.info("开始清理{}账期历史数据", currentDateStr);
        ftpBroadbandMarketResultMapper.deleteByStatCycle(currentDateStr);
        log.info("{}账期历史数据清理完成", currentDateStr);
    }

    // ==================== XXL-Job 入口 ====================

    @XxlJob("resultSalesFileJob")
    public void resultSalesFileJob() {
        execute();
    }
}
