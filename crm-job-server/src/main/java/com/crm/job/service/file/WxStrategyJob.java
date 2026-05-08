package com.crm.job.service.file;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.ssh.Sftp;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.core.domain.dto.WorkOrderDTO;
import com.crm.job.utils.SftpUtil;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.WxStrategyService;
import com.crm.system.service.cust.CustTCustomerProvinceService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: gaoyy
 * @Description: 下发维系策略任务文件
 * @DateTime: 2023/04/01 17:55
 **/

public class WxStrategyJob {

    @Resource
    ISysParamService sysParamService;
    @Resource
    WxStrategyService wxStrategyService;
    @Resource
    CustTCustomerProvinceService customerProvinceService;

    public static final String YEAR_MONTH_DAY_PATTERN = "yyyyMMdd";

    // 下发的文件名前缀
    private static final String FTP_SEND_STRATEGY_INFO_FILE_NAME = "zhejiang_strategy_info_";
    private static final String FTP_SEND_STRATEGY_MONITOR_FILE_NAME = "zhejiang_strategy_monitor_";
    private static final String ftpSendWorkOrderFileName = "zhejiang_work_order_";
    private static final String split_str = "\u0001";
    public static String yyyyMMdd;

    private static final Log log = LogFactory.getLog(WxStrategyJob.class);
    // 省份
    public static final String PROVINCE = "36";
    // 营销范围名称
    public static final String MARKETING_RANGE_NAME = "浙江省分公司";
    // 开始时间
    public static final String START_DATE = "2023-01-01";
    // 结束时间
    public static final String END_DATE = "2050-12-31";
    // 下发方式
    public static final String SEND_TYPE = "3";
    // 商机数
    public static final String BUSINESS_NUM = "0";
    // 发展目标
    public static final String DEVELOP_USER_NUM = "0";
    // 目标收入
    public static final String EXPECTED_INCOME = "0";
    // 策略状态
    public static final String STATUS = "5";
    // 租户id
    public static final String TENANT_ID = "36";
    // 当前营销单元名称
    public static final String ORG_NAME = "浙江省分公司";
    // 当前营销单元级别
    public static final String GRADE = "PROV";
    // 创建时间
    public static final String GMT_CREATE = "2023-01-01 00:00:00";
    // 创建人OA工号
    public static final String CREATE_USER_ID = "wangdx88";
    // 创建人姓名
    public static final String CREATE_USER_NAME = "王东霞";
    // 创建人部门
    public static final String CREATE_USER_ORG = "浙江省联通集团政企客户事业群";
    // 修改时间
    public static final String GMT_MODIFIED = "2023-01-01 00:00:00";
    // 修改人ID
    public static final String UPDATE_USER_ID = "wangdx88";
    // 修改人姓名
    public static final String UPDATE_USER_NAME = "王东霞";
    // 策略类型
    public static final String POLICY_TYPE = "1";
    // 是否量化: 0-非量化，1-量化
    public static final String IS_QUANTIZED = "0";
    // 客户属性: 1-政企要客; 2-商企客户
    public static final String CUSTOMER_PROPERTY = "1";
    // 是否启用模型: 0-未启用，1-启用
    public static final String IS_ENABLE_MODEL = "0";
    // 是否短信提醒: 0-不启用，1-启用，默认为0
    public static final String IS_SMS_NOTIFICATION = "0";
    // 客户群名称
    public static final String CUSTOM_GROUP_NAME = "浙江省政企客户";

    // 商机目标数
    public static final String EXPECTED_BUSINESS_NUM = "0";
    // 商机完成数
    public static final String BUSINESS_COMPLETE_NUM = "0";
    // 发展完成数
    public static final String DEV_COMPLETE_NUM = "0";
    // 完成收入
    public static final String INCOME_COMPLETE_NUM = "0";
    // 待跟进客户数
    public static final String WAIT_CUST_NUM = "0";
    // 拜访成功数
    public static final String IS_VISIT_NUM = "0";
    // 转商机客户数
    public static final String IS_BUSIOPPT_NUM = "0";
    // 是否分解(当前节点是否下发) 1下发 0未下发
    public static final String IS_ASSIGN = "0";
    // 策略所属的营销单元级别
    public static final String CREATE_POLICY_GRADE = "PROV";
    // 顶级策略营销单元等级
    public static final String TOP_ORG_GRADE = "PROV";
    // 省份编码
    public static final String PROV_CODE = "36";
    // 省份名称
    public static final String PROV_NAME = "浙江省分公司";
    // 商机完成率
    public static final String BUSINESS_COMPLETE_RATE = "0";
    // 发展完成率
    public static final String DEV_COMPLETE_RATE = "0";
    // 收入完成率
    public static final String INCOME_COMPLETE_RATE = "0";

