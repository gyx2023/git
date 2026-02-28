package com.crm.web.controller.servicehall;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.utils.StringUtils;
import com.crm.dto.BusinessMessageDTO;
import com.crm.dto.BusinessResponseDTO;
import com.crm.service.mq.ygcf.RabbitMQConsumerService;
import com.crm.service.servicehall.NengKaiService;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/capability/development")
@Slf4j
public class NotificationsController {

    @Resource
    private NengKaiService nengKaiService;

    @Resource
    private RabbitMQConsumerService rabbitMQConsumerService;



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
    public BusinessResponseDTO consumeMessage(@RequestBody BusinessMessageDTO message) throws IOException {

        rabbitMQConsumerService.receiveRpaMessage(message);

        return null;
    }
}
