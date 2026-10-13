package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.crm.common.core.domain.R;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.ServletUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.FlameGuardianOrderResultBo;
import com.crm.system.domain.dao.sys.SysOperLog;
import com.crm.system.service.IZqztFlameGuardianResultService;
import com.crm.system.service.sys.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 火焰卫士受理订单执行结果通知（外部系统调用，appid + sign + timestamp 验签）
 */
@Slf4j
@RestController
@RequestMapping("/api/flameGuardian")
@RequiredArgsConstructor
public class FlameGuardianApi {

    /** 应用ID */
    private static final String APP_ID = "6b31336a9d4ef171";

    /** 公共密钥 */
    private static final String PUBLIC_KEY = "321de37a35c003d7ad27f33473ab74c3";

    /** 时间戳格式 */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 时间戳允许偏差（分钟） */
    private static final int TIMESTAMP_TOLERANCE_MINUTES = 5;

    /** orderId 长度上限 */
    private static final int ORDER_ID_MAX_LENGTH = 64;

    /** 操作日志：标题 / 调用方标识 / 单字段长度上限 */
    private static final String OPER_TITLE = "火焰卫士-受理结果通知";
    private static final String OPER_NAME = "火焰卫士";
    private static final int OPER_LOG_MAX_LENGTH = 2000;

    /** 操作日志里的业务类型与操作人类别，取 BusinessType / OperatorType 的「其它」 */
    private static final int BUSINESS_TYPE_OTHER = 0;
    private static final int OPERATOR_TYPE_OTHER = 0;

    private final IZqztFlameGuardianResultService resultService;
    private final ISysOperLogService sysOperLogService;