    // 战略签约数
    public static final String SIGN_NUM = "0";
    // 发展用户数
    public static final String DEVELOP_NUM = "0";
    // 任务截止时间
    public static final String DEADLINE = "2050-12-31 23:59:59";
    // 地域省份名称
    public static final String REGION_PROV_NAME = "浙江";

    public static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<>();

    private static final int DEFAULT_TIMEOUT = 30000;
    static {
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

    static String ftpUsername;
    static String ftpPassword;
    static String ftpHost;
    static String ftpPort;
    static String ftDirectory;
    static String localPath;

    @PostConstruct
    public void initStaticParam() {
        Map<String, String> param = sysParamService.findValueMapsByType("tc_wx_strategy");
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
    }


    /**
     * 策略中心_任务工单信息 文件下发
     */
    @XxlJob(value = "sendWorkOrderJob")
    public void sendWorkOrder() {
        String yyyyMMdd;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        yyyyMMdd = new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN).format(calendar.getTime());
        String jobparam = XxlJobHelper.getJobParam();
        if (ObjectUtil.isNotEmpty(jobparam)) {
            yyyyMMdd = jobparam;
        }
        sendsendWorkOrder(yyyyMMdd);
    }

    @XxlJob(value="sendStrategyInfoJob")
    public void sendStrategy() {
        String yyyyMMdd;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        yyyyMMdd = new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN).format(calendar.getTime());
        String jobparam= XxlJobHelper.getJobParam();
        if(ObjectUtil.isNotEmpty(jobparam)){
            yyyyMMdd=jobparam;
        }
        sendStrategyInfo(yyyyMMdd);
    }

    public void sendsendWorkOrder(String yyyyMMdd) {
        log.info("下发策略中心_任务工单信息文件 开始  >>>>>>>>>>>> : ");
        //本地下载目录
        SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        try {
            String saveFile = ftpSendWorkOrderFileName + yyyyMMdd;
            String localFile = localPath + File.separator + saveFile;
            File file = new File(localFile);
            if (file.exists()) {
                file.delete();
            }
            InputStream input = null;
            try {
                if (0 != this.createWorkOrderFile(localFile, yyyyMMdd)) {
                    log.error("没有生成文件，下发策略中心_任务工单信息文件失败");
                    return;
                }
                input = new FileInputStream(file);
                // 上传文件前登录，避免超过sftp的等待空闲时间，导致超时断开连接
                ftpUtil.login();
                ftpUtil.upload("/", ftDirectory, saveFile, input);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("下发策略中心_任务工单信息文件异常: " + e.getMessage());
            } finally {
                if (null != input) {
                    input.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ftpUtil.logout();// 释放连接
        log.info("下发策略中心_任务工单信息文件 结束  >>>>>>>>>>>> : ");
    }

    public void sendStrategyInfo(String yyyyMMdd){
        log.info("下发策略中心_策略主信息文件 开始  >>>>>>>>>>>> : ");
        SftpWebUtil ftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
        try {
            String saveFile = FTP_SEND_STRATEGY_INFO_FILE_NAME + yyyyMMdd;
            String localFile = localPath + File.separator + saveFile;
            log.info("ftDirectory----" + ftDirectory);
            log.info("localFile----" + localFile);
            log.info("saveFile----" + saveFile);
            File file =new File(localFile);
            if(file.exists()) {
                file.delete();
            }
            InputStream input = null;
            try {
                if(0 != this.createStrategyInfoFile(localFile, yyyyMMdd)){
                    log.error("没有生成文件，下发策略中心_策略主信息文件失败");
                    return;
                }
                input = new FileInputStream(file);
                // 上传文件前登录，避免超过sftp的等待空闲时间，导致超时断开连接
                ftpUtil.login();
                ftpUtil.upload("/", ftDirectory, saveFile, input);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("下发策略中心_策略主信息文件异常: " + e.getMessage());
            }finally {
                if(null!=input) {
                    input.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 释放连接
        ftpUtil.logout();
        log.info("下发策略中心_策略主信息文件 结束  >>>>>>>>>>>> : ");

    }


    public int createWorkOrderFile(String path, String yyyyMMdd) {
        int flag = 0;
        FileWriter writer = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Map<String, JSONObject> map = wxStrategyService.queryStrategyInfo();
            writer = new FileWriter(file);
            bw = new BufferedWriter(writer);
            for (String key : map.keySet()) {
                JSONObject strategy = map.get(key);
                if (strategy == null) {
                    continue;
                }
                String topPolicyCode = String.valueOf(strategy.get("top_policy_code"));
                String businessScene = String.valueOf(strategy.get("business_scene"));
                List<WorkOrderDTO>  list = wxStrategyService.queryWorkOrder(key, LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString());
                if (ObjectUtil.isNotEmpty(list)) {
                    log.info("下发策略中心_任务工单信息,任务类型" + key + ",本次数据共计:" + list.size());
                    for (WorkOrderDTO workOrder : list) {
                        String customerProperty = customerProvinceService.getCustomerProperty(workOrder.getCustomId());
                        StringBuilder sb = new StringBuilder();
                        // 任务编码 ## 任务状态 ##活动名称
                        sb.append(ifNULL(workOrder.getWorkOrderCode())).append(split_str)
                            .append(ifNULL(workOrder.getWorkOrderStatus())).append(split_str)
                            .append(ifNULL(workOrder.getWorkOrderName())).append(split_str);

                        // 顶级策略编码 ##任务创建时间 ##任务截止时间
                        sb.append(ifNULL(topPolicyCode)).append(split_str)
                            .append(ifNULL(workOrder.getWorkOrderTime())).append(split_str)
                            .append(DEADLINE).append(split_str);

                        // 任务备注 ##省分编码 ##执行人地市编码
                        sb.append("").append(split_str)
                            .append(PROV_CODE).append(split_str)
                            .append(COUNTRY_CODE_MAP.get(workOrder.getAreaId())).append(split_str);

                        // 执行人区县编码 ##执行人网格编码
                        sb.append(ifNULL(workOrder.getOrgName())).append(split_str)
                            .append("").append(split_str);

                        // ##客户所属地域省份id ##客户所属地域省份名称 ##客户所属地域地市id ## 客户所属地域地市名称
                        sb.append("").append(split_str).append("").append(split_str).append("").append(split_str).append("").append(split_str);

                        // 执行人归属组织名称 ##归属组织编码
                        sb.append("").append(split_str).append("").append(split_str);

                        //执行人编码（OA工号） ##执行人姓名 ##执行人手机号
                        sb.append(ifNULL(workOrder.getManagerNo())).append(split_str)
                            .append(ifNULL(workOrder.getManagerName())).append(split_str)
                            .append(ifNULL(workOrder.getManagerMobile())).append(split_str);

                        // ## 客户经理编码（OA工号） ##客户经理姓名 ## 客户经理手机号
                        sb.append(ifNULL(workOrder.getActualManagerNo())).append(split_str)
                            .append(ifNULL(workOrder.getActualManagerName())).append(split_str)
                            .append(ifNULL(workOrder.getActualManagerMobile())).append(split_str);

                        // ##楼宇id ##楼宇名称
                        sb.append(ifNULL(workOrder.getBuildId())).append(split_str)
                            .append(ifNULL(workOrder.getBuildName())).append(split_str);

                        // ##客户id ##客户名称 ##客户属性
                        sb.append(ifNULL(workOrder.getCustomId())).append(split_str)
                            .append(ifNULL(workOrder.getCustomName())).append(split_str)
                            .append(customerProperty).append(split_str);

                        // ##创建时间 ## 修改时间
                        sb.append(ifNULL(workOrder.getGmtCreate())).append(split_str)
                            .append("").append(split_str);

                        // 创建人ID ##创建人姓名
                        sb.append(CREATE_USER_ID).append(split_str).append(CREATE_USER_NAME).append(split_str);

                        // 修改人ID ##修改人姓名
                        sb.append(UPDATE_USER_ID).append(split_str).append(UPDATE_USER_NAME).append(split_str);

                        // ##反馈时间 ##任务是否执行成功
                        sb.append(ifNULL(workOrder.getBackTime())).append(split_str).append("1").append(split_str);

                        // 业务大类 ##业务到期时间
                        sb.append("12".equals(businessScene) ? "" : businessScene).append(split_str)
                            .append(ifNULL(workOrder.getCategory())).append(split_str)
                            .append(ifNULL(workOrder.getBusinessEndDate())).append(split_str);

                        // 合同二级分类 ##到期合同名称 ##实体客户ID
                        sb.append("10000").append(split_str)
                            .append("htdq_warn".equals(key) ? ifNULL(workOrder.getContractName()) : "").append(split_str)
                            .append("htdq_warn".equals(key) ? ifNULL(workOrder.getCustNo()) : "").append(split_str);

                        // 实体客户名称 ##业务号码 ## 合同编码
                        sb.append("htdq_warn".equals(key) ? ifNULL(workOrder.getCustomName()) : "").append(split_str)
                            .append("terminal_contract_warn".equals(key) ? ifNULL(workOrder.getSvcNum()) : "").append(split_str)
                            .append("htdq_warn".equals(key) ? ifNULL(workOrder.getContractCode()) : "");

                        bw.write(sb.toString());
                        bw.write("\r\n");
                    }
                }

            }
        } catch (Exception e) {
            flag = 1;
            e.printStackTrace();
        } finally {
            try {
                if (null != bw) {
                    bw.flush();
                    bw.close();
                }
                if (null != writer) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }
    }


    private Object ifNULL(Object obj) {
        if (null == obj) {
            return "";
        }
        return obj;
    }



    /****
     * 生成策略任务主信息文件
     * @param path
     * @param yyyyMMdd
     * @return
     */
    public int createStrategyInfoFile(String path, String yyyyMMdd){
        int flag=0;
        FileWriter writer=null;
        BufferedWriter bw=null;
        try{
            File file =new File(path);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Map<String, JSONObject> map = wxStrategyService.queryStrategyInfo();
            writer = new FileWriter(file);
            bw = new BufferedWriter(writer);
            for (String key : map.keySet()) {
                JSONObject strategy = map.get(key);
                if (strategy == null) {
                    continue;
                }
                // 预警下发对应客户数
                String custCount = wxStrategyService.queryCustCount(key, LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString());
                // 预警工单总数
                String orderCount = wxStrategyService.queryOrderCount(key, LocalDate.parse(yyyyMMdd, DateTimeFormatter.BASIC_ISO_DATE).toString());
                String policyCode = String.valueOf(strategy.get("policy_code"));
                String topPolicyCode = String.valueOf(strategy.get("top_policy_code"));
                //业务场景编码
                String businessScene = String.valueOf(strategy.get("business_scene"));
                String description = String.valueOf(strategy.get("description"));
                //策略名称
                String name = String.valueOf(strategy.get("name"));
                // 客户群编号
                String customGroupCode= "C" + System.nanoTime();
                //                       策略编码             ##     顶级策略编码                   ##省份
                String tmpStr = ifNULL(policyCode) + split_str + ifNULL(topPolicyCode) + split_str + ifNULL(PROVINCE) + split_str +
                    //              客户群编号                      ##业务场景(见附录码表）           ##开始时间                 ##结束时间
                    ifNULL(customGroupCode) + split_str  +ifNULL(businessScene) + split_str + ifNULL(START_DATE) + split_str + ifNULL(END_DATE) + split_str +
                    //              策略描述                 ##下发方式默认传3         ##策略状态(见附录码表）      ##下发对应客户数
                    ifNULL(description) + split_str + ifNULL(SEND_TYPE) + split_str +  ifNULL(STATUS) + split_str + ifNULL(custCount) + split_str +
                    //            策略名称         ##备注                     ##租户id（传省份编码province）
                    ifNULL(name)  + split_str +ifNULL(null) + split_str + ifNULL(TENANT_ID) + split_str +
                    //         创建时间                 ##创建人OA工号                            ##创建人姓名
                    ifNULL(GMT_CREATE) + split_str + ifNULL(CREATE_USER_ID)  + split_str +ifNULL(CREATE_USER_NAME) + split_str+
                    //        策略类型(见附录码表）默认传1      ##可生成任务数           ##客户属性: 1-政企要客 户              ##客户群名称
                    ifNULL(POLICY_TYPE) + split_str + ifNULL(orderCount) + split_str+ifNULL(CUSTOMER_PROPERTY) + split_str+ ifNULL(CUSTOM_GROUP_NAME);

                bw.write(tmpStr);
                bw.write("\r\n");
            }
        }catch (Exception e)
        {
            flag=1;
            e.printStackTrace();
        }finally {
            try{
                if(null!=bw){
                    bw.flush();
                    bw.close();
                }
                if(null!=writer) {
                    writer.close();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return flag;
        }
    }
}
