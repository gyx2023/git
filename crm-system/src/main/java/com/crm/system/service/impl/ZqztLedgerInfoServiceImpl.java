package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.constant.CacheConstants;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.redis.RedisUtils;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.domain.dto.ZqztCloudConfigItemDTO;
import com.crm.system.domain.dto.ZqztCloudConfigRespDTO;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.ZqztLedgerCloudConfigVo;
import com.crm.system.domain.vo.ZqztLedgerInfoExportVo;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.domain.vo.send.ConOpenSend;
import com.crm.system.mapper.ZqztLedgerAreaMapper;
import com.crm.system.mapper.ZqztLedgerCloudConfigMapper;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.send.impl.ConOpenSendImpl;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 台账数据主表 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztLedgerInfoServiceImpl implements IZqztLedgerInfoService {

    private static final String VALID = "1";
    private static final String INVALID = "0";

    /** 云智眼改密短信模板编码（能开 VERIFY_04，登录验证码模板） */
    private static final String SMS_VERIFY_TEMPLATE = "VERIFY_04";
    /** 能开短信发送成功码 */
    private static final String SMS_SUCCESS_CODE = "00000";
    /** 短信验证码有效期 */
    private static final Duration SMS_CODE_TTL = Duration.ofMinutes(3);
    /** 短信验证码重发间隔（秒） */
    private static final long SMS_RESEND_SECONDS = 60;
    /** 能开短信模板里的平台名称 */
    private static final String SMS_SYSTEM_NAME = "浙江政企运营平台";

    /** 行业字典 */
    private static final Map<String, String> INDUSTRY_MAP = new HashMap<String, String>() {{
        put("1", "尾矿库"); put("2", "露天矿山"); put("3", "地下矿山");
        put("4", "机械"); put("5", "建材"); put("6", "商贸");
        put("7", "纺织"); put("8", "轻工"); put("9", "烟草");
        put("10", "有色"); put("11", "冶金"); put("12", "危化");
        put("13", "其他");
    }};

    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final ZqztLedgerCloudConfigMapper cloudConfigMapper;
    private final ZqztLedgerAreaMapper areaMapper;
    private final ISysUserService sysUserService;
    private final ISysDeptService sysDeptService;
    private final ISysAreaService sysAreaService;
    private final ConOpenSendImpl conOpenSendImpl;

    @Override
    public TableDataInfo<ZqztLedgerInfoVo> queryPageList(ZqztLedgerInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = buildQueryWrapper(bo);
        applyRegionScope(lqw);
        lqw.orderByAsc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> page = ledgerInfoMapper.selectPageList(pageQuery.build(), lqw);
        fillNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public ZqztLedgerInfoVo queryById(Long id) {
        ZqztLedgerInfo entity = getValidEntity(id);
        checkRegionAllowed(entity);
        ZqztLedgerInfoVo vo = toVo(entity);
        fillNames(Collections.singletonList(vo));
        vo.setCloudVisionConfigs(loadCloudConfigs(id));
        maskSensitive(vo);
        return vo;
    }

    @Override
    public ZqztLedgerInfoVo queryForEdit(Long id) {
        ZqztLedgerInfo entity = getValidEntity(id);
        checkRegionAllowed(entity);
        ZqztLedgerInfoVo vo = toVo(entity);
        vo.setCloudVisionConfigs(loadCloudConfigs(id));
        return vo;
    }

    @Override
    public Boolean existsByOrderNo(String orderNo) {
        return ledgerInfoMapper.selectCount(Wrappers.<ZqztLedgerInfo>lambdaQuery()
            .eq(ZqztLedgerInfo::getOrderNo, orderNo)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ZqztLedgerInfoBo bo) {
        checkRegionAllowedForSave(bo.getCityId(), bo.getDistrictId());
        ZqztLedgerInfo entity = BeanUtil.toBean(bo, ZqztLedgerInfo.class);
        String username = currentUsername();
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);
        entity.setIsValid(VALID);
        // 全流程状态由完成时间推导：填了=已完成，没填=未完成
        entity.setStatus(entity.getFullProcessDate() != null ? VALID : INVALID);
        int rows = ledgerInfoMapper.insert(entity);
        saveCloudConfigs(entity.getId(), bo.getCloudVisionConfigs());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ZqztLedgerInfoBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("主键不能为空");
        }
        ZqztLedgerInfo exist = getValidEntity(bo.getId());
        checkRegionAllowed(exist);
        checkRegionAllowedForSave(bo.getCityId(), bo.getDistrictId());
        // 工单号不允许修改：以库中为准
        bo.setOrderNo(null);
        ZqztLedgerInfo entity = BeanUtil.toBean(bo, ZqztLedgerInfo.class);
        entity.setId(bo.getId());
        entity.setOrderNo(exist.getOrderNo());
        entity.setCreateTime(exist.getCreateTime());
        entity.setCreatedBy(exist.getCreatedBy());
        entity.setUpdateTime(new Date());
        entity.setUpdatedBy(currentUsername());
        entity.setIsValid(VALID);
        // 全流程状态由完成时间推导：填了=已完成，没填=未完成
        entity.setStatus(entity.getFullProcessDate() != null ? VALID : INVALID);
        int rows = ledgerInfoMapper.updateById(entity);
        saveCloudConfigs(bo.getId(), bo.getCloudVisionConfigs());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        Date now = new Date();
        String username = currentUsername();
        for (Long id : ids) {
            ZqztLedgerInfo exist = ledgerInfoMapper.selectById(id);
            if (exist == null || !VALID.equals(exist.getIsValid())) {
                continue;
            }
            checkRegionAllowed(exist);
            // 主表逻辑删除
            ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                .eq(ZqztLedgerInfo::getId, id)
                .set(ZqztLedgerInfo::getIsValid, INVALID)
                .set(ZqztLedgerInfo::getUpdateTime, now)
                .set(StringUtils.isNotBlank(username), ZqztLedgerInfo::getUpdatedBy, username));
            // 子表逻辑删除
            cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                .eq(ZqztLedgerCloudConfig::getLedgerId, id)
                .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
                .set(ZqztLedgerCloudConfig::getIsValid, INVALID)
                .set(ZqztLedgerCloudConfig::getUpdateTime, now));
        }
        return true;
    }

    @Override
    public List<ZqztLedgerInfoExportVo> exportPageList(ZqztLedgerInfoBo bo, Page<ZqztLedgerInfoExportVo> page,
                                                       String areaCode, String custManagerOauser) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = buildQueryWrapper(bo);
        // job-server 无登录态，数据权限显式传入
        applyExportScope(lqw, areaCode, custManagerOauser);
        lqw.orderByAsc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> voPage = ledgerInfoMapper.selectPageList(
            new Page<>(page.getCurrent(), page.getSize(), false), lqw);
        List<ZqztLedgerInfoVo> records = voPage.getRecords();
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        fillNames(records);
        List<ZqztLedgerInfoExportVo> exportList = new ArrayList<>(records.size());
        for (ZqztLedgerInfoVo vo : records) {
            ZqztLedgerInfoExportVo ev = BeanUtil.toBean(vo, ZqztLedgerInfoExportVo.class);
            ev.setStatus("1".equals(vo.getStatus()) ? "已完成" : "未完成");
            exportList.add(ev);
        }
        return exportList;
    }

    @Override
    public ZqztCloudConfigRespDTO queryCameraConfig(String cameraAccount) {
        ZqztLedgerInfo info = ledgerInfoMapper.selectOne(Wrappers.<ZqztLedgerInfo>lambdaQuery()
            .eq(ZqztLedgerInfo::getCameraAccount, cameraAccount)
            .eq(ZqztLedgerInfo::getIsValid, VALID)
            .orderByAsc(ZqztLedgerInfo::getCreateTime)
            .last("limit 1"));
        if (info == null) {
            return null;
        }
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, info.getId())
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
        ZqztCloudConfigRespDTO dto = new ZqztCloudConfigRespDTO();
        dto.setCameraAccount(info.getCameraAccount());
        dto.setOrderNo(info.getOrderNo());
        List<ZqztCloudConfigItemDTO> items = new ArrayList<>();
        if (configs != null) {
            for (ZqztLedgerCloudConfig c : configs) {
                ZqztCloudConfigItemDTO item = new ZqztCloudConfigItemDTO();
                item.setDeviceName(c.getDeviceName());
                item.setDeviceType(c.getDeviceType());
                item.setAccessProtocol(c.getAccessProtocol());
                item.setChannelNum(c.getCameraChannelCount() == null ? "1" : String.valueOf(c.getCameraChannelCount()));
                item.setDeviceId(c.getDeviceId());
                item.setDevicePassword(c.getDevicePassword());
                item.setGbId(c.getGbId());
                item.setSipServerId(c.getSipServerId());
                item.setSipServiceDomain(c.getSipServiceDomain());
                item.setSipServerPort(c.getSipServerPort());
                item.setSipServerIp(c.getSipServerIp());
                item.setChannelCodeId(c.getChannelCodeId());
                items.add(item);
            }
        }
        dto.setCloudConfigList(items);
        return dto;
    }

    @Override
    public void sendCloudAccountSmsCode(Long ledgerId) {
        ZqztLedgerInfo entity = getValidEntity(ledgerId);
        checkRegionAllowed(entity);
        String phone = currentUserPhone();
        // 60 秒内不重发
        String rateKey = CacheConstants.RATE_LIMIT_KEY + "sms:" + phone;
        if (RedisUtils.hasKey(rateKey)) {
            throw new ServiceException("验证码已发送，请60秒后再试");
        }
        String code = String.valueOf(RandomUtil.randomInt(100000, 1000000));
        RedisUtils.setCacheObject(CacheConstants.CAPTCHA_CODE_KEY + phone, code, SMS_CODE_TTL);
        RedisUtils.setCacheObject(rateKey, "1", Duration.ofSeconds(SMS_RESEND_SECONDS));
        Map<String, Object> paramMap = new HashMap<>(2);
        paramMap.put("SYSTEM", SMS_SYSTEM_NAME);
        paramMap.put("CAPTCHA", code);
        ConOpenSend send = conOpenSendImpl.sendMessageByTemplate(SMS_VERIFY_TEMPLATE, paramMap, phone);
        if (send == null) {
            throw new ServiceException("短信发送失败：网络连接超时");
        }
        if (!SMS_SUCCESS_CODE.equals(send.getRespCode())) {
            throw new ServiceException("短信发送失败：" + send.getRespDesc());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCloudAccountPassword(Long ledgerId, String newPassword, String smsCode) {
        ZqztLedgerInfo entity = getValidEntity(ledgerId);
        checkRegionAllowed(entity);
        if (StringUtils.isBlank(newPassword)) {
            throw new ServiceException("新密码不能为空");
        }
        if (StringUtils.isBlank(smsCode)) {
            throw new ServiceException("短信验证码不能为空");
        }
        String phone = currentUserPhone();
        // 校验验证码（成功后删除，防重放）
        String key = CacheConstants.CAPTCHA_CODE_KEY + phone;
        String cachedCode = RedisUtils.getCacheObject(key);
        if (StringUtils.isBlank(cachedCode)) {
            throw new ServiceException("验证码已过期或未发送，请重新获取");
        }
        if (!cachedCode.equals(smsCode)) {
            throw new ServiceException("验证码错误");
        }
        RedisUtils.deleteObject(key);
        RedisUtils.deleteObject(CacheConstants.RATE_LIMIT_KEY + "sms:" + phone);
        // 仅更新云智眼密码
        ZqztLedgerInfo update = new ZqztLedgerInfo();
        update.setId(ledgerId);
        update.setCloudPassword(newPassword);
        update.setUpdateTime(new Date());
        update.setUpdatedBy(currentUsername());
        return ledgerInfoMapper.updateById(update) > 0;
    }

    @Override
    public String getCurrentUserMaskedPhone() {
        return maskPhone(currentUserPhone());
    }

    // ==================== 私有辅助 ====================

    /** 当前登录用户手机号（未绑定则抛业务异常） */
    private String currentUserPhone() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUser user = loginUser == null ? null : sysUserService.selectUserById(loginUser.getUserId());
        String phone = user == null ? null : user.getPhonenumber();
        if (StringUtils.isBlank(phone)) {
            throw new ServiceException("当前账号未绑定手机号，无法发送/校验短信验证码");
        }
        return phone;
    }

    /** 手机号脱敏：保留前3后4，中间打码 */
    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private ZqztLedgerInfo getValidEntity(Long id) {
        if (id == null) {
            throw new ServiceException("主键不能为空");
        }
        ZqztLedgerInfo entity = ledgerInfoMapper.selectById(id);
        if (entity == null || !VALID.equals(entity.getIsValid())) {
            throw new ServiceException("台账不存在或已被删除");
        }
        return entity;
    }

    private LambdaQueryWrapper<ZqztLedgerInfo> buildQueryWrapper(ZqztLedgerInfoBo bo) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ZqztLedgerInfo::getIsValid, VALID);
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), ZqztLedgerInfo::getOrderNo, bo.getOrderNo());
        lqw.ge(StringUtils.isNotBlank(bo.getSignDateStart()), ZqztLedgerInfo::getSignDate, bo.getSignDateStart());
        lqw.le(StringUtils.isNotBlank(bo.getSignDateEnd()), ZqztLedgerInfo::getSignDate, bo.getSignDateEnd());
        lqw.eq(StringUtils.isNotBlank(bo.getCityId()), ZqztLedgerInfo::getCityId, bo.getCityId());
        lqw.eq(StringUtils.isNotBlank(bo.getDistrictId()), ZqztLedgerInfo::getDistrictId, bo.getDistrictId());
        lqw.eq(StringUtils.isNotBlank(bo.getStreetId()), ZqztLedgerInfo::getStreetId, bo.getStreetId());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryId()), ZqztLedgerInfo::getIndustryId, bo.getIndustryId());
        lqw.like(StringUtils.isNotBlank(bo.getCompanyName()), ZqztLedgerInfo::getCompanyName, bo.getCompanyName());
        lqw.like(StringUtils.isNotBlank(bo.getUnicomManager()), ZqztLedgerInfo::getUnicomManager, bo.getUnicomManager());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ZqztLedgerInfo::getStatus, bo.getStatus());
        // 零星工程（1是/0否）
        lqw.eq(StringUtils.isNotBlank(bo.getBroadbandIsSporadic()), ZqztLedgerInfo::getBroadbandIsSporadic, bo.getBroadbandIsSporadic());
        // 企业信息录入状态（1是/0否）
        lqw.eq(StringUtils.isNotBlank(bo.getInfoEntryStatus()), ZqztLedgerInfo::getInfoEntryStatus, bo.getInfoEntryStatus());
        // 机构ID（云智眼监管信息机构ID，模糊匹配 + 含空值）
        applyOrgIdFilter(lqw, bo);
        // 宽带实际完成时间（范围 + 未完成含空值）
        applyBroadbandActualDateFilter(lqw, bo);
        // 设备ID（子表，模糊匹配 + 含空值）
        applyDeviceIdFilter(lqw, bo);
        return lqw;
    }

    /** 机构ID筛选：模糊匹配 + 「含空值」 */
    private void applyOrgIdFilter(LambdaQueryWrapper<ZqztLedgerInfo> lqw, ZqztLedgerInfoBo bo) {
        boolean hasValue = StringUtils.isNotBlank(bo.getCloudOrgId());
        boolean includeEmpty = Boolean.TRUE.equals(bo.getCloudOrgIdEmpty());
        if (hasValue && includeEmpty) {
            lqw.and(w -> w.like(ZqztLedgerInfo::getCloudOrgId, bo.getCloudOrgId())
                .or().isNull(ZqztLedgerInfo::getCloudOrgId)
                .or().eq(ZqztLedgerInfo::getCloudOrgId, ""));
        } else if (hasValue) {
            lqw.like(ZqztLedgerInfo::getCloudOrgId, bo.getCloudOrgId());
        } else if (includeEmpty) {
            lqw.and(w -> w.isNull(ZqztLedgerInfo::getCloudOrgId)
                .or().eq(ZqztLedgerInfo::getCloudOrgId, ""));
        }
    }

    /** 宽带实际完成时间筛选：范围 + 「未完成」（含空值） */
    private void applyBroadbandActualDateFilter(LambdaQueryWrapper<ZqztLedgerInfo> lqw, ZqztLedgerInfoBo bo) {
        boolean hasStart = StringUtils.isNotBlank(bo.getBroadbandActualDateStart());
        boolean hasEnd = StringUtils.isNotBlank(bo.getBroadbandActualDateEnd());
        boolean includeEmpty = Boolean.TRUE.equals(bo.getBroadbandActualDateEmpty());
        if (hasStart || hasEnd) {
            if (includeEmpty) {
                lqw.and(w -> {
                    w.ge(hasStart, ZqztLedgerInfo::getBroadbandSporadicActualDate, bo.getBroadbandActualDateStart());
                    w.le(hasEnd, ZqztLedgerInfo::getBroadbandSporadicActualDate, bo.getBroadbandActualDateEnd());
                    w.or();
                    w.isNull(ZqztLedgerInfo::getBroadbandSporadicActualDate);
                });
            } else {
                lqw.ge(hasStart, ZqztLedgerInfo::getBroadbandSporadicActualDate, bo.getBroadbandActualDateStart());
                lqw.le(hasEnd, ZqztLedgerInfo::getBroadbandSporadicActualDate, bo.getBroadbandActualDateEnd());
            }
        } else if (includeEmpty) {
            lqw.isNull(ZqztLedgerInfo::getBroadbandSporadicActualDate);
        }
    }

    /** 设备ID筛选（子表 zqzt_ledger_cloud_config，模糊匹配）+ 「含空值」 */
    private void applyDeviceIdFilter(LambdaQueryWrapper<ZqztLedgerInfo> lqw, ZqztLedgerInfoBo bo) {
        boolean hasValue = StringUtils.isNotBlank(bo.getDeviceId());
        boolean includeEmpty = Boolean.TRUE.equals(bo.getDeviceIdEmpty());
        String likeSql = "EXISTS (SELECT 1 FROM zqzt_ledger_cloud_config c WHERE c.ledger_id = zqzt_ledger_info.id "
            + "AND c.device_id LIKE {0} AND c.is_valid = '1')";
        String emptySql = "EXISTS (SELECT 1 FROM zqzt_ledger_cloud_config c WHERE c.ledger_id = zqzt_ledger_info.id "
            + "AND (c.device_id IS NULL OR c.device_id = '') AND c.is_valid = '1')";
        if (hasValue && includeEmpty) {
            lqw.and(w -> w.apply(likeSql, "%" + bo.getDeviceId() + "%").or().apply(emptySql));
        } else if (hasValue) {
            lqw.apply(likeSql, "%" + bo.getDeviceId() + "%");
        } else if (includeEmpty) {
            lqw.apply(emptySql);
        }
    }

    /** 登录用户区域范围：返回 台账区域 org_code 与层级；省级/管理员/无法解析返回 null */
    private RegionScope resolveRegionScope() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || LoginHelper.isAdmin()) {
            return null;
        }
        SysUser user = sysUserService.selectUserById(loginUser.getUserId());
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        SysDept dept = sysDeptService.selectDeptById(user.getDeptId());
        if (dept == null || StringUtils.isBlank(dept.getAreaCode())) {
            return null;
        }
        SysAreaVo area = sysAreaService.queryById(Long.valueOf(dept.getAreaCode()));
        if (area == null) {
            return null;
        }
        RegionScope rs = new RegionScope();
        rs.level = area.getType();
        ZqztLedgerArea lc = findAreaByZqId(String.valueOf(area.getId()));
        if (lc != null) {
            rs.orgCode = lc.getOrgCode();
        }
        return rs;
    }

    private void applyRegionScope(LambdaQueryWrapper<ZqztLedgerInfo> lqw) {
        RegionScope rs = resolveRegionScope();
        if (rs == null || StringUtils.isBlank(rs.orgCode)) {
            return;
        }
        if ("二级".equals(rs.level)) {
            lqw.eq(ZqztLedgerInfo::getCityId, rs.orgCode);
        } else if ("三级".equals(rs.level)) {
            lqw.eq(ZqztLedgerInfo::getDistrictId, rs.orgCode);
        }
    }

    private void applyExportScope(LambdaQueryWrapper<ZqztLedgerInfo> lqw, String areaCode, String custManagerOauser) {
        if (StringUtils.isNotBlank(custManagerOauser)) {
            lqw.eq(ZqztLedgerInfo::getCreatedBy, custManagerOauser);
        }
        if (StringUtils.isNotBlank(areaCode) && !"330000".equals(areaCode)) {
            SysAreaVo area = sysAreaService.queryById(Long.valueOf(areaCode));
            if (area == null) {
                throw new ServiceException("导出失败：无法解析区域权限，请联系管理员");
            }
            ZqztLedgerArea lc = findAreaByZqId(areaCode);
            if (lc == null) {
                throw new ServiceException("导出失败：未匹配到台账区域数据，请联系管理员");
            }
            if ("二级".equals(area.getType())) {
                lqw.eq(ZqztLedgerInfo::getCityId, lc.getOrgCode());
            } else if ("三级".equals(area.getType())) {
                lqw.eq(ZqztLedgerInfo::getDistrictId, lc.getOrgCode());
            }
        }
    }

    private void checkRegionAllowed(ZqztLedgerInfo entity) {
        RegionScope rs = resolveRegionScope();
        if (rs == null || StringUtils.isBlank(rs.orgCode)) {
            return;
        }
        if ("二级".equals(rs.level) && !Objects.equals(rs.orgCode, entity.getCityId())) {
            throw new ServiceException("无权查看该台账");
        }
        if ("三级".equals(rs.level) && !Objects.equals(rs.orgCode, entity.getDistrictId())) {
            throw new ServiceException("无权查看该台账");
        }
    }

    /** 新增/修改时校验提交地域在登录用户范围内（防止越界提交） */
    private void checkRegionAllowedForSave(String cityId, String districtId) {
        RegionScope rs = resolveRegionScope();
        if (rs == null || StringUtils.isBlank(rs.orgCode)) {
            return;
        }
        if ("二级".equals(rs.level) && !Objects.equals(rs.orgCode, cityId)) {
            throw new ServiceException("只能在本区域内新增/修改台账");
        }
        if ("三级".equals(rs.level) && !Objects.equals(rs.orgCode, districtId)) {
            throw new ServiceException("只能在本区县内新增/修改台账");
        }
    }

    private ZqztLedgerArea findAreaByZqId(String zqId) {
        return areaMapper.selectOne(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getZqId, zqId)
            .eq(ZqztLedgerArea::getIsValid, VALID)
            .last("limit 1"));
    }

    private void fillNames(List<ZqztLedgerInfoVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<String> codes = new HashSet<>();
        for (ZqztLedgerInfoVo vo : records) {
            addCode(codes, vo.getCityId());
            addCode(codes, vo.getDistrictId());
            addCode(codes, vo.getStreetId());
        }
        Map<String, String> nameMap = new HashMap<>();
        if (!codes.isEmpty()) {
            areaMapper.selectList(Wrappers.<ZqztLedgerArea>lambdaQuery()
                    .in(ZqztLedgerArea::getOrgCode, codes)
                    .eq(ZqztLedgerArea::getIsValid, VALID))
                .forEach(a -> nameMap.put(a.getOrgCode(), a.getOrgName()));
        }
        for (ZqztLedgerInfoVo vo : records) {
            vo.setCityName(nameMap.get(vo.getCityId()));
            vo.setDistrictName(nameMap.get(vo.getDistrictId()));
            vo.setStreetName(nameMap.get(vo.getStreetId()));
            vo.setIndustryName(INDUSTRY_MAP.get(vo.getIndustryId()));
        }
    }

    private void addCode(Set<String> set, String code) {
        if (StringUtils.isNotBlank(code)) {
            set.add(code);
        }
    }

    private List<ZqztLedgerCloudConfigVo> loadCloudConfigs(Long ledgerId) {
        return cloudConfigMapper.selectVoList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
    }

    /**
     * 保存云智眼配置子表
     */
    private void saveCloudConfigs(Long ledgerId, List<com.crm.system.domain.bo.ZqztLedgerCloudConfigBo> configs) {
        List<ZqztLedgerCloudConfig> existList = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID));
        Map<Long, ZqztLedgerCloudConfig> existMap = existList.stream()
            .collect(Collectors.toMap(ZqztLedgerCloudConfig::getId, c -> c));
        Set<Long> submittedIds = new HashSet<>();
        Date now = new Date();
        int sort = 0;
        if (configs != null) {
            for (com.crm.system.domain.bo.ZqztLedgerCloudConfigBo bo : configs) {
                ZqztLedgerCloudConfig cfg = BeanUtil.toBean(bo, ZqztLedgerCloudConfig.class);
                cfg.setLedgerId(ledgerId);
                cfg.setSortOrder(bo.getSortOrder() != null ? bo.getSortOrder() : ++sort);
                if (bo.getId() != null && existMap.containsKey(bo.getId())) {
                    cfg.setCreateTime(existMap.get(bo.getId()).getCreateTime());
                    cfg.setUpdateTime(now);
                    cfg.setIsValid(VALID);
                    cloudConfigMapper.updateById(cfg);
                    submittedIds.add(bo.getId());
                } else {
                    // 新增记录
                    cfg.setId(null);
                    cfg.setCreateTime(now);
                    cfg.setUpdateTime(now);
                    cfg.setIsValid(VALID);
                    cloudConfigMapper.insert(cfg);
                }
            }
        }
        for (ZqztLedgerCloudConfig exist : existList) {
            if (!submittedIds.contains(exist.getId())) {
                cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                    .eq(ZqztLedgerCloudConfig::getId, exist.getId())
                    .set(ZqztLedgerCloudConfig::getIsValid, INVALID)
                    .set(ZqztLedgerCloudConfig::getUpdateTime, now));
            }
        }
    }

    private ZqztLedgerInfoVo toVo(ZqztLedgerInfo entity) {
        return BeanUtil.toBean(entity, ZqztLedgerInfoVo.class);
    }

    /**
     * 敏感字段脱敏（详情页展示）
     * <p>与原型详情页一致：云智眼账号、实际地址明文展示；
     * 注册地址保留后 4 位；摄像头施工账号固定打码；
     * 云智眼密码/接入秘钥、云智眼设备密码固定打码。</p>
     */
    private void maskSensitive(ZqztLedgerInfoVo vo) {
        // 摄像头施工账号：原型固定展示 ****
        vo.setCameraAccount("****");
        if (StringUtils.isNotBlank(vo.getCloudPassword())) {
            vo.setCloudPassword("********");
        }
        if (StringUtils.isNotBlank(vo.getCloudAccessKey())) {
            vo.setCloudAccessKey("********");
        }
        if (vo.getCloudVisionConfigs() != null) {
            for (ZqztLedgerCloudConfigVo cfg : vo.getCloudVisionConfigs()) {
                if (StringUtils.isNotBlank(cfg.getDevicePassword())) {
                    cfg.setDevicePassword("********");
                }
            }
        }
        // 注册地址：原型 '****' + 后4位；实际地址明文展示（不打码）
        vo.setRegAddress(maskAddress(vo.getRegAddress()));
    }

    private String maskAddress(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() > 4) {
            return "****" + value.substring(value.length() - 4);
        }
        return "****";
    }

    private String currentUsername() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /** 登录用户区域范围（内部辅助类） */
    private static class RegionScope {
        String level;
        String orgCode;
    }
}
