package com.crm.job.service.nlpt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.dao.sys.SysLog;
import com.crm.system.domain.dao.sys.SysUserOld;
import com.crm.system.domain.dao.topp.ToppGeneralRequirements;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.domain.topp.ToppOrderSyncGroup;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.mapper.flowable.FlowableHiProcinstMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.mapper.sys.SysUserOldMapper;
import com.crm.system.mapper.topp.ToppGeneralRequirementsMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.mapper.topp.ToppOrderVariablesMapper;
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
import org.springframework.stereotype.Component;

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
     * 省份地市和集团地市区域编码映射关系（每次执行时刷新）
     */
    private Map<String, String> areaMap;

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
    private ToppOrderVariablesMapper toppOrderVariablesMapper;

    @Resource
    private ToppGeneralRequirementsMapper toppGeneralRequirementsMapper;

    @Resource
    private SysUserOldMapper sysUserOldMapper;

    @Resource
    private FlowableHiProcinstMapper flowableHiProcinstMapper;

    @Resource
    private SysCodeMapper sysCodeMapper;

    @Resource
    private SysAreaMapper sysAreaMapper;

    /**
     * 需要发送集团服务大厅的订单类型配置缓存（每次执行时刷新）
     */
    private Map<String, JSONObject> needSendOrderTypeMap;

    /**
     * 每次执行前刷新缓存数据
     */
    private void refreshMaps() {
        // 刷新订单类型配置
        needSendOrderTypeMap = new HashMap<>();
        List<SysCode> codeList = sysCodeMapper.selectList(
            new LambdaQueryWrapper<SysCode>()
                .in(SysCode::getSettingStr3, Arrays.asList("business_middle_order", "middle_service", "business_examine_order"))
        );
        for (SysCode code : codeList) {
            if (StringUtils.isNotEmpty(code.getSettingStr2())) {
                needSendOrderTypeMap.put(code.getCode(), JSON.parseObject(code.getSettingStr2()));
            }
        }
        // 刷新区域编码映射
        areaMap = new HashMap<>();
        List<Map<String, Object>> areaJtCodeList = sysAreaMapper.selectAreaJtCodeList();
        for (Map<String, Object> row : areaJtCodeList) {
            areaMap.put((String) row.get("area_code"), (String) row.get("jt_code"));
        }
        log.info("【订单同步集团任务】缓存刷新完成，订单类型配置{}个，区域映射{}个",
            needSendOrderTypeMap.size(), areaMap.size());
    }

    /**
     * 省分订单数据同步服务大厅接口
     */
    @XxlJob(JOB_HANDLER_NAME)
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("【订单同步集团任务】开始执行，时间：{}", DateUtil.now());
        XxlJobHelper.log("【订单同步集团任务】开始执行");

        // 每次执行前刷新缓存
        refreshMaps();

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
    private JSONObject buildHead() {
        Map<String, String> params = sysParamService.findValueMapsByType("oppSyncGroupParam");
        return new JSONObject(UniBssApiClient.buildHead(
                params.getOrDefault("appId", ""),
                params.getOrDefault("appKey", "")));
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
        body.put("cityCode", areaMap.get(String.valueOf(order.getAreaId()).substring(0, 4)));
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
        body.put("bizName", null);
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
            SysUserOld user = sysUserOldMapper.selectOne(new LambdaQueryWrapper<SysUserOld>()
                .eq(SysUserOld::getId, creatorId));
            if (user != null) {
                return new String[]{String.valueOf(user.getNameCh()), String.valueOf(user.getOaAccount())};
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
        Date endTime = flowableHiProcinstMapper.selectEndTimeByProcInstId(procInstId);
        return DateUtil.format(endTime, "yyyyMMddHHmmss");
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
                    String ywxtddh = getOrderVariable(orderId, "crv_initiate-ywxtddh");
                    if (StringUtils.isNotEmpty(ywxtddh)) {
                        body.put("relOrderId", ywxtddh.replaceAll("\\|", ","));
                    }
                    break;
                }
                case "mainCircuit": {
                    String slxtdh = getOrderVariable(orderId, "mainCircuit_kthm-slxtdh");
                    if (StringUtils.isNotEmpty(slxtdh)) {
                        body.put("relOrderId", slxtdh.replaceAll("\\|", ","));
                    }
                    break;
                }
                case "businessAcceptance": {
                    // 与老项目一致：先一次性查询全部变量，再从 Map 中按优先级取值
                    Map<String, String> vars = getOrderVariablesMap(orderId);
                    String relOrderId = vars.get("ba_accept-bbssxtdh");
                    if (StringUtils.isEmpty(relOrderId)) {
                        relOrderId = vars.get("ba_accept-ywslxtdh");
                    }
                    if (StringUtils.isEmpty(relOrderId)) {
                        relOrderId = vars.get("ba_accept-cbssxtdh");
                    }
                    if (StringUtils.isNotEmpty(relOrderId)) {
                        body.put("relOrderId", relOrderId.replaceAll("\\|", ","));
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
     * 查询订单全部变量并处理类型
     */
    private Map<String, String> getOrderVariablesMap(Long orderId) {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> rows = toppOrderVariablesMapper.selectAllVarsByOrderId(orderId);
        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name_");
            String type = (String) row.get("type");
            Object val;
            if ("long".equals(type)) {
                val = row.get("long_");
            } else if ("double".equals(type)) {
                val = row.get("double_");
            } else {
                val = row.get("text_");
            }
            result.put(name, val != null ? String.valueOf(val) : null);
        }
        return result;
    }

    /**
     * 从 TOPP_ORDER_VARIABLES 表中查询流程变量值（与老项目类型处理逻辑一致）
     * 老项目逻辑：按 type_ 字段判断取 text_ / lon_ / doub_
     */
    private String getOrderVariable(Long orderId, String variableName) {
        Map<String, Object> row = toppOrderVariablesMapper.selectVarByOrderIdAndName(orderId, variableName);
        if (row == null) {
            return null;
        }
        String type = (String) row.get("type");
        if ("long".equals(type)) {
            Object val = row.get("long_");
            return val != null ? String.valueOf(val) : null;
        } else if ("double".equals(type)) {
            Object val = row.get("double_");
            return val != null ? String.valueOf(val) : null;
        } else {
            Object val = row.get("text_");
            return val != null ? String.valueOf(val) : null;
        }
    }

    /**
     * 发送接口请求
     */
    private void sendInterface(String url, JSONObject body) {
        SysLog sysLog = new SysLog();
        JSONObject sendJson = new JSONObject();
        sendJson.put("UNI_BSS_HEAD", buildHead());
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
        String operationType = toppGeneralRequirementsMapper.selectOne(
            new LambdaQueryWrapper<ToppGeneralRequirements>()
                .eq(ToppGeneralRequirements::getOrderId, orderId)).getOperationType();
        return needSendOrderTypeMap.get("general_order_support")
            .getJSONObject("type")
            .getJSONObject(operationType)
            .getString("type");
    }
}
