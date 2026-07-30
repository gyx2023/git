package com.crm.job.service.file;

import com.crm.common.utils.StringUtils;
import com.crm.job.base.AbstractFtpImportJob;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.dao.FtpBroadbandMarketUser;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.FtpBroadbandMarketUserMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 宽带专项营销文件导入 + 工作流启动
 * <p>
 * 流程：解析账期 → 下载客户/用户文件 → 解析入库 → 重建产品字典 → 调用 warningStartService 启动流程
 * <p>
 * 流程启动委托给 CustSalesWarningStrategy + WarningStartService（single_approval 通用审批流程），
 * 不再直接操作 Flowable API。
 * <p>
 * XXL-Job 参数：只传 yyyyMM 账期（如 202607），不传则默认上月
 *
 * @author crm
 */
@Slf4j
@Component
public class CustSalesFileJob extends AbstractFtpImportJob<FtpBroadbandMarketCust> {

    // ==================== 文件常量 ====================
    private static final String CUST_FILE_NAME = "TC_CUST_BROADBAND_MARKETING_M_";
    private static final String USER_FILE_NAME = "TC_USER_BROADBAND_MARKETING_M_";

    /** 用户文件本地路径（download 时赋值，afterImport 中使用） */
    private String userLocalPath;

    // ==================== 注入 ====================
    @Autowired
    private ISysParamService sysParamService;
    @Autowired
    private FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;
    @Autowired
    private FtpBroadbandMarketUserMapper ftpBroadbandMarketUserMapper;
    @Autowired
    private SysCodeMapper sysCodeMapper;
    @Autowired
    private IWarningStartService warningStartService;

    // ==================== AbstractFtpImportJob 实现 ====================

    @Override
    protected String getJobName() { return "custSalesFileJob"; }

    @Override
    protected String getConfigType() { return "broadband_market"; }

    @Override
    protected String getFileDateType() { return FILE_DATE_TYPE_MONTHLY; }

    @Override
    protected String getSeparator() { return "\\x1b"; }

    @Override
    protected String getFileName(String dateStr) {
        return CUST_FILE_NAME + dateStr + ".txt";
    }

    @Override
    protected FtpBroadbandMarketCust parseRow(String[] cols) {
        return parseCustRow(cols);
    }

    @Override
    protected int doInsert(List<FtpBroadbandMarketCust> batch) {
        ftpBroadbandMarketCustMapper.insertBatch(batch);
        return batch.size();
    }

    @Override
    protected int getDefaultDaysBefore() { return 30; } // 默认上月

    @Override
    protected int getBatchSize() { return 1000; }

    // ==================== 覆盖 download()：使用 SftpWebUtil 下载两个文件 ====================

    /**
     * 使用 SftpWebUtil 下载客户文件 + 用户文件
     * 返回客户文件路径（基类用其解析入库）
     */
    @Override
    protected String download(String configType, String dateStr, String fileName) {
        Map<String, String> param = sysParamService.findValueMapsByType(configType);
        SftpWebUtil ftpUtil = new SftpWebUtil(
            param.get("sftp.username"),
            param.get("sftp.password"),
            param.get("sftp.host"),
            MapUtils.getIntValue(param, "sftp.port")
        );

        try {
            log.info("宽带专项营销下载文件开始");
            ftpUtil.login();

            // 下载客户文件
            String custFileDirectory = param.get("sftp.dataRootPath") + dateStr;
            String custLocalPath = MapUtils.getString(param, "sftp.dataLocalPath") + File.separator + fileName;
            ftpUtil.download(custFileDirectory, fileName, custLocalPath);
            log.info("客户文件下载成功: {}", custLocalPath);

            // 下载用户文件
            String userFileName = USER_FILE_NAME + dateStr + ".txt";
            String userFileDirectory = param.get("sftp.dataRootPath") + dateStr;
            this.userLocalPath = MapUtils.getString(param, "sftp.dataLocalPath") + File.separator + userFileName;
            ftpUtil.download(userFileDirectory, userFileName, this.userLocalPath);
            log.info("用户文件下载成功: {}", this.userLocalPath);

            if (!new File(custLocalPath).exists()) {
                log.error("【{}】客户文件下载后不存在: {}", getJobName(), custLocalPath);
                return null;
            }
            return custLocalPath;
        } catch (Exception e) {
            log.error("宽带专项营销文件下载失败", e);
            return null;
        } finally {
            ftpUtil.logout();
        }
    }

