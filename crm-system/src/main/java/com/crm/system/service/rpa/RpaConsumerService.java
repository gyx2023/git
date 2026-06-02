package com.crm.system.service.rpa;

import cn.hutool.json.JSONUtil;
import com.crm.common.utils.StringUtils;

import com.crm.system.domain.dto.rpa.BusinessMessageDTO;
import com.crm.system.domain.dto.rpa.BusinessResponseDTO;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.rpa.strategy.MessageStrategyFactory;
import com.crm.system.service.rpa.strategy.RpaMessageProcessStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RpaConsumerService {

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    /**
     * 消费RPA业务消息
     *
     * @param businessMessage 业务消息
     */
    public BusinessResponseDTO receiveRpaMessage(BusinessMessageDTO businessMessage) throws IOException {

        BusinessResponseDTO response = new BusinessResponseDTO();


        // 获取信息
        String messageBody = businessMessage.getMessageBody();
        log.info("接收到消息ID：{}", businessMessage.getMessageId());
        log.info("接收到流水线编号：{}", businessMessage.getLineCode());
        log.info("接收到业务类型：{}", businessMessage.getBizType());
        log.info("接收到业务数据：{}", messageBody);
        try {
            ZqztJtNoticeRecordDo jtNoticeRecordDo = processMessage(messageBody);
            String failReason =extractFailReason(jtNoticeRecordDo.getResBody());
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setResBody(jtNoticeRecordDo.getResBody());
            noticeRecordDo.setFailReason(StringUtils.isNotEmpty(failReason) ? failReason : null);
            noticeRecordDo.setUpdateTime(new Date());
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);

            log.info("更新成功");
            response.setStatus("success");
            response.setMessage("处理成功");

        } catch (Exception e) {
            log.error("处理消息时发生错误：{}", e.getMessage(),e);
            ZqztJtNoticeRecordDo jtNoticeRecordDo = JSONUtil.toBean(messageBody, ZqztJtNoticeRecordDo.class);
            ZqztJtNoticeRecordDo noticeRecordDo = new ZqztJtNoticeRecordDo();
            noticeRecordDo.setId(jtNoticeRecordDo.getId());
            noticeRecordDo.setDealResultCode("1");
            noticeRecordDo.setUpdateTime(new Date());
            zqztJtNoticeRecordMapper.updateById(noticeRecordDo);
            response.setStatus("failed");
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @Resource
    private MessageStrategyFactory messageStrategyFactory;


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
     * 处理消息 - 委托给策略工厂路由到对应策略
     *
     * @param messageBody 消息内容
     * @return 处理结果
     */
    public ZqztJtNoticeRecordDo processMessage(String messageBody) throws Exception {
        ZqztJtNoticeRecordDo recordDo = JSONUtil.toBean(messageBody, ZqztJtNoticeRecordDo.class);
        String bizCode = recordDo.getBizCode();

        RpaMessageProcessStrategy strategy = messageStrategyFactory.resolve(bizCode);
        log.info("[RPA消息] bizCode={} 使用策略: {}", bizCode, strategy.getClass().getSimpleName());
        return strategy.process(recordDo);
    }
}
