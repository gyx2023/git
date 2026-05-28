package com.crm.job.service.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.core.domain.dto.WorkOrderDTO;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.WxStrategyService;
import com.crm.system.service.cust.CustTCustomerProvinceService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 维系策略任务文件下发 Job
 *
 * @Author: gaoyy
 * @DateTime: 2023/04/01 17:55
 */
@Component
public class WxStrategyJob {

    private static final Log log = LogFactory.getLog(WxStrategyJob.class);

    // ====================== 静态常量 ======================
    public static final String YEAR_MONTH_DAY_PATTERN = "yyyyMMdd";
    private static final String SPLIT_STR = "\u0001";
    public static final String PROVINCE = "36";
    public static final String MARKETING_RANGE_NAME = "浙江省分公司";
    public static final String START_DATE = "2023-01-01";
    public static final String END_DATE = "2050-12-31";
    public static final String SEND_TYPE = "3";
    public static final String BUSINESS_NUM = "0";
    public static final String DEVELOP_USER_NUM = "0";
    public static final String EXPECTED_INCOME = "0";
    public static final String STATUS = "5";
    public static final String TENANT_ID = "36";
    public static final String ORG_NAME = "浙江省分公司";
    public static final String GRADE = "PROV";
    public static final String GMT_CREATE = "2023-01-01 00:00:00";
    public static final String CREATE_USER_ID = "wangdx88";
    public static final String CREATE_USER_NAME = "王东霞";
    public static final String CREATE_USER_ORG = "浙江省联通集团政企客户事业群";
    public static final String GMT_MODIFIED = "2023-01-01 00:00:00";
    public static final String UPDATE_USER_ID = "wangdx88";
    public static final String UPDATE_USER_NAME = "王东霞";
    public static final String POLICY_TYPE = "1";
    public static final String IS_QUANTIZED = "0";
    public static final String CUSTOMER_PROPERTY = "1";
    public static final String IS_ENABLE_MODEL = "0";
    public static final String IS_SMS_NOTIFICATION = "0";
    public static final String CUSTOM_GROUP_NAME = "浙江省政企客户";
    public static final String EXPECTED_BUSINESS_NUM = "0";
    public static final String BUSINESS_COMPLETE_NUM = "0";
    public static final String DEV_COMPLETE_NUM = "0";
    public static final String INCOME_COMPLETE_NUM = "0";
    public static final String WAIT_CUST_NUM = "0";
    public static final String IS_VISIT_NUM = "0";
    public static final String IS_BUSIOPPT_NUM = "0";
    public static final String IS_ASSIGN = "0";
    public static final String CREATE_POLICY_GRADE = "PROV";
    public static final String TOP_ORG_GRADE = "PROV";
    public static final String PROV_CODE = "36";
    public static final String PROV_NAME = "浙江省分公司";
    public static final String BUSINESS_COMPLETE_RATE = "0";
    public static final String DEV_COMPLETE_RATE = "0";
    public static final String INCOME_COMPLETE_RATE = "0";
    public static final String SIGN_NUM = "0";
    public static final String DEVELOP_NUM = "0";
    public static final String DEADLINE = "2050-12-31 23:59:59";
    public static final String REGION_PROV_NAME = "浙江";

    // 文件名前缀
    private static final String FTP_SEND_STRATEGY_INFO_FILE_NAME = "zhejiang_strategy_info_";
    private static final String FTP_SEND_STRATEGY_MONITOR_FILE_NAME = "zhejiang_strategy_monitor_";
    private static final String FTP_SEND_WORK_ORDER_FILE_NAME = "zhejiang_work_order_";

    // 地市编码映射
    public static final Map<String, String> COUNTRY_CODE_MAP;

    static {
        COUNTRY_CODE_MAP = new HashMap<>();
        COUNTRY_CODE_MAP.put("3300", "");
        COUNTRY_CODE_MAP.put("3301", "V0330100");
        COUNTRY_CODE_MAP.put("3302", "V0330200");
        COUNTRY_CODE_MAP.put("3303", "V0330300");
        COUNTRY_CODE_MAP.put("3304", "V0330400");
        COUNTRY_CODE_MAP.put("3305", "V0330500");
        COUNTRY_CODE_MAP.put("3306", "V0330600");
        COUNTRY_CODE_MAP.put("3307", "V0330700");
        COUNTRY_CODE_MAP.put("3308", "V0330800");
        COUNTRY_CODE_MAP.put("3309", "V0330900");
        COUNTRY_CODE_MAP.put("3310", "V0332600");
        COUNTRY_CODE_MAP.put("3311", "V0332500");
    }