    // ==================== 覆盖 beforeImport()：清理历史数据 ====================

    @Override
    protected void beforeImport() {
        log.info("开始清理{}账期历史数据", currentDateStr);
        ftpBroadbandMarketCustMapper.deleteByStatCycle(currentDateStr);
        ftpBroadbandMarketUserMapper.deleteByStatCycle(currentDateStr);
        log.info("{}账期历史数据清理完成", currentDateStr);
    }

    // ==================== 覆盖 afterImport()：用户文件入库 + 重建字典 + 委托启动流程 ====================

    @Override
    protected void afterImport(int custCount) {
        // 1. 解析用户文件并入库
        log.info("开始解析用户文件: {}", userLocalPath);
        int userCount = parseAndInsertUserFile(userLocalPath);
        log.info("用户文件入库完成，共 {} 条", userCount);

        // 2. 重建产品字典
        sysCodeMapper.deleteByType("cust_sale_product");
        sysCodeMapper.insertCustSaleProduct();
        log.info("产品字典重建完成");

        // 3. 委托 WarningStartService 启动流程
        //  CustSalesWarningStrategy 会从 ftp_broadband_market_cust 表查询未生成订单的记录
        //  逐条启动 single_approval 流程（流程定义 key=single_approval），创建工单并设置任务变量
        warningStartService.startByType("cust_sales", currentDateStr);

        log.info("【{}】完成: 客户{}条, 用户{}条", getJobName(), custCount, userCount);
    }

    // ==================== 客户文件解析 ====================

    private FtpBroadbandMarketCust parseCustRow(String[] data) {
        FtpBroadbandMarketCust cust = new FtpBroadbandMarketCust();
        if (StringUtils.isNotEmpty(data[0])) { cust.setStatCycle(data[0]); }
        if (StringUtils.isNotEmpty(data[1])) { cust.setCustMarketingId(data[1]); }
        if (StringUtils.isNotEmpty(data[2])) { cust.setAreaName(data[2]); }
        if (StringUtils.isNotEmpty(data[3])) { cust.setCountyName(data[3]); }
        if (StringUtils.isNotEmpty(data[4])) { cust.setCustGrpId(data[4]); }
        if (StringUtils.isNotEmpty(data[5])) { cust.setGrpCode(data[5]); }
        if (StringUtils.isNotEmpty(data[6])) { cust.setGrpName(data[6]); }
        if (StringUtils.isNotEmpty(data[7])) { cust.setServManagerId(data[7]); }
        if (StringUtils.isNotEmpty(data[8])) { cust.setServManagerName(data[8]); }
        if (StringUtils.isNotEmpty(data[9])) { cust.setGrpExpandType(data[9]); }
        if (StringUtils.isNotEmpty(data[10])) { cust.setGrpBusiDeptType(data[10]); }
        if (StringUtils.isNotEmpty(data[11])) { cust.setMarketingType(data[11]); }
        if (StringUtils.isNotEmpty(data[12])) { cust.setMarketingName(data[12]); }
        if (StringUtils.isNotEmpty(data[13])) { cust.setUserScale(data[13]); }
        return cust;
    }

    // ==================== 用户文件解析 ====================

