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
 * 从 SFTP 下载结果文件（TC_USER_BROADBAND_MARKETING_RESULT_M_yyyyMM.txt）并入库到 ftp_broadband_market_result 表。
 * </p>
 * <p>
 * <b>与 CustSalesFileJob 的关系：</b><br>
 * CustSalesFileJob 导入客户/用户文件（营销目标）并启动工作流；<br>
 * ResultSalesFileJob 导入受理结果文件（营销执行结果，如是否成功办理、办理的产品等）。<br>
 * 两个 Job 独立运行，ResultSalesFileJob 只做数据入库，不启动流程。
 * </p>
 * <p>
 * XXL-Job 参数：只传 yyyyMM 账期（如 202607），不传则默认上月。
 * </p>
 *
 * @author crm
 * @see CustSalesFileJob
 */
@Slf4j
@Component
public class ResultSalesFileJob extends AbstractFtpImportJob<FtpBroadbandMarketResult> {

    /** 结果文件前缀（月文件），完整文件名如：TC_USER_BROADBAND_MARKETING_RESULT_M_202607.txt */
    private static final String RESULT_FILE_NAME = "TC_USER_BROADBAND_MARKETING_RESULT_M_";

    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private FtpBroadbandMarketResultMapper ftpBroadbandMarketResultMapper;

    // ==================== AbstractFtpImportJob 实现 ====================

    @Override
    protected String getJobName() { return "resultSalesFileJob"; }

    /** 使用公共 FTP 配置（JF_COMMON_FTP），因为结果文件可能与客户文件不在同一台 SFTP */
    @Override
    protected String getConfigType() { return "JF_COMMON_FTP"; }

    /** 月文件类型，基类按 yyyyMM 格式解析 XXL 参数 */
    @Override
    protected String getFileDateType() { return FILE_DATE_TYPE_MONTHLY; }

    /** 字段分隔符：\x1b（ASCII 分隔符），与客户文件一致 */
    @Override
    protected String getSeparator() { return "\\x1b"; }

    @Override
    protected String getFileName(String dateStr) {
        return RESULT_FILE_NAME + dateStr + ".txt";
    }

    /**
     * 行解析：将分隔符分割的字段数组映射为 FtpBroadbandMarketResult 实体
     * <p>
     * 字段顺序（共15列）：
     * <ol>
     *   <li>statCycle - 账期（yyyyMM）</li>
     *   <li>marketingType - 营销类型编码</li>
     *   <li>marketingName - 营销类型名称</li>
     *   <li>userMarketingId - 用户营销ID</li>
     *   <li>userId - 用户ID</li>
     *   <li>svcNum - 服务号码</li>
     *   <li>isSucc - 是否成功（Y/N）</li>
     *   <li>createDate - 受理创建日期</li>
     *   <li>startDate - 生效日期</li>
     *   <li>endDate - 失效日期</li>
     *   <li>orderProductCode - 订购产品编码</li>
     *   <li>orderProductName - 订购产品名称</li>
     *   <li>orderDiscntCode - 订购优惠编码</li>
     *   <li>orderDiscntName - 订购优惠名称</li>
     *   <li>newSpeed - 新速率</li>
     * </ol>
     */
    @Override
    protected FtpBroadbandMarketResult parseRow(String[] data) {
        FtpBroadbandMarketResult result = new FtpBroadbandMarketResult();
        if (StringUtils.isNotEmpty(data[0])) { result.setStatCycle(data[0]); }           // 账期
        if (StringUtils.isNotEmpty(data[1])) { result.setMarketingType(data[1]); }        // 营销类型
        if (StringUtils.isNotEmpty(data[2])) { result.setMarketingName(data[2]); }        // 营销名称
        if (StringUtils.isNotEmpty(data[3])) { result.setUserMarketingId(data[3]); }      // 用户营销ID
        if (StringUtils.isNotEmpty(data[4])) { result.setUserId(data[4]); }               // 用户ID
        if (StringUtils.isNotEmpty(data[5])) { result.setSvcNum(data[5]); }               // 服务号码
        if (StringUtils.isNotEmpty(data[6])) { result.setIsSucc(data[6]); }               // 是否成功
        if (StringUtils.isNotEmpty(data[7])) { result.setCreateDate(data[7]); }           // 受理日期
        if (StringUtils.isNotEmpty(data[8])) { result.setStartDate(data[8]); }            // 生效日期
        if (StringUtils.isNotEmpty(data[9])) { result.setEndDate(data[9]); }              // 失效日期
        if (StringUtils.isNotEmpty(data[10])) { result.setOrderProductCode(data[10]); }   // 产品编码
        if (StringUtils.isNotEmpty(data[11])) { result.setOrderProductName(data[11]); }   // 产品名称
        if (StringUtils.isNotEmpty(data[12])) { result.setOrderDiscntCode(data[12]); }    // 优惠编码
        if (StringUtils.isNotEmpty(data[13])) { result.setOrderDiscntName(data[13]); }    // 优惠名称
        if (StringUtils.isNotEmpty(data[14])) { result.setNewSpeed(data[14]); }           // 新速率
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

    /**
     * 入库前清理：按账期删除历史结果数据，保证幂等性
     * <p>
     * 不覆盖 download()，使用基类默认的 SftpUtil 下载逻辑。
     * 远程路径格式由基类 getRemotePath() 控制：{dateStr}/{fileName}
     */
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
