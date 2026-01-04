package com.crm.system.service.servicehall.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.mq.ygcf.RabbitMQProducerService;
import com.crm.system.service.servicehall.NengKaiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class NengKaiServiceImpl implements NengKaiService {

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Resource
    private RabbitMQProducerService rabbitMQProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo) {
        //入集团通知记录表
        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = convert(requestVo);
        zqztJtNoticeRecordMapper.insert(zqztJtNoticeRecordDo);
        //发送mq消息
        rabbitMQProducerService.sendMessage(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));
        return NengKaiResponseVo.success();
    }



    /**
     * 将OrderProcessInfoRequestVo的属性复制到OrderProcessInfo
     * @param requestVo 请求参数对象
     * @return 实体对象
     */
    public static ZqztJtNoticeRecordDo convert(NengKaiRequestVo requestVo) {
        if (requestVo == null) {
            return null;
        }

        ZqztJtNoticeRecordDo processInfo = new ZqztJtNoticeRecordDo();

        // 逐一设置属性
        processInfo.setOrderId(requestVo.getOrderId());
        processInfo.setOrderOperatorId(requestVo.getOrderOperatorId());
        processInfo.setOrderOperatorName(requestVo.getOrderOperatorName());
        processInfo.setOrderProvinceCode(requestVo.getOrderProvinceCode());
        processInfo.setOrderCityCode(requestVo.getOrderCityCode());
        processInfo.setOrderStatus(requestVo.getOrderStatus());
        processInfo.setWorksheetId(requestVo.getWorksheetId());
        processInfo.setTaskCode(requestVo.getTaskCode());
        processInfo.setTaskName(requestVo.getTaskName());
        processInfo.setBizCode(requestVo.getBizCode());
        processInfo.setDealUserCode(requestVo.getDealUserCode());
        processInfo.setDealUserName(requestVo.getDealUserName());
        processInfo.setDealResultCode(requestVo.getDealResultCode());
        Map<String, Object> body = requestVo.getBody();
        if (body != null) {
            processInfo.setBody(JSONUtil.toJsonStr(body));
        }
        Map<String, Object> formTemplate = requestVo.getFormTemplate();
        if (formTemplate != null) {
            processInfo.setFormTemplate(JSONUtil.toJsonStr(formTemplate));
        }
        processInfo.setCreateTime(new Date());
        processInfo.setUpdateTime(new Date());

        return processInfo;
    }
}
