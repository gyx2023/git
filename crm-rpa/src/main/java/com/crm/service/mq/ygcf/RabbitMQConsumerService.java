package com.crm.service.mq.ygcf;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.mq.config.RabbitMQConfig;
import com.crm.common.utils.StringUtils;
import com.crm.dto.BusinessMessageDTO;
import com.crm.dto.BusinessResponseDTO;
import com.crm.service.servicehall.NengKaiService;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.domain.rpa.ZqztRpaFormMappingDo;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.rpa.ZqztRpaFormMappingMapper;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.crm.utils.DealMapUtil.getValueByPath;
import static com.crm.utils.DealMapUtil.setValueByPath;
import static com.crm.utils.EvaluateUtil.evaluateCondition;

/**
 * RabbitMQ 消费者服务
 *
 * @author crm
 */
@Slf4j
@Service
public class RabbitMQConsumerService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = Integer.MAX_VALUE;

    @Resource
    private ZqztRpaFormMappingMapper zqztRpaFormMappingMapper;

    @Resource
    private NengKaiService nengKaiService;

    @Resource
    private SysParamMapper sysParamMapper;

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    /**
     * 消费测试队列的消息（使用手动确认）
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.YgcfConfig.RPA_YGCF_QUEUE,
                   ackMode = "MANUAL")
    public void receiveTestMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 获取消息ID
            String messageId = getMessageId(message);

            // 转换消息体
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("消费者接收到测试队列消息：{}", messageBody);
            log.info("消息ID：{}", messageId);
            log.info("消息路由键：{}", message.getMessageProperties().getReceivedRoutingKey());
            // 模拟业务逻辑处理
            ZqztJtNoticeRecordDo jtNoticeRecordDo = processMessage(messageBody);
            String failReason = this.extractFailReason(jtNoticeRecordDo.getResBody());
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setResBody(jtNoticeRecordDo.getResBody());
            noticeRecordDo.setFailReason(StringUtils.isNotEmpty(failReason)?failReason:null);
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);
            // 处理成功后手动确认消息
            channel.basicAck(deliveryTag, false);
            log.info("消息处理成功，手动确认");
        } catch (Exception e) {
            log.error("处理消息时发生错误：{}", e.getMessage(), e);
            // 获取重试次数
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("x-retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount < MAX_RETRY_COUNT) {
                // 增加重试次数并重新发送到重试队列
                retryMessage(message, channel, deliveryTag, retryCount + 1);
            } else {
                // 达到最大重试次数，直接进入死信队列
                channel.basicReject(deliveryTag, false);
                log.warn("消息处理失败，已达到最大重试次数，移至死信队列");
            }
        }
    }

    /**
     * 消费RPA业务消息
     *
     * @param businessMessage 业务消息
     */
    public BusinessResponseDTO receiveRpaMessage(BusinessMessageDTO businessMessage) throws IOException {

        BusinessResponseDTO response = new BusinessResponseDTO();

        try {
            // 获取信息
            String messageBody = businessMessage.getMessageBody();
            log.info("接收到消息ID：{}", businessMessage.getMessageId());
            log.info("接收到流水线编号：{}", businessMessage.getLineCode());
            log.info("接收到业务类型：{}", businessMessage.getBizType());
            log.info("接收到业务数据：{}", messageBody);

            ZqztJtNoticeRecordDo jtNoticeRecordDo = processMessage(messageBody);
            String failReason = this.extractFailReason(jtNoticeRecordDo.getResBody());
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setResBody(jtNoticeRecordDo.getResBody());
            noticeRecordDo.setFailReason(StringUtils.isNotEmpty(failReason) ? failReason : null);
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);

            log.info("更新成功");
            response.setStatus("success");
            response.setMessage("处理成功");

        } catch (Exception e) {
            log.error("处理消息时发生错误：{}", e.getMessage());
            response.setStatus("failed");
            response.setMessage(e.getMessage());
        }
        return response;
    }



    public String extractFailReason(String responseBody) {
        try {
            // 解析完整的响应JSON
            Map<String, Object> responseMap = JSONUtil.toBean(responseBody, Map.class);

            // 获取 UNI_BSS_BODY
            Map<String, Object> uniBssBody = (Map<String, Object>) responseMap.get("UNI_BSS_BODY");

            // 获取 WORKSHEETS_COMPLETE_RSP
            Map<String, Object> worksheetsCompleteRsp = (Map<String, Object>) uniBssBody.get("WORKSHEETS_COMPLETE_RSP");

            // 获取 data
            Map<String, Object> data = (Map<String, Object>) worksheetsCompleteRsp.get("data");

            // 获取 fworksheetIds 列表
            List<Map<String, Object>> fworksheetIds = (List<Map<String, Object>>) data.get("fworksheetIds");

            // 遍历 fworksheetIds 查找 failReason 不为空的项
            for (Map<String, Object> fworksheet : fworksheetIds) {
                String failReason = (String) fworksheet.get("failReason");
                if (failReason != null && !failReason.isEmpty()) {
                    return failReason; // 返回第一个非空的 failReason
                }
            }

            return null; // 如果没有找到非空的 failReason

        } catch (Exception e) {
            log.error("解析响应体失败", e);
            return null;
        }
    }

    /**
     * 重试消息
     *
     * @param message 原始消息
     * @param channel 通道
     * @param deliveryTag 交付标签
     * @param retryCount 重试次数
     * @throws IOException IO异常
     */
    private void retryMessage(Message message, Channel channel, long deliveryTag, int retryCount) throws IOException {
        long delay = 20000L;
        // 复制消息头并添加重试次数
        Map<String, Object> headers = new java.util.HashMap<>(message.getMessageProperties().getHeaders());
        headers.put("x-retry-count", retryCount);

        // 发送消息到重试队列，带延迟
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.YgcfConfig.RPA_YGCF_RETRY_EXCHANGE,
            RabbitMQConfig.YgcfConfig.RPA_YGCF_RETRY_ROUTING_KEY,
            new String(message.getBody(), StandardCharsets.UTF_8),
            msg -> {
                msg.getMessageProperties().getHeaders().putAll(headers);
                // 设置消息过期时间（TTL），实现延迟
                msg.getMessageProperties().setExpiration(String.valueOf(delay));
                return msg;
            }
        );

        // 确认原消息，从原队列中移除
        channel.basicAck(deliveryTag, false);
        log.info("消息已发送到重试队列，当前重试次数：{}", retryCount);
    }

    /**
     * 消费死信队列的消息（手动确认）
     *
     * @param message 消息内容
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.CommonConfig.RPA_DLQ)
    public void receiveDLQMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);

        log.error("死信队列接收到消息：{}", messageBody);
        log.error("死信原因：{}", message.getMessageProperties().getReceivedRoutingKey());
        log.error("Delivery Tag：{}", deliveryTag);

        try {
            // 可以在这里进行死信消息的处理，比如记录日志、发送告警等
            // 实际应用中可以考虑将死信消息存入数据库，以便后续人工处理
            log.error("死信消息已记录，等待人工处理");

            // 手动确认消息，从死信队列中移除
            channel.basicAck(deliveryTag, false);
            log.info("死信消息手动确认成功");
        } catch (Exception e) {
            log.error("处理死信消息时发生错误：{}", e.getMessage(), e);

            // 死信消息处理失败，拒绝并从队列中移除
            channel.basicReject(deliveryTag, false);
            log.error("死信消息处理失败，已从队列移除");
        }
    }

    /**
     * 模拟处理消息的业务逻辑
     *
     * @param messageBody 消息内容
     * @throws Exception 处理异常
     */
    public ZqztJtNoticeRecordDo processMessage(String messageBody) throws Exception {
        ZqztJtNoticeRecordDo recordDo = JSONUtil.toBean(messageBody, ZqztJtNoticeRecordDo.class);
        String bizCode = recordDo.getBizCode();
        SysParamDo rpaDealUser = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "RPA_DEAL_USER").eq(SysParamDo::getCode, bizCode));
        if(rpaDealUser !=null && rpaDealUser.getValue() != null){
            recordDo.setDealResultCode(rpaDealUser.getValue());
        }
        String body = recordDo.getBody();

        Boolean checkResult = this.checekData(recordDo);
        recordDo.setDealResultCode("0");
        // 先将JSON字符串转换为JSONObject
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(body);
        // 完全转换为Map结构，确保嵌套的JSON对象和数组也被正确转换
        Map<String, Object> messageMap = convertJsonToMap(jsonObject);

        List<ZqztRpaFormMappingDo> zqztRpaFormMappingDos = zqztRpaFormMappingMapper.selectList(new LambdaQueryWrapper<ZqztRpaFormMappingDo>().eq(ZqztRpaFormMappingDo::getBizCode, bizCode));
        //普通表单处理
        List<ZqztRpaFormMappingDo> formList = zqztRpaFormMappingDos.stream()
            .filter(item -> "0".equals(item.getIsComponent()))
            .collect(java.util.stream.Collectors.toList());

        for (ZqztRpaFormMappingDo item : formList) {
            //判断是否条件展示
            if("1".equals(item.getConditionsShow())){
                String conditionsValue = item.getConditionsValue();
                boolean res = evaluateCondition(conditionsValue, messageMap);
                if(res){
                    if(StringUtils.isEmpty(item.getSourceKey()) && StringUtils.isNotEmpty(item.getDefaultValue())){
                        if("Integer".equals(item.getValueType())){
                            setValueByPath(messageMap, item.getFormKey(), Integer.valueOf(item.getDefaultValue()));
                        }else {
                            setValueByPath(messageMap, item.getFormKey(),item.getDefaultValue());
                        }
                    }else {
                        Object valueByPath = getValueByPath(messageMap, item.getSourceKey());
                        if("Integer".equals(item.getValueType()) && valueByPath !=null){
                            setValueByPath(messageMap, item.getFormKey(), Integer.valueOf((String) valueByPath));
                        }else if("String".equals(item.getValueType()) && valueByPath !=null){
                            setValueByPath(messageMap, item.getFormKey(),valueByPath.toString());
                        }else {
                            setValueByPath(messageMap, item.getFormKey(),valueByPath);
                        }
                    }
//                    String[] split = item.getSourceKey().split("\\|");
//                    Arrays.asList(split).forEach(key -> {
//                        if(messageMap.get(key) !=null && messageMap.get(key) !=""){
//                            messageMap.put(item.getFormKey(), messageMap.get(key));
//                        }
//                    });
                }
            }else {
                if(StringUtils.isEmpty(item.getSourceKey()) && StringUtils.isNotEmpty(item.getDefaultValue())){
                    if("Integer".equals(item.getValueType())){
                        setValueByPath(messageMap, item.getFormKey(), Integer.valueOf(item.getDefaultValue()));
                    }else {
                        setValueByPath(messageMap, item.getFormKey(), item.getDefaultValue());
                    }

                }else {
                    Object valueByPath = getValueByPath(messageMap, item.getSourceKey());
                    if("Integer".equals(item.getValueType()) && valueByPath !=null){
                        setValueByPath(messageMap, item.getFormKey(), Integer.valueOf((String) valueByPath));
                    }else if("String".equals(item.getValueType()) && valueByPath !=null){
                        setValueByPath(messageMap, item.getFormKey(),valueByPath.toString());
                    }else {
                        setValueByPath(messageMap, item.getFormKey(),valueByPath);
                    }
                }
//                String[] split = item.getSourceKey().split("\\|");
//                Arrays.asList(split).forEach(key -> {
//                    if(messageMap.get(key) !=null && messageMap.get(key) !=""){
//                        messageMap.put(item.getFormKey(), messageMap.get(key));
//                    }
//                });
            }
        }

        //处理组件表单
        List<ZqztRpaFormMappingDo> componentList = zqztRpaFormMappingDos.stream()
            .filter(item -> "1".equals(item.getIsComponent()))
            .collect(java.util.stream.Collectors.toList());

        Map<String, List<ZqztRpaFormMappingDo>> ComponentMap = componentList.stream()
            .collect(java.util.stream.Collectors.groupingBy(ZqztRpaFormMappingDo::getComponentKey));
        for (Map.Entry<String, List<ZqztRpaFormMappingDo>> entry : ComponentMap.entrySet()) {
            List<ZqztRpaFormMappingDo> values = entry.getValue();
            //如果组件的集合只有一个，那么认为组件的值是固定的，直接将模版值放入到body中
            if(values.size()==1){
                Map<String, Object> componentMap = JSONUtil.toBean(values.get(0).getComponentTemplate(), Map.class);
                messageMap.put(entry.getKey(), componentMap);
            }else {
                List<ZqztRpaFormMappingDo> sortedValues = values.stream()
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))  // 按ID升序排列
                    .collect(java.util.stream.Collectors.toList());
                String componentTemplate = sortedValues.get(0).getComponentTemplate();
                // 将JSON字符串转换为Map
                // 先将JSON字符串转换为JSONObject
                cn.hutool.json.JSONObject componentObject = JSONUtil.parseObj(componentTemplate);
                // 完全转换为Map结构，确保嵌套的JSON对象和数组也被正确转换
                Map<String, Object> componentMap = convertJsonToMap(componentObject);
                for (ZqztRpaFormMappingDo item : sortedValues) {
                    //个性化处理电子表单组件
                    if(item.getComponentKey().equals("marketingContract1_Qu7n57Gt") && StringUtils.isNotEmpty(item.getComponentTemplate())){
                        Map<String,Object> goodsMsg = (Map<String,Object> )messageMap.get("GOODS_MSG");
                        Map<String,Object> contractInfo = (Map<String,Object> )goodsMsg.get("contractInfo");
                        componentMap.putAll(contractInfo);
                        continue;
                    }
                    //个性化处理合同期
                    if(item.getComponentKey().equals("marketingContract1_Qu7n57Gt") && item.getFormKey().equals("contract.endTime")){
                        String startTime = (String)getValueByPath(messageMap, "dateField_BBlT1ons");
                        Integer monthsToAdd = (Integer)getValueByPath(messageMap, item.getSourceKey());

                        // 定义日期格式（假设为常见的年月日格式）
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                        // 解析开始日期
                        assert startTime != null;
                        LocalDate startLocalDate = LocalDate.parse(startTime, formatter);

                        // 添加月数
                        LocalDate endLocalDate = startLocalDate.plusMonths(monthsToAdd);
                        setValueByPath(componentMap, item.getFormKey(), endLocalDate.format(formatter));
                        continue;
                    }
                    //个性化处理我方主体
                    if(item.getComponentKey().equals("marketingContract1_Qu7n57Gt") && item.getFormKey().equals("subjectName")){
                        String wfzt = (String) getValueByPath(messageMap, item.getSourceKey());
                        String[] parts = wfzt.split("-", 2);
                        String result="";
                        if (parts.length > 1) {
                            result = parts[1]; // 结果为"湖州"
                        }
                        if(!result.equals("浙江")){
                            result = "中国联合网络通信有限公司"+result+"市分公司";
                        }else {
                            result = "中国联合网络通信有限公司浙江省分公司";
                        }
                        setValueByPath(componentMap, item.getFormKey(),result);
                        continue;
                    }
                    //个性化处理盖章模式
                    if(item.getComponentKey().equals("marketingContract1_Qu7n57Gt") && item.getFormKey().equals("paperlessSign")){
                        Object valueByPath = getValueByPath(messageMap, item.getSourceKey());
                        if(valueByPath !=null){
                            SysParamDo paperlessSign = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "paperlessSign").eq(SysParamDo::getCode, (String) valueByPath));
                            setValueByPath(componentMap, item.getFormKey(), paperlessSign.getValue() !=null?Integer.valueOf(paperlessSign.getValue()):null);
                        }
                        continue;
                    }
                    if("1".equals(item.getConditionsShow())){
                        String conditionsValue = item.getConditionsValue();
                        boolean res = evaluateCondition(conditionsValue, messageMap);
                        if(res){
                            //个性化处理延展期限
                            if(item.getComponentKey().equals("marketingContract1_Qu7n57Gt") && item.getFormKey().equals("contract.timeLimit")){
                                String valueByPath = (String) getValueByPath(messageMap, item.getSourceKey());
                                SysParamDo timeLimit = sysParamMapper.selectOne(new LambdaQueryWrapper<SysParamDo>().eq(SysParamDo::getType, "timeLimit")
                                    .eq(SysParamDo::getCode, valueByPath));
                                setValueByPath(componentMap, item.getFormKey(), Integer.valueOf(timeLimit.getValue()));
                                continue;
                            }
                            if(StringUtils.isEmpty(item.getSourceKey()) && StringUtils.isNotEmpty(item.getDefaultValue())){
                                if("Integer".equals(item.getValueType())){
                                    setValueByPath(componentMap, item.getFormKey(), Integer.valueOf(item.getDefaultValue()));
                                }else if("String".equals(item.getValueType())){
                                    setValueByPath(componentMap, item.getFormKey(), item.getDefaultValue());
                                }
                            }else {
                                Object valueByPath = getValueByPath(messageMap, item.getSourceKey());
                                if("Integer".equals(item.getValueType()) && valueByPath !=null){
                                    setValueByPath(componentMap, item.getFormKey(), Integer.valueOf((String) valueByPath));
                                }else if("String".equals(item.getValueType()) && valueByPath !=null){
                                    setValueByPath(componentMap, item.getFormKey(), valueByPath.toString());
                                }
                            }
                        }
                        continue;
                    }
                    if(StringUtils.isNotEmpty(item.getSourceKey())){
                        Object valueByPath = getValueByPath(messageMap, item.getSourceKey());
                        if("Integer".equals(item.getValueType()) && valueByPath !=null){
                            setValueByPath(componentMap, item.getFormKey(), Integer.valueOf((String) valueByPath));
                        }else if("String".equals(item.getValueType()) && valueByPath !=null){
                            setValueByPath(componentMap, item.getFormKey(), valueByPath.toString());
                        }else {
                            setValueByPath(componentMap, item.getFormKey(), valueByPath);
                        }
                    }else {
                        if("Integer".equals(item.getValueType())){
                            setValueByPath(componentMap, item.getFormKey(), Integer.valueOf(item.getDefaultValue()));
                        } else {
                            setValueByPath(componentMap, item.getFormKey(), item.getDefaultValue());
                        }

                    }
                }
                messageMap.put(entry.getKey(),componentMap);
            }
        }

        //将电子表单组件的数据，复制到GOODS_MSG.contractInfo中，不知道这样对不对，问集团，集团让我自己去页面抓，那我就只能凭感觉写了
        Map<String,Object> goodsMsg = (Map<String,Object>)messageMap.get("GOODS_MSG");
        goodsMsg.put("contractInfo",messageMap.get("marketingContract1_Qu7n57Gt"));
        recordDo.setBody(JSONUtil.toJsonStr(messageMap));
        String response = nengKaiService.worksheetsComplete(recordDo, "RPA-YGCF");
        recordDo.setResBody(response);
        log.info("消息处理成功：{}", messageBody);
        return recordDo;
    }

    /**
     * 从消息中获取消息ID，如果没有则生成一个
     *
     * @param message 消息
     * @return 消息ID
     */
    private String getMessageId(Message message) {
        MessageProperties properties = message.getMessageProperties();
        String messageId = properties.getMessageId();
        if (StringUtils.isBlank(messageId)) {
            messageId = UUID.randomUUID().toString();
            properties.setMessageId(messageId);
        }

        return messageId;
    }

    private Boolean checekData(ZqztJtNoticeRecordDo recordDo){
        //合同金额校验

        //合同到期时间校验
        return true;
    }

    /**
     * 递归转换JSON对象为纯Map/List结构
     *
     * @param jsonObject 待转换的JSON对象
     * @return 转换后的Map对象
     */
    private Map<String, Object> convertJsonToMap(cn.hutool.json.JSONObject jsonObject) {
        Map<String, Object> resultMap = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            resultMap.put(key, convertJsonValue(value));
        }
        return resultMap;
    }

    /**
     * 递归转换JSON值为纯Java对象
     *
     * @param value 待转换的值
     * @return 转换后的Java对象
     */
    private Object convertJsonValue(Object value) {
        if (value instanceof cn.hutool.json.JSONObject) {
            return convertJsonToMap((cn.hutool.json.JSONObject) value);
        } else if (value instanceof cn.hutool.json.JSONArray) {
            cn.hutool.json.JSONArray jsonArray = (cn.hutool.json.JSONArray) value;
            List<Object> resultList = new java.util.ArrayList<>();
            for (Object item : jsonArray) {
                resultList.add(convertJsonValue(item));
            }
            return resultList;
        } else {
            // 对于基本类型直接返回
            return value;
        }
    }
}
