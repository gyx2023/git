package com.crm.system.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.FlameGuardianOrderResultBo;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.mapper.ZqztLedgerCloudConfigMapper;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.service.IZqztFlameGuardianResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 火焰卫士受理订单执行结果 服务实现
 * <p>
 * T1（RPA 执行结果回填）：主表 all_success / all_desc / all_time / step_list <b>整体覆盖</b>
 * 子表按设备名称（deviceName）匹配。
 * </p>
 * <p>
 * T2（设备信息修改）：主表四个结果列保持原值；子表按设备ID（deviceId）匹配。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianResultServiceImpl implements IZqztFlameGuardianResultService {

    private static final String VALID = "1";

    /** step_list 列长度上限，超出按 step 粒度截断存放 */
    private static final int STEP_LIST_MAX_LENGTH = 5000;

    /** 操作类型：RPA 执行结果回填 */
    private static final String OPER_TYPE_RPA = "T1";

    /** 操作类型：设备信息修改 */
    private static final String OPER_TYPE_DEVICE = "T2";

    /** 通道列分隔符 */
    private static final String CHANNEL_SEPARATOR = "|";

    /** 设备创建时间格式 */
    private static final String DEVICE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final ZqztLedgerCloudConfigMapper cloudConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderResult(FlameGuardianOrderResultBo bo) {
        ZqztLedgerInfo info = ledgerInfoMapper.selectOne(Wrappers.<ZqztLedgerInfo>lambdaQuery()
            .eq(ZqztLedgerInfo::getOrderNo, bo.getOrderId())
            .eq(ZqztLedgerInfo::getIsValid, VALID)
            .orderByAsc(ZqztLedgerInfo::getId)
            .last("limit 1"));
        if (info == null) {
            throw new ServiceException("未找到对应台账，orderId=" + bo.getOrderId());
        }
        boolean rpa = OPER_TYPE_RPA.equals(bo.getOperType());
        if (rpa) {
            // T1：RPA 执行结果回填 —— 主表结果字段整体覆盖
            updateLedgerResult(info.getId(), bo);
        } else {
            // T2（设备信息修改）：主表 4 个执行结果列保持原值，仅更新主表云智眼机构ID（不为空）
            if (StringUtils.isNotBlank(bo.getMerchantNum())) {
                ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                    .eq(ZqztLedgerInfo::getId, info.getId())
                    .set(ZqztLedgerInfo::getCloudOrgId, bo.getMerchantNum())
                    .set(ZqztLedgerInfo::getUpdateTime, new Date()));
            }
        }
        updateDeviceResults(info.getId(), bo, rpa);
    }

    // ==================== 主表 ====================

    /**
     * 主表受理结果整体覆盖（同一订单可能回调多次，以最后一次为准）
     */
    private void updateLedgerResult(Long ledgerId, FlameGuardianOrderResultBo bo) {
        ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
            .eq(ZqztLedgerInfo::getId, ledgerId)
            .set(ZqztLedgerInfo::getAllSuccess, bo.getAllSuccess() == null ? null : String.valueOf(bo.getAllSuccess()))
            .set(ZqztLedgerInfo::getAllDesc, bo.getAllDesc())
            .set(ZqztLedgerInfo::getAllTime, bo.getAllTime() == null ? null : bo.getAllTime().intValue())
            .set(ZqztLedgerInfo::getStepList, toStepListJson(bo.getStepList()))
            .set(StringUtils.isNotBlank(bo.getMerchantNum()), ZqztLedgerInfo::getCloudOrgId, bo.getMerchantNum())
            .set(ZqztLedgerInfo::getUpdateTime, new Date()));
    }

    /**
     * 步骤结果列表整体序列化存放。
     * <p>超出列长上限时按「step 粒度」丢弃尾部步骤，保证仍是合法 JSON</p>
     */
    private String toStepListJson(List<Map<String, Object>> stepList) {
        if (stepList == null || stepList.isEmpty()) {
            return null;
        }
        String json = JSON.toJSONString(stepList);
        if (json.length() <= STEP_LIST_MAX_LENGTH) {
            return json;
        }
        List<Map<String, Object>> kept = new java.util.ArrayList<>(stepList);
        while (!kept.isEmpty()) {
            kept.remove(kept.size() - 1);
            String candidate = JSON.toJSONString(kept);
            if (candidate.length() <= STEP_LIST_MAX_LENGTH) {
                log.warn("【火焰卫士回调】step_list 超长，按 step 粒度截断：原始 {} 步 → 保留 {} 步",
                        stepList.size(), kept.size());
                return candidate;
            }
        }
        // 单步都放不下的情形会先在循环里命中 kept 为空、序列化成 "[]"（长度 2）返回；
        // 仅当上限降到 2 以下才会走到这里，此时整列置空（NULL）
        log.warn("【火焰卫士回调】step_list 超长且单步也放不下，整列置空（原始 {} 步）", stepList.size());
        return null;
    }

    // ==================== 子表 ====================

    /**
     * 按操作类型分发设备结果
     *
     * @param rpa true=T1（按 deviceName 匹配）；false=T2（按 deviceId 匹配）
     */
    private void updateDeviceResults(Long ledgerId, FlameGuardianOrderResultBo bo, boolean rpa) {
        List<FlameGuardianOrderResultBo.Device> devices = bo.getDeviceList();
        if (devices == null || devices.isEmpty()) {
            return;
        }
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID));
        if (configs == null || configs.isEmpty()) {
            throw new ServiceException("台账没有有效的云智眼配置，无法回填设备结果，ledgerId=" + ledgerId);
        }
        Map<String, List<ZqztLedgerCloudConfig>> index = new HashMap<>();
        for (ZqztLedgerCloudConfig config : configs) {
            String key = rpa ? config.getDeviceName() : config.getDeviceId();
            index.computeIfAbsent(key, k -> new ArrayList<>()).add(config);
        }
        List<String> unmatched = new ArrayList<>();
        for (FlameGuardianOrderResultBo.Device device : devices) {
            String key = rpa ? device.getDeviceName() : device.getDeviceId();
            if (StringUtils.isBlank(key)) {
                unmatched.add(rpa ? "(设备名称为空)" : "(设备ID为空)");
            } else if (!index.containsKey(key)) {
                unmatched.add(key);
            }
        }
        if (!unmatched.isEmpty()) {
            throw new ServiceException("未匹配到台账设备：" + String.join("、", unmatched));
        }
        for (FlameGuardianOrderResultBo.Device device : devices) {
            String key = rpa ? device.getDeviceName() : device.getDeviceId();
            for (ZqztLedgerCloudConfig config : index.get(key)) {
                updateDeviceResult(config.getId(), device);
            }
        }
    }

    /**
     * 单条设备结果写入：设备字段一律「有值才写，无值不动」，T1 / T2 口径一致。
     * <p>未传的字段视为本次不更新，保留库中原值
     * 唯一整体覆盖的是主表执行结果四列，见 {@link #updateLedgerResult}。</p>
     */
    private void updateDeviceResult(Long configId, FlameGuardianOrderResultBo.Device device) {
        Date deviceCreateTime = parseDeviceCreateTime(device.getDeviceCreateTime());

        LambdaUpdateWrapper<ZqztLedgerCloudConfig> update = Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
            .eq(ZqztLedgerCloudConfig::getId, configId)
            .set(StringUtils.isNotBlank(device.getDesc()), ZqztLedgerCloudConfig::getDeviceDesc, device.getDesc())
            .set(deviceCreateTime != null, ZqztLedgerCloudConfig::getDeviceCreateTime, deviceCreateTime)
            .set(StringUtils.isNotBlank(device.getDeviceGbCode()), ZqztLedgerCloudConfig::getDeviceGbCode, device.getDeviceGbCode())
            .set(StringUtils.isNotBlank(device.getComboId()), ZqztLedgerCloudConfig::getComboId, device.getComboId())
            .set(StringUtils.isNotBlank(device.getComboName()), ZqztLedgerCloudConfig::getComboName, device.getComboName())
            .set(StringUtils.isNotBlank(device.getServiceNode()), ZqztLedgerCloudConfig::getServiceNode, device.getServiceNode())
            .set(StringUtils.isNotBlank(device.getBroadbandAccount()), ZqztLedgerCloudConfig::getBroadbandAccount, device.getBroadbandAccount())
            // deviceTypeText(null) 也是 null，判据必须落在原始值上
            .set(device.getDeviceType() != null, ZqztLedgerCloudConfig::getDeviceType, deviceTypeText(device.getDeviceType()))
            .set(device.getChannelCount() != null, ZqztLedgerCloudConfig::getCameraChannelCount, device.getChannelCount())
            .set(StringUtils.isNotBlank(device.getPassword()), ZqztLedgerCloudConfig::getDevicePassword, device.getPassword())
            .set(StringUtils.isNotBlank(device.getAccessProtocol()), ZqztLedgerCloudConfig::getAccessProtocol, device.getAccessProtocol())
            .set(StringUtils.isNotBlank(device.getSipIp()), ZqztLedgerCloudConfig::getSipServerIp, device.getSipIp())
            .set(StringUtils.isNotBlank(device.getSipPort()), ZqztLedgerCloudConfig::getSipServerPort, device.getSipPort())
            .set(StringUtils.isNotBlank(device.getSipId()), ZqztLedgerCloudConfig::getSipServerId, device.getSipId())
            .set(StringUtils.isNotBlank(device.getSipDomain()), ZqztLedgerCloudConfig::getSipServiceDomain, device.getSipDomain());

        // 通道三列：按 channelCodeList 下标对齐写入（列表非空才写；与通道数不一致时按最短条数写并 warn）
        List<FlameGuardianOrderResultBo.ChannelCode> channels = device.getChannelCodeList();
        if (channels != null && !channels.isEmpty()) {
            Integer channelCount = device.getChannelCount();
            // 通道数以报文声明的 channelCount 为准；未带通道数时按列表全量写入
            int limit = channelCount == null ? channels.size() : Math.min(channels.size(), channelCount);
            if (channelCount != null && channelCount != channels.size()) {
                log.warn("【火焰卫士回调】channelCodeList 长度 {} 与通道数 {} 不一致，按最短 {} 条写入",
                        channels.size(), channelCount, limit);
            }
            List<String> ids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<String> statuses = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                FlameGuardianOrderResultBo.ChannelCode channel = channels.get(i);
                ids.add(blankToEmpty(channel.getChannelCodeId()));
                names.add(blankToEmpty(channel.getChannelCodeName()));
                statuses.add(blankToEmpty(channel.getChannelCodeStatus()));
            }
            update.set(ZqztLedgerCloudConfig::getChannelCodeId, String.join(CHANNEL_SEPARATOR, ids))
                  .set(ZqztLedgerCloudConfig::getChannelName, String.join(CHANNEL_SEPARATOR, names))
                  .set(ZqztLedgerCloudConfig::getChannelCodeStatus, String.join(CHANNEL_SEPARATOR, statuses));
        }
        if (StringUtils.isBlank(update.getSqlSet())) {
            return;
        }
        update.set(ZqztLedgerCloudConfig::getUpdateTime, new Date());
        cloudConfigMapper.update(null, update);
    }

    /** 接口设备类型（1 IPC / 4 NVR）→ 台账文本口径 */
    private String deviceTypeText(Integer deviceType) {
        if (deviceType == null) {
            return null;
        }
        return deviceType == 4 ? "NVR设备" : "IPC设备";
    }

    private String blankToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /** 设备创建时间：为空或格式不对时返回 null，调用方按「无值不更新」跳过该列 */
    private Date parseDeviceCreateTime(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return DateUtil.parse(text.trim(), DEVICE_TIME_PATTERN);
        } catch (Exception e) {
            log.warn("【火焰卫士回调】设备创建时间无法解析：[{}]，按空值处理", text);
            return null;
        }
    }
}