    // SFTP 配置
    private static String ftpUsername;
    private static String ftpPassword;
    private static String ftpHost;
    private static String ftpPort;
    private static String ftDirectory;
    private static String localPath;

    // ====================== 注入服务 ======================
    @Resource
    private ISysParamService sysParamService;
    @Resource
    private WxStrategyService wxStrategyService;
    @Resource
    private CustTCustomerProvinceService customerProvinceService;

    /**
     * 初始化静态参数
     */

    public void initStaticParam() {
        Map<String, String> param = sysParamService.findValueMapsByType("tc_wx_strategy");
        ftpUsername = param.get("ftp_username");
        ftpPassword = param.get("ftp_password");
        ftpHost = param.get("ftp_host");
        ftpPort = param.get("ftp_port");
        localPath = param.get("ftp_local_new");
        ftDirectory = param.get("ftp_directory");
        log.info("SFTP 配置初始化完成：host=" + ftpHost + ", dir=" + ftDirectory);
    }

    // ====================== XXL-JOB 任务 ======================

    /**
     * 策略中心 - 任务工单信息文件下发
     */
    @XxlJob(value = "sendWorkOrderJob")
    public void sendWorkOrder() {
        initStaticParam();
        String yyyyMMdd = getJobDate();
        log.info("====== 任务工单文件下发开始，日期：" + yyyyMMdd + " ======");
        sendsendWorkOrder(yyyyMMdd);
        log.info("====== 任务工单文件下发结束 ======");
    }

    /**
     * 策略中心 - 策略主信息文件下发
     */
    @XxlJob(value = "sendStrategyInfoJob")
    public void sendStrategy() {
        initStaticParam();
        String yyyyMMdd = getJobDate();
        log.info("====== 策略主信息文件下发开始，日期：" + yyyyMMdd + " ======");
        sendStrategyInfo(yyyyMMdd);
        log.info("====== 策略主信息文件下发结束 ======");
    }

    /**
     * 策略中心 - 策略监控信息文件下发
     */
    @XxlJob(value = "sendStrategyMonitorJob")
    public void sendMonitor() {
        initStaticParam();
        String yyyyMMdd = getJobDate();
        log.info("====== 策略监控文件下发开始，日期：" + yyyyMMdd + " ======");
        sendStrategyMonitor(yyyyMMdd);
        log.info("====== 策略监控文件下发结束 ======");
    }

    // ====================== 核心业务方法 ======================

