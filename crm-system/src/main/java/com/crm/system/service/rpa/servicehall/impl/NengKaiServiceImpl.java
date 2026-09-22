package com.crm.system.service.rpa.servicehall.impl;

import cn.hutool.json.JSONUtil;
import com.crm.system.domain.dao.Sequence;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;
import com.crm.system.mapper.SequenceMapper;
import com.crm.system.service.jt.IZqztJtNoticeRecordService;
import com.crm.system.service.rpa.servicehall.NengKaiService;
import com.crm.system.service.servicehall.INengKaiCallbackService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class NengKaiServiceImpl implements NengKaiService {

    @Resource
    private NengKaiNoticeAsyncService nengKaiNoticeAsyncService;

    @Resource
    private IZqztJtNoticeRecordService iZqztJtNoticeRecordService;

    @Resource
    private INengKaiCallbackService nengKaiCallbackCoreService;

    @Override
    public NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo) {
        ZqztJtNoticeRecordDo record = convert(requestVo);
        if (record == null || !Boolean.TRUE.equals(iZqztJtNoticeRecordService.saveNoticeRecord(record))
                || record.getId() == null) {
            return NengKaiResponseVo.error("通知记录保存失败");
        }
        nengKaiNoticeAsyncService.send(record);
        return NengKaiResponseVo.success();
    }

    @Override
    public String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo, String scene) {
        return nengKaiCallbackCoreService.callWorksheetsComplete(zqztJtNoticeRecordDo, scene);
    }

    @Override
    public String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo, String scene, String assignee) {
        return nengKaiCallbackCoreService.callWorksheetsComplete(zqztJtNoticeRecordDo, scene, assignee);
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
