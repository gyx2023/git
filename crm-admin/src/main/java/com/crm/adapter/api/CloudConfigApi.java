package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.crypto.SecureUtil;
import com.crm.common.core.domain.R;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.ZqztCloudConfigQueryBo;
import com.crm.system.domain.dto.ZqztCloudConfigRespDTO;
import com.crm.system.service.IZqztLedgerInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 云智眼设备配置信息查询（外部系统同步调用，appid + sign + timestamp 验签）
 */
@Slf4j
@RestController
@RequestMapping("/api/cloudConfig")
@RequiredArgsConstructor
public class CloudConfigApi {

    /** 应用ID（与老框架 SignatureConstant.APP_KEY_SJ_QUERY 一致） */
    private static final String APP_ID = "1b0ce31a10c26f08";

    /** 公钥（与老框架 SignatureConstant.PUBLIC_KEY 一致） */
    private static final String PUBLIC_KEY = "67b037d31b0ce31a10c26f081f5edff1";

    /** 时间戳格式 */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IZqztLedgerInfoService ledgerInfoService;

    /**
     * 云智眼设备配置信息查询
     */
    @SaIgnore
    @PostMapping("/queryCamera")
    public R<ZqztCloudConfigRespDTO> queryCamera(@RequestBody ZqztCloudConfigQueryBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getCameraAccount()) || bo.getCameraAccount().length() > 100) {
            return R.fail(400, "参数校验失败：cameraAccount不能为空或格式不正确");
        }
        verifySign(bo.getAppid(), bo.getSign(), bo.getTimestamp());
        return R.ok(ledgerInfoService.queryCameraConfig(bo.getCameraAccount()));
    }

    /** 验签：sign = MD5(appid & 公钥 & timestamp)（小写），timestamp 与当前时间相差不超过 5 分钟 */
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
        if (Math.abs(minutes) > 5) {
            throw new ServiceException("时间戳与当前时间相差超过5分钟，验签失败");
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
