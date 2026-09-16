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
 * 只沉淀受理结果：主表回填 all_success / all_desc / all_time / step_list 与云智眼机构ID，
 * 子表按「订单号 + 摄像头名称」分设备回填 device_desc / device_create_time / device_gbCode
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianResultServiceImpl implements IZqztFlameGuardianResultService {

    private static final String VALID = "1";

    /** step_list 列长度上限，超出截断存放 */
    private static final int STEP_LIST_MAX_LENGTH = 5000;

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
        updateLedgerResult(info.getId(), bo);
        updateDeviceResults(info.getId(), bo);
    }

    // ==================== 主表 ====================

    /**
     * 主表受理结果整体覆盖（同一订单可能回调多次，以最后一次为准）
     * <p>云智眼机构ID 是人工录入字段，只在回调带了值时才覆盖。</p>
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

    /** 步骤结果列表整体序列化存放，超长截断（截断会破坏 json 结构，但总好过整条回调失败） */
    private String toStepListJson(List<Map<String, Object>> stepList) {
        if (stepList == null || stepList.isEmpty()) {
            return null;
        }
        String json = JSON.toJSONString(stepList);
        if (json.length() > STEP_LIST_MAX_LENGTH) {
            log.warn("【火焰卫士回调】step_list 长度 {} 超过列上限 {}，已截断存放", json.length(), STEP_LIST_MAX_LENGTH);
            return json.substring(0, STEP_LIST_MAX_LENGTH);
        }
        return json;
    }

    // ==================== 子表 ====================

    /**
     * 按摄像头名称把结果分发到对应设备
     */
    private void updateDeviceResults(Long ledgerId, FlameGuardianOrderResultBo bo) {
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
        Map<String, List<ZqztLedgerCloudConfig>> byName = new HashMap<>();
        for (ZqztLedgerCloudConfig config : configs) {
            byName.computeIfAbsent(config.getDeviceName(), key -> new ArrayList<>()).add(config);
        }
        List<String> unmatched = new ArrayList<>();
        for (FlameGuardianOrderResultBo.Device device : devices) {
            // 设备名称是唯一的匹配依据，为空同样归入对不上，不能静默跳过
            if (StringUtils.isBlank(device.getDeviceName())) {
                unmatched.add("(设备名称为空)");
            } else if (!byName.containsKey(device.getDeviceName())) {
                unmatched.add(device.getDeviceName());
            }
        }
        if (!unmatched.isEmpty()) {
            throw new ServiceException("未匹配到台账设备：" + String.join("、", unmatched));
        }
        for (FlameGuardianOrderResultBo.Device device : devices) {
            for (ZqztLedgerCloudConfig config : byName.get(device.getDeviceName())) {
                updateDeviceResult(config.getId(), device);
            }
        }
    }

    /** 单条设备结果整体覆盖 */
    private void updateDeviceResult(Long configId, FlameGuardianOrderResultBo.Device device) {
        LambdaUpdateWrapper<ZqztLedgerCloudConfig> update = Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
            .eq(ZqztLedgerCloudConfig::getId, configId)
            .set(ZqztLedgerCloudConfig::getDeviceDesc, device.getDesc())
            .set(ZqztLedgerCloudConfig::getDeviceCreateTime, parseDeviceCreateTime(device.getDeviceCreateTime()))
            .set(ZqztLedgerCloudConfig::getDeviceGbCode, device.getDeviceGbCode())
            .set(ZqztLedgerCloudConfig::getComboId, device.getComboId())
            .set(ZqztLedgerCloudConfig::getComboName, device.getComboName())
            .set(ZqztLedgerCloudConfig::getServiceNode, device.getServiceNode())
            .set(ZqztLedgerCloudConfig::getUpdateTime, new Date());
        cloudConfigMapper.update(null, update);
    }

    /** 设备创建时间：为空或格式不对时返回 null，由调用方按覆盖语义清空 */
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