    private int parseAndInsertUserFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("宽带专项营销用户文件不存在: {}", filePath);
            return 0;
        }

        int total = 0;
        List<FtpBroadbandMarketUser> batch = new ArrayList<>(1000);

        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split("\\x1b", -1);
                FtpBroadbandMarketUser user = parseUserRow(data);
                batch.add(user);

                if (batch.size() >= 1000) {
                    ftpBroadbandMarketUserMapper.insertBatch(batch);
                    total += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                ftpBroadbandMarketUserMapper.insertBatch(batch);
                total += batch.size();
            }
        } catch (IOException e) {
            log.error("用户文件读取失败", e);
        }
        return total;
    }

    private FtpBroadbandMarketUser parseUserRow(String[] data) {
        FtpBroadbandMarketUser user = new FtpBroadbandMarketUser();
        if (StringUtils.isNotEmpty(data[0])) { user.setStatCycle(data[0]); }
        if (StringUtils.isNotEmpty(data[1])) { user.setMarketingType(data[1]); }
        if (StringUtils.isNotEmpty(data[2])) { user.setMarketingName(data[2]); }
        if (StringUtils.isNotEmpty(data[3])) { user.setUserMarketingId(data[3]); }
        if (StringUtils.isNotEmpty(data[4])) { user.setAreaName(data[4]); }
        if (StringUtils.isNotEmpty(data[5])) { user.setUserId(data[5]); }
        if (StringUtils.isNotEmpty(data[6])) { user.setSvcNum(data[6]); }
        if (StringUtils.isNotEmpty(data[7])) { user.setCountyName(data[7]); }
        if (StringUtils.isNotEmpty(data[8])) { user.setOpenDate(data[8]); }
        if (StringUtils.isNotEmpty(data[9])) { user.setCustKind(data[9]); }
        if (StringUtils.isNotEmpty(data[10])) { user.setCustId(data[10]); }
        if (StringUtils.isNotEmpty(data[11])) { user.setCustName(data[11]); }
        if (StringUtils.isNotEmpty(data[12])) { user.setCertType(data[12]); }
        if (StringUtils.isNotEmpty(data[13])) { user.setIsGrpKey(data[13]); }
        if (StringUtils.isNotEmpty(data[14])) { user.setCustGrpId(data[14]); }
        if (StringUtils.isNotEmpty(data[15])) { user.setGrpCode(data[15]); }
        if (StringUtils.isNotEmpty(data[16])) { user.setGrpName(data[16]); }
        if (StringUtils.isNotEmpty(data[17])) { user.setServManagerId(data[17]); }
        if (StringUtils.isNotEmpty(data[18])) { user.setServManagerName(data[18]); }
        if (StringUtils.isNotEmpty(data[19])) { user.setGrpExpandType(data[19]); }
        if (StringUtils.isNotEmpty(data[20])) { user.setGrpBusiDeptType(data[20]); }
        if (StringUtils.isNotEmpty(data[21])) { user.setProdId(data[21]); }
        if (StringUtils.isNotEmpty(data[22])) { user.setProdName(data[22]); }
        if (StringUtils.isNotEmpty(data[23])) { user.setUserStatus(data[23]); }
        if (StringUtils.isNotEmpty(data[24])) { user.setHqSvcType(data[24]); }
        if (StringUtils.isNotEmpty(data[25])) { user.setCustCountyName(data[25]); }
        if (StringUtils.isNotEmpty(data[26])) { user.setDeveloperId(data[26]); }
        if (StringUtils.isNotEmpty(data[27])) { user.setDeveloperName(data[27]); }
        if (StringUtils.isNotEmpty(data[29])) { user.setUserAddr(data[29]); }
        if (StringUtils.isNotEmpty(data[30])) { user.setCycleStartTime(data[30]); }
        if (StringUtils.isNotEmpty(data[31])) { user.setCycleEndTime(data[31]); }
        if (StringUtils.isNotEmpty(data[32])) { user.setCycleLength(data[32]); }
        if (StringUtils.isNotEmpty(data[33])) { user.setCycleFee(data[33]); }
        if (StringUtils.isNotEmpty(data[34])) { user.setMktProductId1(data[34]); }
        if (StringUtils.isNotEmpty(data[35])) { user.setMktProductName1(data[35]); }
        if (StringUtils.isNotEmpty(data[36])) { user.setMktDiscntCode1(data[36]); }
        if (StringUtils.isNotEmpty(data[37])) { user.setMktDiscntName1(data[37]); }
        if (StringUtils.isNotEmpty(data[38])) { user.setMktDiscntFee1(data[38]); }
        if (StringUtils.isNotEmpty(data[39])) { user.setMktProductId2(data[39]); }
        if (StringUtils.isNotEmpty(data[40])) { user.setMktProductName2(data[40]); }
        if (StringUtils.isNotEmpty(data[41])) { user.setMktDiscntCode2(data[41]); }
        if (StringUtils.isNotEmpty(data[42])) { user.setMktDiscntName2(data[42]); }
        if (StringUtils.isNotEmpty(data[43])) { user.setMktDiscntFee2(data[43]); }
        return user;
    }

    // ==================== XXL-Job 入口 ====================

    @XxlJob("custSalesFileJob")
    public void custSalesFileJob() {
        execute();
    }
}
