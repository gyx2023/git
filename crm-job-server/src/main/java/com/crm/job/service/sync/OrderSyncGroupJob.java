package com.crm.job.service.sync;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.HaoKaoSignUtil;
import com.crm.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.dao.sys.SysLog;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.domain.topp.ToppOrderSyncGroup;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.sys.ISysLogService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.topp.IToppOrderSyncGroupService;
import com.crm.system.utils.SSLClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 省分订单数据同步服务大厅接口定时任务
 * 迁移自老项目 yypt_web OrderSyncGroupJob
 *
 * @author crm
 */
@Slf4j
@Component
public class OrderSyncGroupJob {

    private static final String JOB_HANDLER_NAME = "orderInfoSyncGroup";
    private static final String SEQ_NAME = "SYNC_GROOUP";

    /**
     * 省份地市和集团地市区域编码映射关系
     */
    private static final Map<String, String> AREA_MAP = new HashMap<>();

    static {
        AREA_MAP.put("3301", "360"); // 杭州
        AREA_MAP.put("3302", "370"); // 宁波
        AREA_MAP.put("3303", "470"); // 温州
        AREA_MAP.put("3304", "363"); // 嘉兴
        AREA_MAP.put("3305", "362"); // 湖州
        AREA_MAP.put("3306", "365"); // 绍兴
        AREA_MAP.put("3307", "367"); // 金华
        AREA_MAP.put("3308", "468"); // 衢州
        AREA_MAP.put("3309", "364"); // 舟山
        AREA_MAP.put("3310", "476"); // 台州
        AREA_MAP.put("3311", "469"); // 丽水
        AREA_MAP.put("3300", "360"); // 省本级对应到杭州
    }

    @Resource
    private IToppOrderSyncGroupService orderSyncGroupService;

    @Resource
    private ToppOrderMapper toppOrderMapper;

    @Resource
    private ISysLogService sysLogService;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private SequenceMapper sequenceMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SysCodeMapper sysCodeMapper;

