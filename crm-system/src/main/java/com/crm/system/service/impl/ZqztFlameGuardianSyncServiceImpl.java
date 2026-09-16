package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.StringUtils;
import com.crm.system.client.flameguardian.FlameGuardianApiClient;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.domain.dto.FlameGuardianOrderReqDTO;
import com.crm.system.domain.dto.FlameGuardianRespDTO;
import com.crm.system.domain.dto.FlameGuardianSyncResultDTO;
import com.crm.system.mapper.ZqztLedgerAreaMapper;
import com.crm.system.mapper.ZqztLedgerCloudConfigMapper;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.service.IZqztFlameGuardianSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 台账火焰卫士同步 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianSyncServiceImpl implements IZqztFlameGuardianSyncService {

    private static final String VALID = "1";

    /** 同步状态：待同步 / 同步中 / 同步成功 / 同步失败 */
    private static final String SYN_PENDING = "1";
    private static final String SYN_RUNNING = "2";
    private static final String SYN_SUCCESS = "3";
    private static final String SYN_FAIL = "4";

    /** 网关成功码 */
    private static final int API_SUCCESS_CODE = 200;

    /** 单轮最多处理条数，避免单次调度执行过长 */
    private static final int BATCH_SIZE = 200;

    /** 属地默认浙江省 */
    private static final String PROVINCE = "浙江省";

    /** 宽带服务商默认联通 */
    private static final String BROADBAND_SERVICE_TYPE = "unicom";

    /** 台账设备类型对应关系（1 IPC摄像头、4 NVR摄像头） */
    private static final String DEVICE_TYPE_NVR_TEXT = "NVR设备";
    private static final int API_DEVICE_TYPE_IPC = 1;
    private static final int API_DEVICE_TYPE_NVR = 4;

    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final ZqztLedgerCloudConfigMapper cloudConfigMapper;
    private final ZqztLedgerAreaMapper areaMapper;
    private final FlameGuardianApiClient flameGuardianApiClient;

    @Override
    public FlameGuardianSyncResultDTO syncPending() {
        FlameGuardianSyncResultDTO result = new FlameGuardianSyncResultDTO();
        result.setReclaimed(reclaimStuck());

        List<ZqztLedgerInfo> list = ledgerInfoMapper.selectList(Wrappers.<ZqztLedgerInfo>lambdaQuery()
            .eq(ZqztLedgerInfo::getIsValid, VALID)
            .in(ZqztLedgerInfo::getFlameGuardianSyn, SYN_PENDING, SYN_FAIL)
            .orderByAsc(ZqztLedgerInfo::getId)
            .last("limit " + BATCH_SIZE));
        if (list == null || list.isEmpty()) {
            log.info("【火焰卫士同步】无待同步台账，回收僵死记录 {} 条", result.getReclaimed());
            return result;
        }

        result.setTotal(list.size());
        Map<String, String> areaNames = loadAreaNames(list);
        for (ZqztLedgerInfo info : list) {
            // 条件更新抢锁：只有把 1/4 改成 2 成功的那一次才真正发起调用，避免重复推送
            if (!markRunning(info.getId())) {
                continue;
            }
            if (syncOne(info, areaNames)) {
                result.setSuccess(result.getSuccess() + 1);
            } else {
                result.setFailed(result.getFailed() + 1);
            }
        }
        log.info("【火焰卫士同步】执行完成：待处理 {}，成功 {}，失败 {}，回收僵死 {}",
            result.getTotal(), result.getSuccess(), result.getFailed(), result.getReclaimed());
        return result;
    }

    // ==================== 状态流转 ====================

    /**
     * 抢锁：把「待同步 / 同步失败」条件更新为「同步中」
     *
     * @return true=抢到（本次由我发起调用），false=状态已不是 1/4，跳过
     */
    private boolean markRunning(Long id) {
        return ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
            .eq(ZqztLedgerInfo::getId, id)
            .in(ZqztLedgerInfo::getFlameGuardianSyn, SYN_PENDING, SYN_FAIL)
            .set(ZqztLedgerInfo::getFlameGuardianSyn, SYN_RUNNING)
            .set(ZqztLedgerInfo::getUpdateTime, new Date())) > 0;
    }

    /** 落终态：同步成功 / 同步失败 */
    private void markFinished(Long id, boolean success) {
        ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
            .eq(ZqztLedgerInfo::getId, id)
            .set(ZqztLedgerInfo::getFlameGuardianSyn, success ? SYN_SUCCESS : SYN_FAIL)
            .set(ZqztLedgerInfo::getUpdateTime, new Date()));
    }

    /**
     * 回收上一轮遗留的「同步中」，重置为失败以便本轮重新推送
     */
    private int reclaimStuck() {
        return ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
            .eq(ZqztLedgerInfo::getFlameGuardianSyn, SYN_RUNNING)
            .set(ZqztLedgerInfo::getFlameGuardianSyn, SYN_FAIL)
            .set(ZqztLedgerInfo::getUpdateTime, new Date()));
    }

    // ==================== 单条同步 ====================

    private boolean syncOne(ZqztLedgerInfo info, Map<String, String> areaNames) {
        try {
            FlameGuardianOrderReqDTO req = buildRequest(info, areaNames);
            FlameGuardianRespDTO resp = flameGuardianApiClient.applyOrder(req);
            boolean success = resp.getCode() != null && resp.getCode() == API_SUCCESS_CODE;
            if (!success) {
                log.warn("【火焰卫士同步】台账 {} 同步失败：code={}, message={}",
                    info.getId(), resp.getCode(), resp.getMessage());
            }
            markFinished(info.getId(), success);
            return success;
        } catch (Exception e) {
            // 超时、非 2xx、报文解析失败等都归入同步失败，下一轮继续重试
            log.error("【火焰卫士同步】台账 {} 调用异常", info.getId(), e);
            markFinished(info.getId(), false);
            return false;
        }
    }

    // ==================== 报文组装 ====================

    private FlameGuardianOrderReqDTO buildRequest(ZqztLedgerInfo info, Map<String, String> areaNames) {
        FlameGuardianOrderReqDTO req = new FlameGuardianOrderReqDTO();
        req.setOrderId(info.getOrderNo());
        req.setProvince(PROVINCE);
        req.setCity(areaName(areaNames, info.getCityId()));
        req.setDistrict(areaName(areaNames, info.getDistrictId()));
        req.setStreet(areaName(areaNames, info.getStreetId()));
        req.setCloudId(info.getCloudAccount());
        req.setMerchantName(info.getCompanyName());
        req.setCreditCode(info.getCreditCode());
        req.setManageAddress(info.getActualAddress());
        req.setIndustry(info.getIndustryId());
        req.setContactAddress(info.getRegAddress());
        req.setContactPersonList(buildPersonList(info.getCompanyPrincipal(), info.getPrincipalPhone()));
        req.setSafetyPersonList(buildSafetyPersonList(info));
        req.setBroadbandServiceType(BROADBAND_SERVICE_TYPE);
        req.setBroadbandAccountList(buildBroadbandAccounts(info.getBroadbandAccount()));
        req.setDeviceList(buildDeviceList(info));
        req.setComboList(buildComboList(info));
        return req;
    }

    /** 企业负责人列表：台账只登记 1 位，两个字段都为空时不传该条 */
    private List<FlameGuardianOrderReqDTO.Person> buildPersonList(String name, String phone) {
        if (StringUtils.isBlank(name)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(buildPerson(name, phone));
    }

    /** 企业安全责任人列表：台账最多登记 2 位，按登记顺序返回 */
    private List<FlameGuardianOrderReqDTO.Person> buildSafetyPersonList(ZqztLedgerInfo info) {
        List<FlameGuardianOrderReqDTO.Person> list = new ArrayList<>(2);
        if (StringUtils.isNotBlank(info.getSafetyOfficer1())) {
            list.add(buildPerson(info.getSafetyOfficer1(), info.getSafetyOfficer1Phone()));
        }
        if (StringUtils.isNotBlank(info.getSafetyOfficer2())) {
            list.add(buildPerson(info.getSafetyOfficer2(), info.getSafetyOfficer2Phone()));
        }
        return list;
    }

    private FlameGuardianOrderReqDTO.Person buildPerson(String name, String phone) {
        FlameGuardianOrderReqDTO.Person person = new FlameGuardianOrderReqDTO.Person();
        person.setPersonName(name);
        person.setPersonPhone(phone);
        return person;
    }

    /** 带宽账号：台账为单个字段，按接口要求转成数组 */
    private List<String> buildBroadbandAccounts(String account) {
        if (StringUtils.isBlank(account)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(account);
    }

    /** 设备列表：设备信息取自台账云智眼配置子表，带宽账号取主表 */
    private List<FlameGuardianOrderReqDTO.Device> buildDeviceList(ZqztLedgerInfo info) {
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, info.getId())
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
        List<FlameGuardianOrderReqDTO.Device> list = new ArrayList<>();
        if (configs == null) {
            return list;
        }
        for (ZqztLedgerCloudConfig config : configs) {
            FlameGuardianOrderReqDTO.Device device = new FlameGuardianOrderReqDTO.Device();
            device.setDeviceName(config.getDeviceName());
            device.setDeviceType(toApiDeviceType(config.getDeviceType()));
            device.setChannelCount(config.getCameraChannelCount() == null ? 1 : config.getCameraChannelCount());
            device.setPassword(config.getDevicePassword());
            device.setBroadbandAccount(info.getBroadbandAccount());
            list.add(device);
        }
        return list;
    }

    /** 台账设备类型文案转接口编码：仅 NVR设备 为 4，其余按 IPC摄像头 处理 */
    private Integer toApiDeviceType(String deviceType) {
        return DEVICE_TYPE_NVR_TEXT.equals(deviceType) ? API_DEVICE_TYPE_NVR : API_DEVICE_TYPE_IPC;
    }

    /** 资源包列表：台账只登记一个视频资源包，接入数量按接口最新口径取模块1的签约路数 */
    private List<FlameGuardianOrderReqDTO.Combo> buildComboList(ZqztLedgerInfo info) {
        if (StringUtils.isBlank(info.getCloudVisionPackageId())) {
            return Collections.emptyList();
        }
        FlameGuardianOrderReqDTO.Combo combo = new FlameGuardianOrderReqDTO.Combo();
        combo.setComboId(info.getCloudVisionPackageId());
        combo.setChannelAllocate(info.getSignRoutes());
        return Collections.singletonList(combo);
    }

    // ==================== 区域名称 ====================

    /** 一次性把本轮涉及的省市县街道 code 换成名称，避免逐条查库 */
    private Map<String, String> loadAreaNames(List<ZqztLedgerInfo> list) {
        Set<String> codes = new HashSet<>();
        for (ZqztLedgerInfo info : list) {
            addIfNotBlank(codes, info.getCityId());
            addIfNotBlank(codes, info.getDistrictId());
            addIfNotBlank(codes, info.getStreetId());
        }
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ZqztLedgerArea> areas = areaMapper.selectList(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .in(ZqztLedgerArea::getOrgCode, codes));
        Map<String, String> names = new HashMap<>();
        if (areas == null) {
            return names;
        }
        for (ZqztLedgerArea area : areas) {
            names.put(area.getOrgCode(), area.getOrgName());
        }
        return names;
    }

    private String areaName(Map<String, String> areaNames, String orgCode) {
        return StringUtils.isBlank(orgCode) ? null : areaNames.get(orgCode);
    }

    private void addIfNotBlank(Set<String> target, String value) {
        if (StringUtils.isNotBlank(value)) {
            target.add(value);
        }
    }
}
