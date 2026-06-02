package com.crm.adapter.admin.servicehall;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.json.JSONUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dto.rpa.BusinessMessageDTO;
import com.crm.system.domain.dto.rpa.BusinessResponseDTO;
import com.crm.system.domain.dto.rpa.Response;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.jt.IZqztJtNoticeRecordService;
import com.crm.system.service.rpa.RpaConsumerService;
import com.crm.system.service.rpa.servicehall.NengKaiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/capability/development")
@Slf4j
public class NotificationsController {

    @Resource
    private NengKaiService nengKaiService;

    @Resource
    private RpaConsumerService rpaConsumerService;

    @Resource
    private ZqztJtNoticeRecordMapper zqztJtNoticeRecordMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private IZqztJtNoticeRecordService iZqztJtNoticeRecordService;



    /**
     * 集团能力开发平台任务前置通知接口
     */
    @PostMapping(value = "notice")
    @ResponseBody
    @SaIgnore
    public NengKaiResponseVo notice(@RequestBody NengKaiRequestVo requestVo) {
        try {
            if (requestVo == null) {
                return NengKaiResponseVo.error("参数不能为空");
            }
            if (StringUtils.isBlank(requestVo.getOrderId())) {
                return NengKaiResponseVo.error("orderId不能为空");
            }
            return nengKaiService.nengKaiNotice(requestVo);
        } catch (Exception e) {
            log.error("流程和任务状态变更回调接口 异常", e);
            return NengKaiResponseVo.error("action异常：" + e.getMessage());
        }

    }

    /**
     * 无人工厂消息消费接口
     */
    @PostMapping(value = "consume")
    @ResponseBody
    @SaIgnore
    public BusinessResponseDTO consumeMessage(@RequestBody BusinessMessageDTO message) throws IOException {

        BusinessResponseDTO response = new BusinessResponseDTO();

        log.info("无人工厂消息消费接口 接收消息：{}", message);
        if (message == null) {
            response.setStatus("failed");
            response.setMessage("所有参数为空");
            return response;
        }

        if (StringUtils.isBlank(message.getMessageBody())) {
            response.setStatus("failed");
            response.setMessage("处理参数为空");
            return response;
        }

        return rpaConsumerService.receiveRpaMessage(message);
    }

    @GetMapping(value = "consume1")
    @ResponseBody
    @SaIgnore
    public void consumeMessage1() throws IOException {

        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = zqztJtNoticeRecordMapper.selectById(2910);
        BusinessMessageDTO businessMessageDTO = new BusinessMessageDTO();
        //获取唯一标识
        businessMessageDTO.setMessageId("202604231127BIZRPA002LINERPA0011014");
        businessMessageDTO.setBizType("BIZRPA001");
        businessMessageDTO.setLineCode("LINERPA001");
        businessMessageDTO.setMessageBody(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));
        rpaConsumerService.receiveRpaMessage(businessMessageDTO);
    }

    @GetMapping(value = "notice1")
    @ResponseBody
    @SaIgnore
    public void notice1() {
//        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = zqztJtNoticeRecordMapper.selectById(258);
//        System.out.println(zqztJtNoticeRecordDo.getOrderStatus());
//        zqztJtNoticeRecordDo.setId(null);
//        iZqztJtNoticeRecordService.saveNoticeRecord(zqztJtNoticeRecordDo);
        String UnmannedFactoryURL = "http://10.129.99.194:12001/prod-api/asiainfo_micro/unmannedfactory/kafkaMessageRecord/insert";
        ZqztJtNoticeRecordDo zqztJtNoticeRecordDo = zqztJtNoticeRecordMapper.selectById(923);
        BusinessMessageDTO businessMessageDTO = new BusinessMessageDTO();
        //获取唯一标识
        businessMessageDTO.setMessageId("202604281830BIZRPA002LINERPA0010027");
        businessMessageDTO.setBizType("BIZRPA001");
        businessMessageDTO.setLineCode("LINERPA001");
        businessMessageDTO.setMessageBody(JSONUtil.toJsonStr(zqztJtNoticeRecordDo));
        Response response = restTemplate.postForObject(UnmannedFactoryURL, businessMessageDTO, Response.class);
    }
}