    /**
     * 需要发送集团服务大厅的订单类型配置缓存
     * 与 OrderSyncGroupEventListener.needSendOrderTypeMap 加载逻辑一致
     */
    private final Map<String, JSONObject> needSendOrderTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        List<SysCode> codeList = sysCodeMapper.selectList(
            new LambdaQueryWrapper<SysCode>()
                .in(SysCode::getSettingStr3, Arrays.asList("business_middle_order", "middle_service", "business_examine_order"))
        );
        for (SysCode code : codeList) {
            if (StringUtils.isNotEmpty(code.getSettingStr2())) {
                JSONObject config = JSON.parseObject(code.getSettingStr2());
                needSendOrderTypeMap.put(code.getCode(), config);
                log.info("【订单同步集团任务】加载订单同步配置，orderType: {}", code.getCode());
            }
        }
        log.info("【订单同步集团任务】订单同步配置加载完成，共{}个订单类型", needSendOrderTypeMap.size());
    }

    /**
     * 省分订单数据同步服务大厅接口
     */
    @XxlJob(JOB_HANDLER_NAME)
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("【订单同步集团任务】开始执行，时间：{}", DateUtil.now());
        XxlJobHelper.log("【订单同步集团任务】开始执行");

        try {
            List<ToppOrderSyncGroup> dataList = orderSyncGroupService.getPendingList();
            if (CollUtil.isEmpty(dataList)) {
                log.info("【订单同步集团任务】无待同步数据");
                XxlJobHelper.handleSuccess("无待同步数据");
                return;
            }

            log.info("【订单同步集团任务】待同步数据共{}条", dataList.size());

            // 从系统参数中获取接口URL
            String syncUrl = sysParamService.findValueMapsByType("oppSyncGroupParam").get("orderInfoSyncGroupUrl");

            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < dataList.size(); i++) {
                ToppOrderSyncGroup data = dataList.get(i);
                try {
                    String procInstId = data.getProcInstId();
                    String sendType = data.getSendType();
                    log.info("【订单同步集团任务】第{}条，流程实例ID:{}，同步类型:{}", i + 1, procInstId, sendType);

                    // 构建请求体
                    JSONObject body = buildBody(procInstId, sendType);
                    // 发送接口
                    sendInterface(syncUrl, body);
                    // 删除已发送数据
                    orderSyncGroupService.deleteById(data.getId());

                    successCount++;
                    log.info("【订单同步集团任务】第{}条，工单处理完成", i + 1);
                } catch (Exception e) {
                    failCount++;
                    log.error("【订单同步集团任务】第{}条处理异常：{}", i + 1, e.getMessage(), e);
                }
            }

            long costTime = System.currentTimeMillis() - startTime;
            String msg = String.format("【订单同步集团任务】执行完成，总计%d条，成功%d条，失败%d条，耗时：%dms",
                    dataList.size(), successCount, failCount, costTime);
            log.info(msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("【订单同步集团任务】执行失败，耗时：%dms，异常：%s", costTime, e.getMessage());
            log.error(errorMsg, e);
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 构建请求头
     */
    private JSONObject buildHead() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Map<String, String> params = sysParamService.findValueMapsByType("oppSyncGroupParam");
        JSONObject head = new JSONObject();
        head.put("APP_ID", params.getOrDefault("appId", ""));
        head.put("key", params.getOrDefault("appKey", ""));
        HaoKaoSignUtil.buildAppParams(head);
        head.put("RESERVED", new ArrayList<>());
        return head;
    }

    /**
     * 构建请求体
     */
    private JSONObject buildBody(String procInstId, String sendType) {
        // 根据流程实例ID查询订单信息
        LambdaQueryWrapper<ToppOrder> wrapper = new LambdaQueryWrapper<ToppOrder>()
                .eq(ToppOrder::getPiId, procInstId);
        ToppOrder order = toppOrderMapper.selectOne(wrapper);

        JSONObject body = new JSONObject();
        // 服务大厅固定传UC，租户编码
        body.put("tenantId", "UC");
        // 服务大厅固定传SC，应用编码
        body.put("appId", "SC");
        // 归属省分编码，浙江固定36
        body.put("provinceCode", "36");
        // 地市编码，根据配置做转化
        body.put("cityCode", AREA_MAP.get(String.valueOf(order.getAreaId()).substring(0, 4)));
        // 创建人信息（从sys_user表查询）
        String[] creatorInfo = getCreatorInfo(order.getCreator());
        body.put("operatorName", creatorInfo[0]);
        body.put("operatorId", creatorInfo[1]);
        // 订单创建时间 YYYYMMDDHHMMSS
        LocalDateTime createLdt = order.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        body.put("createTime", createLdt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        // 订单ID
        body.put("orderId", order.getOrderNumber());
        // 订单状态 1-草稿 2-处理中 3-已撤单 4-已竣工
        if ("E".equals(sendType)) {
            if ("abandon".equals(order.getStatus())) {
                body.put("orderStatus", "3");
            } else if ("end".equals(order.getStatus())) {
                body.put("orderStatus", "4");
            } else {
                body.put("orderStatus", sendType);
            }
            // 订单结束时间：从Flowable历史流程实例中获取真正的结束时间
            body.put("finishTime", getProcessEndTime(procInstId));
        } else {
            body.put("orderStatus", sendType);
        }
        // 流程ID
        body.put("bizCode", order.getPiId());
        // 流程名称（与老项目行为一致：老项目使用orderTypeName，@Transient，该上下文下为null）
        body.put("bizName", "");
        // 流程状态 1-草稿 2-已启用 3-已停用 默认启用
        body.put("bizStatus", "2");
        // 归属业务类型编码
        body.put("bizType", getBizType(order));

        // 关联政企订单中心的订单号
        setRelOrderId(body, order);

        JSONObject retJson = new JSONObject();
        retJson.put("QRY_INFOS_REQ", body);
        return retJson;
    }

    /**
     * 查询创建人信息（姓名和OA账号）
     * 返回 [nameCh, oaAccount]
     */
    private String[] getCreatorInfo(Long creatorId) {
        if (creatorId == null) {
            return new String[]{"", ""};
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT name_ch, oa_account FROM sys_user WHERE id_ = ?", creatorId);
            if (CollUtil.isNotEmpty(rows)) {
                Map<String, Object> row = rows.get(0);
                String nameCh = String.valueOf(row.getOrDefault("name_ch", ""));
                String oaAccount = String.valueOf(row.getOrDefault("oa_account", ""));
                return new String[]{nameCh, oaAccount};
            }
        } catch (Exception e) {
            log.warn("【订单同步集团任务】查询sys_user失败，creatorId: {}, 异常: {}", creatorId, e.getMessage());
        }
        return new String[]{"", ""};
    }

    /**
     * 获取流程结束时间（从Flowable历史流程实例表）
     * 与老项目行为一致：historyService.createHistoricProcessInstanceQuery().finished().list().get(0)
     * 查不到时异常会传播至上层 catch 块处理
     */
    private String getProcessEndTime(String procInstId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT end_time_ FROM flo_hi_procinst WHERE proc_inst_id_ = ? AND end_time_ IS NOT NULL ORDER BY end_time_ DESC LIMIT 1",
            procInstId);
        Object endTime = rows.get(0).get("end_time_");
        if (endTime instanceof Date) {
            return DateUtil.format((Date) endTime, "yyyyMMddHHmmss");
        }
        LocalDateTime ldt = new java.sql.Timestamp(((Date) endTime).getTime()).toLocalDateTime();
        return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 设置关联政企订单中心的订单号
     */
    private void setRelOrderId(JSONObject body, ToppOrder order) {
        String orderType = order.getOrderType();
        if (StringUtils.isEmpty(orderType)) {
            return;
        }
        Long orderId = order.getId();
        try {
            switch (orderType) {
                case "crv": {
                    // 资源核查：取支撑系统订单号
                    String ywxtddh = getOrderVariable(orderId, "crv_initiate-ywxtddh");
                    if (StringUtils.isNotEmpty(ywxtddh)) {
                        body.put("relOrderId", ywxtddh.replaceAll("\\|", ","));
                    }
                    break;
                }
                case "mainCircuit": {
                    // 电路开通：取受理系统单号
                    String slxtdh = getOrderVariable(orderId, "mainCircuit_kthm-slxtdh");
                    if (StringUtils.isNotEmpty(slxtdh)) {
                        body.put("relOrderId", slxtdh.replaceAll("\\|", ","));
                    }
                    break;
                }
                case "businessAcceptance": {
                    // 业务受理：按优先级依次取 B-BSS系统单号 > BSS系统单号 > cBSS系统单号
                    String bbssxtdh = getOrderVariable(orderId, "ba_accept-bbssxtdh");
                    if (StringUtils.isNotEmpty(bbssxtdh)) {
                        body.put("relOrderId", bbssxtdh.replaceAll("\\|", ","));
                    } else {
                        String ywslxtdh = getOrderVariable(orderId, "ba_accept-ywslxtdh");
                        if (StringUtils.isNotEmpty(ywslxtdh)) {
                            body.put("relOrderId", ywslxtdh.replaceAll("\\|", ","));
                        } else {
                            String cbssxtdh = getOrderVariable(orderId, "ba_accept-cbssxtdh");
                            if (StringUtils.isNotEmpty(cbssxtdh)) {
                                body.put("relOrderId", cbssxtdh.replaceAll("\\|", ","));
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("【订单同步集团任务】查询relOrderId失败，orderId: {}, orderType: {}, 异常: {}",
                orderId, orderType, e.getMessage());
        }
    }

    /**
     * 从 TOPP_ORDER_VARIABLES 表中查询流程变量值（与老项目查询逻辑一致）
     * 老项目 SQL: SELECT T.TEXT_ FROM TOPP_ORDER_VARIABLES T
     *            WHERE T.ORDER_ID=? AND T.NAME_=? ORDER BY create_time DESC LIMIT 0,1
     */
    private String getOrderVariable(Long orderId, String variableName) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT text_ FROM topp_order_variables WHERE order_id = ? AND name_ = ?"
                + " ORDER BY create_time DESC LIMIT 1",
                orderId, variableName);
            if (CollUtil.isNotEmpty(rows)) {
                Object val = rows.get(0).get("text_");
                return val != null ? String.valueOf(val) : null;
            }
        } catch (Exception e) {
            log.debug("【订单同步集团任务】查询topp_order_variables失败，orderId: {}, name: {}, 异常: {}",
                orderId, variableName, e.getMessage());
        }
        return null;
    }

    /**
     * 发送接口请求
     */
    private void sendInterface(String url, JSONObject body) {
        SysLog sysLog = new SysLog();
        JSONObject sendJson = new JSONObject();
        try {
            sendJson.put("UNI_BSS_HEAD", buildHead());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            log.error("【订单同步集团任务】构建请求头异常", e);
        }
        sendJson.put("UNI_BSS_BODY", body);

        String jsonStr = sendJson.toString();
        sysLog.setType("省分订单数据同步服务大厅接口");
        sysLog.setContent(jsonStr);
        sysLog.setCreateTime(LocalDateTime.now());
        log.info("【订单同步集团任务】请求数据:{}", jsonStr);

        HttpClient httpClient = null;
        HttpPost httpPost = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Accept-Encoding", "utf-8");
            httpPost.addHeader("nlpt-wm-tenant-id", "UC");
            httpPost.addHeader("nlpt-wm-app-id", "SC");
            httpPost.addHeader("nlpt-wm-touchpoint", "037");
            String serialNo = "037" + getCode();
            httpPost.addHeader("nlpt-wm-serial", serialNo);
            httpPost.setEntity(new StringEntity(jsonStr, "utf-8"));
            log.info("【订单同步集团任务】流水号:{}", serialNo);
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    JSONObject retJson = JSON.parseObject(EntityUtils.toString(resEntity, "utf-8"));
                    log.info("【订单同步集团任务】返回消息:{}", retJson);
                    sysLog.setRemark("请求接口返回消息" + retJson);
                }
            }
        } catch (Exception ex) {
            log.error("【订单同步集团任务】请求接口出错:{}", ex.getMessage(), ex);
            sysLog.setRemark("请求接口出错" + ex.getMessage());
        } finally {
            sysLogService.saveLog(sysLog);
            if (httpClient instanceof java.io.Closeable) {
                try { ((java.io.Closeable) httpClient).close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 获取流水号（与老项目 getCode() 逻辑完全一致）
     * 格式：yyyyMMdd + 9位自增序号，跨天重置
     */
    private String getCode() {
        String searchDate = DateUtil.formatDate(new Date()).replaceAll("-", "");
        sequenceMapper.update(null, new LambdaUpdateWrapper<Sequence>()
                .set(Sequence::getIncrementVal, 1)
                .eq(Sequence::getSeqName, SEQ_NAME));
        Sequence seq = sequenceMapper.selectOne(new LambdaUpdateWrapper<Sequence>()
                .eq(Sequence::getSeqName, SEQ_NAME));
        int currentVal = seq.getCurrentVal();
        if (searchDate.equals(seq.getSearchDate())) {
            currentVal = currentVal + 1;
            sequenceMapper.update(null, new LambdaUpdateWrapper<Sequence>()
                    .set(Sequence::getCurrentVal, currentVal)
                    .eq(Sequence::getSeqName, SEQ_NAME));
        } else {
            currentVal = 1;
            sequenceMapper.update(null, new LambdaUpdateWrapper<Sequence>()
                    .set(Sequence::getCurrentVal, currentVal)
                    .set(Sequence::getSearchDate, searchDate)
                    .eq(Sequence::getSeqName, SEQ_NAME));
        }
        return searchDate + String.format("%09d", currentVal);
    }

    /**
     * 获取归属业务类型编码（与老项目逻辑完全一致，无兜底）
     */
    private String getBizType(ToppOrder order) {
        String orderType = order.getOrderType();
        if (StringUtils.isEmpty(orderType)) {
            return "";
        }
        if ("general_order_support".equals(orderType)) {
            return getGeneralOrderBizType(order.getId());
        }
        // 与老项目一致：NEED_SEND_ORDER_TYPE.get(orderType).getString("type")
        return needSendOrderTypeMap.get(orderType).getString("type");
    }

    /**
     * 获取通用需求订单的业务类型编码（与老项目逻辑完全一致）
     * 老项目逻辑：
     *   1. generalRequirementsService.getByOrderId(orderId) → query topp_general_requirements
     *   2. config.getJSONObject("type").getJSONObject(operationType).getString("type")
     */
    private String getGeneralOrderBizType(Long orderId) {
        String operationType = jdbcTemplate.queryForList(
            "SELECT operation_type FROM topp_general_requirements WHERE order_id = ?", orderId)
            .get(0).get("operation_type").toString();
        return needSendOrderTypeMap.get("general_order_support")
            .getJSONObject("type")
            .getJSONObject(operationType)
            .getString("type");
    }
}