    /**
     * 下发任务工单文件
     */
    public void sendsendWorkOrder(String yyyyMMdd) {
        String fileName = FTP_SEND_WORK_ORDER_FILE_NAME + yyyyMMdd;
        String localFile = localPath + File.separator + fileName;

        try (SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort))) {
            // 生成文件
            if (createWorkOrderFile(localFile, yyyyMMdd) != 0) {
                log.error("任务工单文件生成失败，终止上传");
                return;
            }
            // 上传
            try (InputStream input = new FileInputStream(localFile)) {
                ftpUtil.login();
                ftpUtil.upload("/", ftDirectory, fileName, input);
                log.info("任务工单文件上传成功：" + fileName);
                ftpUtil.close();
            }
        } catch (Exception e) {
            log.error("任务工单文件下发异常", e);
        }
    }

    /**
     * 下发策略主信息文件
     */
    public void sendStrategyInfo(String yyyyMMdd) {
        String fileName = FTP_SEND_STRATEGY_INFO_FILE_NAME + yyyyMMdd;
        String localFile = localPath + File.separator + fileName;

        try (SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort))) {
            if (createStrategyInfoFile(localFile, yyyyMMdd) != 0) {
                log.error("策略主文件生成失败，终止上传");
                return;
            }
            try (InputStream input = new FileInputStream(localFile)) {
                ftpUtil.login();
                ftpUtil.upload("/", ftDirectory, fileName, input);
                log.info("策略主文件上传成功：" + fileName);
                ftpUtil.close();
            }
        } catch (Exception e) {
            log.error("策略主文件下发异常", e);
        }
    }

    /**
     * 下发策略监控文件
     */
    public void sendStrategyMonitor(String yyyyMMdd) {
        String fileName = FTP_SEND_STRATEGY_MONITOR_FILE_NAME + yyyyMMdd;
        String localFile = localPath + File.separator + fileName;

        try (SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort))) {
            if (createStrategyMonitorFile(localFile, yyyyMMdd) != 0) {
                log.error("策略监控文件生成失败，终止上传");
                return;
            }
            try (InputStream input = new FileInputStream(localFile)) {
                ftpUtil.login();
                ftpUtil.upload("/", ftDirectory, fileName, input);
                log.info("策略监控文件上传成功：" + fileName);
                ftpUtil.close();
            }
        } catch (Exception e) {
            log.error("策略监控文件下发异常", e);
        }
    }

    // ====================== 文件生成 ======================

    /**
     * 生成任务工单文件
     */
    public int createWorkOrderFile(String path, String yyyyMMdd) {
        int flag = 0;

        try {
            // 自动创建父目录 + 新建文件，已存在则覆盖
            File file = FileUtil.touch(path);
            // 1. 解析日期（提前处理）
            String dateStr = LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString();

            // 2. 获取策略
            Map<String, JSONObject> strategyMap = wxStrategyService.queryStrategyInfo();
            if (ObjectUtil.isEmpty(strategyMap)) {
                log.warn("策略信息为空");
                return 0;
            }

            // ======================== 核心优化 ========================
            // 3. 一次性查询所有工单 & 收集客户ID（只查1轮库，不重复查）
            Map<String, List<WorkOrderDTO>> strategyWorkOrderMap = new HashMap<>();
            Set<String> customIdSet = new HashSet<>();

            for (Map.Entry<String, JSONObject> entry : strategyMap.entrySet()) {
                String key = entry.getKey();
                JSONObject strategy = entry.getValue();
                if (strategy == null) {
                    continue;
                }

                // 每个策略只查一次！！！
                List<WorkOrderDTO> workOrderList = wxStrategyService.queryWorkOrder(key, dateStr);
                if (ObjectUtil.isEmpty(workOrderList)) {
                    continue;
                }

                // 放入缓存
                strategyWorkOrderMap.put(key, workOrderList);

                // 收集客户ID，用于后续批量查属性
                for (WorkOrderDTO dto : workOrderList) {
                    if (ObjectUtil.isNotEmpty(dto.getCustomId())) {
                        customIdSet.add(dto.getCustomId());
                    }
                }
            }

            // 4. 批量查询客户属性（只查1次库）
            Map<String, String> custPropMap = customerProvinceService.getCustomerPropertyBatch(new ArrayList<>(customIdSet));

            // ======================== 文件写入 ========================
            // 安全创建文件
            if (file.exists()) {
                file.delete();
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();

            // UTF-8 编码写入
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

                for (Map.Entry<String, JSONObject> entry : strategyMap.entrySet()) {
                    String key = entry.getKey();
                    JSONObject strategy = entry.getValue();
                    if (strategy == null) {
                        continue;
                    }

                    // 从内存取，不再查库！
                    List<WorkOrderDTO> workOrderList = strategyWorkOrderMap.get(key);
                    if (ObjectUtil.isEmpty(workOrderList)) {
                        continue;
                    }

                    log.info("策略类型【" + key + "】，待下发工单数：" + workOrderList.size());


                    String topPolicyCode = strategy.getString("top_policy_code");
                    String businessScene = strategy.getString("business_scene");

                    for (WorkOrderDTO workOrder : workOrderList) {
                        // 内存取值
                        String customerProperty = custPropMap.getOrDefault(workOrder.getCustomId(), "2");

                        StringBuilder sb = new StringBuilder();
                        sb.append(ifNull(workOrder.getWorkOrderCode())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getWorkOrderStatus())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getWorkOrderName())).append(SPLIT_STR)
                            .append(ifNull(topPolicyCode)).append(SPLIT_STR)
                            .append(ifNull(workOrder.getWorkOrderTime())).append(SPLIT_STR)
                            .append(DEADLINE).append(SPLIT_STR)
                            .append("").append(SPLIT_STR)
                            .append(PROV_CODE).append(SPLIT_STR)
                            .append(COUNTRY_CODE_MAP.getOrDefault(workOrder.getAreaId(), "")).append(SPLIT_STR)
                            .append(ifNull(workOrder.getOrgName())).append(SPLIT_STR)
                            .append("").append(SPLIT_STR)
                            .append("").append(SPLIT_STR).append("").append(SPLIT_STR)
                            .append("").append(SPLIT_STR).append("").append(SPLIT_STR)
                            .append("").append(SPLIT_STR).append("").append(SPLIT_STR)
                            .append(ifNull(workOrder.getManagerNo())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getManagerName())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getManagerMobile())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getActualManagerNo())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getActualManagerName())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getActualManagerMobile())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getBuildId())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getBuildName())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getCustomId())).append(SPLIT_STR)
                            .append(ifNull(workOrder.getCustomName())).append(SPLIT_STR)
                            .append(ifNull(customerProperty)).append(SPLIT_STR)
                            .append(ifNull(workOrder.getGmtCreate())).append(SPLIT_STR)
                            .append("").append(SPLIT_STR)
                            .append(CREATE_USER_ID).append(SPLIT_STR).append(CREATE_USER_NAME).append(SPLIT_STR)
                            .append(UPDATE_USER_ID).append(SPLIT_STR).append(UPDATE_USER_NAME).append(SPLIT_STR)
                            .append(ifNull(workOrder.getBackTime())).append(SPLIT_STR).append("1").append(SPLIT_STR)
                            .append("12".equals(businessScene) ? "" : businessScene).append(SPLIT_STR)
                            .append(ifNull(workOrder.getCategory())).append(SPLIT_STR)
                            .append(workOrder.getBusinessEndDate()).append(SPLIT_STR)
                            .append("10000").append(SPLIT_STR)
                            .append("htdq_warn".equals(key) ? ifNull(workOrder.getContractName()) : "").append(SPLIT_STR)
                            .append("htdq_warn".equals(key) ? ifNull(workOrder.getCustNo()) : "").append(SPLIT_STR)
                            .append("htdq_warn".equals(key) ? ifNull(workOrder.getCustomName()) : "").append(SPLIT_STR)
                            .append("terminal_contract_warn".equals(key) ? ifNull(workOrder.getSvcNum()) : "").append(SPLIT_STR)
                            .append("htdq_warn".equals(key) ? ifNull(workOrder.getContractCode()) : "");

                        bw.write(sb.toString());
                        bw.newLine();
                    }
                }
            }
            log.info("工单文件生成成功");
        } catch (Exception e) {
            flag = 1;
            log.error("生成任务工单文件异常", e);
        }
        return flag;
    }

    /**
     * 生成策略主信息文件
     */
    public int createStrategyInfoFile(String path, String yyyyMMdd) {
        int flag = 0;

        try {
            // 自动创建父目录 + 新建文件，已存在则覆盖
            File file = FileUtil.touch(path);
            Map<String, JSONObject> strategyMap = wxStrategyService.queryStrategyInfo();
            String dateStr = LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (Map.Entry<String, JSONObject> entry : strategyMap.entrySet()) {
                    JSONObject strategy = entry.getValue();
                    if (strategy == null) {
                        continue;
                    }

                    String key = entry.getKey();
                    String custCount = wxStrategyService.queryCustCount(key, dateStr);
                    String orderCount = wxStrategyService.queryOrderCount(key, dateStr);
                    String policyCode = strategy.getString("policy_code");
                    String topPolicyCode = strategy.getString("top_policy_code");
                    String businessScene = strategy.getString("business_scene");
                    String description = strategy.getString("description");
                    String name = strategy.getString("name");
                    String customGroupCode = "C" + System.nanoTime();

                    String line = String.join(SPLIT_STR,
                        ifNull(policyCode),
                        ifNull(topPolicyCode),
                        ifNull(PROVINCE),
                        ifNull(customGroupCode),
                        ifNull(businessScene),
                        ifNull(START_DATE),
                        ifNull(END_DATE),
                        ifNull(description),
                        ifNull(SEND_TYPE),
                        ifNull(STATUS),
                        ifNull(custCount),
                        ifNull(name),
                        "",
                        ifNull(TENANT_ID),
                        ifNull(GMT_CREATE),
                        ifNull(CREATE_USER_ID),
                        ifNull(CREATE_USER_NAME),
                        ifNull(POLICY_TYPE),
                        ifNull(orderCount),
                        ifNull(CUSTOMER_PROPERTY),
                        ifNull(CUSTOM_GROUP_NAME)
                    );

                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (Exception e) {
            flag = 1;
            log.error("生成策略主文件异常", e);
        }
        return flag;
    }

    /**
     * 生成策略监控文件
     */
    public int createStrategyMonitorFile(String path, String yyyyMMdd) {
        int flag = 0;

        try {
            // 自动创建父目录 + 新建文件，已存在则覆盖
            File file = FileUtil.touch(path);
            Map<String, JSONObject> strategyMap = wxStrategyService.queryStrategyInfo();
            String dateStr = LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString();
            String acctDate = dateStr + " 00:00:00";

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (Map.Entry<String, JSONObject> entry : strategyMap.entrySet()) {
                    JSONObject strategy = entry.getValue();
                    if (strategy == null) {
                        continue;
                    }

                    String key = entry.getKey();
                    String custCount = wxStrategyService.queryCustCount(key, dateStr);
                    String orderCount = wxStrategyService.queryOrderCount(key, dateStr);
                    String closedOrderCount = wxStrategyService.queryClosedOrderCount(key, dateStr);
                    String policyCode = strategy.getString("policy_code");
                    String topPolicyCode = strategy.getString("top_policy_code");
                    String policyName = strategy.getString("name");

                    String line = String.join(SPLIT_STR,
                        ifNull(acctDate),
                        ifNull(policyCode),
                        ifNull(policyName),
                        "",
                        ifNull(topPolicyCode),
                        ifNull(PROVINCE),
                        ifNull(START_DATE),
                        ifNull(END_DATE),
                        ifNull(EXPECTED_BUSINESS_NUM),
                        ifNull(BUSINESS_COMPLETE_NUM),
                        ifNull(DEVELOP_USER_NUM),
                        ifNull(DEV_COMPLETE_NUM),
                        ifNull(EXPECTED_INCOME),
                        ifNull(INCOME_COMPLETE_NUM),
                        ifNull(custCount),
                        ifNull(custCount),
                        ifNull(WAIT_CUST_NUM),
                        ifNull(custCount),
                        ifNull(IS_VISIT_NUM),
                        ifNull(IS_BUSIOPPT_NUM),
                        ifNull(IS_ASSIGN),
                        "",
                        ifNull(ORG_NAME),
                        ifNull(CREATE_POLICY_GRADE),
                        ifNull(TOP_ORG_GRADE),
                        ifNull(STATUS),
                        "", "",
                        ifNull(PROV_CODE),
                        ifNull(PROV_NAME),
                        "", "", "", "", "", "",
                        ifNull(BUSINESS_COMPLETE_RATE),
                        ifNull(DEV_COMPLETE_RATE),
                        ifNull(INCOME_COMPLETE_RATE),
                        ifNull(CREATE_USER_ID),
                        ifNull(CREATE_USER_NAME),
                        ifNull(CREATE_USER_ORG),
                        ifNull(CUSTOMER_PROPERTY),
                        ifNull(orderCount),
                        ifNull(closedOrderCount)
                    );

                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (Exception e) {
            flag = 1;
            log.error("生成策略监控文件异常", e);
        }
        return flag;
    }

    // ====================== 工具方法 ======================

    /**
     * 获取任务日期（支持参数覆盖）
     */
    private String getJobDate() {
        String jobParam = XxlJobHelper.getJobParam();
        if (ObjectUtil.isNotEmpty(jobParam)) {
            return jobParam.trim();
        }
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    /**
     * 空值处理
     */
    private String ifNull(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
