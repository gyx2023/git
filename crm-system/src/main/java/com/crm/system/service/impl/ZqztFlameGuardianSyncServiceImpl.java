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
import java.util.LinkedHashSet;
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

    /** 台账全流程状态：0-未完成 / 2-派单失败（只有这两种才推送火焰卫士平台） */
    private static final String STATUS_UNFINISHED = "0";
    private static final String STATUS_DISPATCH_FAILED = "2";

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

    /** 接口 broadbandAccountList 最多 10 个 */
    private static final int BROADBAND_ACCOUNT_MAX = 10;

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
            // 只有「未完成 / 派单失败」才推送：已作废(-1) 与 已完成(1) 都不再同步。
            // 本轮已抢占(syn=2)的行随后 markFinished 会无条件写回 3/4（其中 4 会重新入队），
            // 所以必须把不推的单挡在选取之外，而不能只靠 syn 状态。
            .in(ZqztLedgerInfo::getStatus, STATUS_UNFINISHED, STATUS_DISPATCH_FAILED)
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
            // 与扫描同一套选取条件：扫描后、抢锁前被作废或标记完成的单不再抢锁，也就不发起推送
            .in(ZqztLedgerInfo::getStatus, STATUS_UNFINISHED, STATUS_DISPATCH_FAILED)
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
        // 宽带账号与设备信息同源，子表只查一次，避免两次相同查询
        List<ZqztLedgerCloudConfig> configs = loadValidConfigs(info.getId());
        req.setBroadbandAccountList(buildBroadbandAccounts(configs));
        req.setDeviceList(buildDeviceList(configs));
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

    /**
     * 企业安全责任人列表：安全负责人1 支持多组登记（姓名与联系方式在同一字段内用「|」分隔，序号一一对应），
     * 按「|」拆分后逐组展开为独立的人。
     * <p>安全负责人2 已下线、不再由表单维护（数据库列保留待删），故不再进入同步报文。</p>
     */
    private List<FlameGuardianOrderReqDTO.Person> buildSafetyPersonList(ZqztLedgerInfo info) {
        List<FlameGuardianOrderReqDTO.Person> list = new ArrayList<>();
        // 姓名与联系方式按下标配对，只遍历两者中较短的长度，避免多组错位时产生半空的人
        String[] names = splitMultiGroup(info.getSafetyOfficer1());
        String[] phones = splitMultiGroup(info.getSafetyOfficer1Phone());
        if (phones.length == 0 && names.length > 0) {
            // 未填联系方式时按「每组都是空联系方式」处理，补齐到姓名组数，
            // 既保持单组登记（有姓名无电话）原有行为，也避免多组姓名被截断丢失
            phones = new String[names.length];
        }
        int size = Math.min(names.length, phones.length);
        for (int i = 0; i < size; i++) {
            // 姓名缺失的组直接跳过，与原「姓名为空不传该条」的行为一致
            if (StringUtils.isNotBlank(names[i])) {
                list.add(buildPerson(names[i], phones[i]));
            }
        }
        return list;
    }

    /**
     * 按「|」拆分多组登记值；空白输入返回空数组。
     * <p>limit 取 -1：末尾分隔符保留为空串而非被静默丢弃，交由调用方按需跳过。</p>
     */
    private String[] splitMultiGroup(String value) {
        if (StringUtils.isBlank(value)) {
            return new String[0];
        }
        return value.split("\\|", -1);
    }

    private FlameGuardianOrderReqDTO.Person buildPerson(String name, String phone) {
        FlameGuardianOrderReqDTO.Person person = new FlameGuardianOrderReqDTO.Person();
        person.setPersonName(name);
        person.setPersonPhone(phone);
        return person;
    }

    /**
     * 宽带账号列表：从配置子表拼接（需求 §5.4(3) 取数口径变更）
     * <p>主表 broadband_account 列保留但不再参与取数（待删列，勿新增引用）。</p>
     */
    private List<String> buildBroadbandAccounts(List<ZqztLedgerCloudConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> ordered = new LinkedHashSet<>();
        for (ZqztLedgerCloudConfig config : configs) {
            if (StringUtils.isNotBlank(config.getBroadbandAccount())) {
                ordered.add(config.getBroadbandAccount().trim());
            }
        }
        if (ordered.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(ordered);
        if (list.size() > BROADBAND_ACCOUNT_MAX) {
            log.warn("【火焰卫士同步】宽带账号去重后 {} 个，超过接口上限 {}，只取前 {} 个",
                    list.size(), BROADBAND_ACCOUNT_MAX, BROADBAND_ACCOUNT_MAX);
            list = list.subList(0, BROADBAND_ACCOUNT_MAX);
        }
        return list;
    }

    /** 取该台账下有效的配置子表行（按 sort_order 升序） */
    private List<ZqztLedgerCloudConfig> loadValidConfigs(Long ledgerId) {
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
        return configs == null ? Collections.emptyList() : configs;
    }

    /** 设备列表：设备信息和带宽账号均取自台账云智眼配置子表 */
    private List<FlameGuardianOrderReqDTO.Device> buildDeviceList(List<ZqztLedgerCloudConfig> configs) {
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
            device.setBroadbandAccount(config.getBroadbandAccount());
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