    /**
     * 接收受理订单执行结果，支持RPA执行结果回填和设备信息修改；
     */
    @SaIgnore
    @PostMapping("/orderResult")
    public R<Void> orderResult(@RequestBody FlameGuardianOrderResultBo bo) {
        SysOperLog operLog = buildOperLog(bo);
        long startTime = System.currentTimeMillis();

        if (bo != null && StringUtils.isNotBlank(bo.getOrderId())) {
            try {
                validateOperType(bo);
            } catch (ServiceException se) {
                R<Void> bad = R.fail(400, se.getMessage());
                operLog.setStatus(1);
                operLog.setErrorMsg(StringUtils.substring(se.getMessage(), 0, OPER_LOG_MAX_LENGTH));
                operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(bad), 0, OPER_LOG_MAX_LENGTH));
                operLog.setRequestTime(System.currentTimeMillis() - startTime);
                writeOperLog(operLog);
                return bad;
            }
        }

        R<Void> resp;
        try {
            if (bo == null || StringUtils.isBlank(bo.getOrderId())
                || bo.getOrderId().length() > ORDER_ID_MAX_LENGTH) {
                resp = R.fail(400, "参数校验失败：orderId不能为空或格式不正确");
            } else {
                verifySign(bo.getAppid(), bo.getSign(), bo.getTimestamp());
                resultService.handleOrderResult(bo);
                resp = R.ok();
            }
        } catch (Exception e) {
            log.error("【火焰卫士回调】处理异常，orderId={}", bo == null ? null : bo.getOrderId(), e);
            resp = R.fail(500, e.getMessage());
        }

        operLog.setStatus(resp.getCode() == R.SUCCESS ? 0 : 1);
        operLog.setErrorMsg(resp.getCode() == R.SUCCESS ? null
            : StringUtils.substring(resp.getMsg(), 0, OPER_LOG_MAX_LENGTH));
        operLog.setJsonResult(StringUtils.substring(JSON.toJSONString(resp), 0, OPER_LOG_MAX_LENGTH));
        operLog.setRequestTime(System.currentTimeMillis() - startTime);
        writeOperLog(operLog);
        return resp;
    }

    /**
     * 操作类型校验：必填且仅 T1/T2；
     * T1: RPA执行结果回填，必填 allSuccess/stepList 与 deviceName
     * T2: 设备信息修改，必填 deviceId
     * */
    private void validateOperType(FlameGuardianOrderResultBo bo) {
        String operType = bo.getOperType();
        if (StringUtils.isBlank(operType)) {
            throw new ServiceException("参数校验失败：operType不能为空");
        }
        boolean rpa;
        if ("T1".equals(operType)) {
            rpa = true;
        } else if ("T2".equals(operType)) {
            rpa = false;
        } else {
            throw new ServiceException("参数校验失败：operType仅支持T1/T2");
        }
        if (rpa) {
            if (bo.getAllSuccess() == null) {
                throw new ServiceException("参数校验失败：操作类型T1时allSuccess不能为空");
            }
            if (bo.getStepList() == null || bo.getStepList().isEmpty()) {
                throw new ServiceException("参数校验失败：操作类型T1时stepList不能为空");
            }
        }
        List<FlameGuardianOrderResultBo.Device> devices = bo.getDeviceList();
        if (devices == null) {
            return;
        }
        for (FlameGuardianOrderResultBo.Device device : devices) {
            if (rpa) {
                if (StringUtils.isBlank(device.getDeviceName())) {
                    throw new ServiceException("参数校验失败：操作类型T1时deviceList.deviceName不能为空");
                }
            } else if (StringUtils.isBlank(device.getDeviceId())) {
                throw new ServiceException("参数校验失败：操作类型T2时deviceList.deviceId不能为空");
            }
        }
    }

    /**
     * 组装操作日志
     */
    private SysOperLog buildOperLog(FlameGuardianOrderResultBo bo) {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle(OPER_TITLE);
        operLog.setBusinessType(BUSINESS_TYPE_OTHER);
        operLog.setOperatorType(OPERATOR_TYPE_OTHER);
        operLog.setOperName(OPER_NAME);
        operLog.setMethod("com.crm.adapter.api.FlameGuardianApi.orderResult()");
        operLog.setRequestMethod("POST");
        operLog.setOperUrl("/api/flameGuardian/orderResult");
        operLog.setOperIp(ServletUtils.getClientIP());
        operLog.setOperParam(StringUtils.substring(JSON.toJSONString(bo), 0, OPER_LOG_MAX_LENGTH));
        operLog.setOperTime(new Date());
        return operLog;
    }

    /** 日志写入失败不能影响回调结果，单独兜底 */
    private void writeOperLog(SysOperLog operLog) {
        try {
            sysOperLogService.insertOperlog(operLog);
        } catch (Exception e) {
            log.error("【火焰卫士回调】操作日志写入失败", e);
        }
    }

    /** 验签：sign = MD5(appid & 公共密钥 & timestamp)，取 32 位小写；timestamp 与当前时间相差不超过 5 分钟 */
    private void verifySign(String appid, String sign, String timestamp) {
        if (StringUtils.isBlank(appid) || StringUtils.isBlank(sign) || StringUtils.isBlank(timestamp)) {
            throw new ServiceException("参数为空，验签失败");
        }
        long minutes;
        try {
            LocalDateTime ts = LocalDateTime.parse(timestamp, TIMESTAMP_FORMAT);
            minutes = ChronoUnit.MINUTES.between(ts, LocalDateTime.now());
        } catch (Exception e) {
            throw new ServiceException("时间戳格式错误，验签失败");
        }
        if (Math.abs(minutes) > TIMESTAMP_TOLERANCE_MINUTES) {
            throw new ServiceException("timestamp与当前时间相差超过5分钟，验签失败");
        }
        if (!APP_ID.equals(appid)) {
            throw new ServiceException("appid不匹配，验签失败");
        }
        String expected = SecureUtil.md5(appid + "&" + PUBLIC_KEY + "&" + timestamp);
        if (!expected.equals(sign)) {
            throw new ServiceException("sign不匹配，验签失败");
        }
    }
}
