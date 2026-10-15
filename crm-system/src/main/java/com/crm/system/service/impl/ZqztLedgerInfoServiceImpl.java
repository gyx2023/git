package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    /**
     * 台账全流程状态：1-已完成 / 0-未完成 / -1-作废 / 2-派单失败。
     */
    private static final String STATUS_FINISHED = "1";
    private static final String STATUS_CANCELED = "-1";

    /** 火焰卫士同步状态：待同步（2-同步中 / 3-同步成功 / 4-同步失败 由同步任务维护） */
    private static final String FLAME_SYN_PENDING = "1";

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
    /** 云智眼密码打码占位（详情页/修改回显统一返回该值，原值需短信校验后单独查看） */
    private static final String PASSWORD_MASK = "********";

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
        lqw.orderByDesc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> page = ledgerInfoMapper.selectPageList(pageQuery.build(), lqw);
        fillNames(page.getRecords());
        fillCameraFinishDate(page.getRecords());
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
        // 云智眼密码不随回显下发原值：改密走短信校验接口，查看原值走短信校验接口
        if (StringUtils.isNotBlank(vo.getCloudPassword())) {
            vo.setCloudPassword(PASSWORD_MASK);
        }
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
        return insertByBo(bo, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ZqztLedgerInfoBo bo, String status, String remark) {
        // 新增 / 落台账不做辖区写入校验：辖区由 OB 工单自动带出（与操作人辖区无关），
        // 硬拦会让跨区派单的客户经理在待办页「通过」时直接失败
        ZqztLedgerInfo entity = BeanUtil.toBean(bo, ZqztLedgerInfo.class);
        String username = currentUsername();
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);
        entity.setIsValid(VALID);
        entity.setStatus(StringUtils.isNotBlank(status)
            ? status
            : (entity.getFullProcessDate() != null ? STATUS_FINISHED : INVALID));
        if (StringUtils.isNotBlank(remark)) {
            entity.setRemark(remark);
        }
        // 新增即进入火焰卫士同步队列
        entity.setFlameGuardianSyn(FLAME_SYN_PENDING);
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
        // 云智眼密码不通过修改表单更新（走短信校验的改密接口）
        entity.setCloudPassword(exist.getCloudPassword());
        entity.setCloudOrgId(exist.getCloudOrgId());
        entity.setCloudAccessKey(exist.getCloudAccessKey());
        entity.setSafetyOfficer2(exist.getSafetyOfficer2());
        entity.setSafetyOfficer2Phone(exist.getSafetyOfficer2Phone());
        entity.setBroadbandAccount(exist.getBroadbandAccount());
        entity.setCameraAccount(exist.getCameraAccount());
        entity.setBroadbandFinishDate(exist.getBroadbandFinishDate());
        entity.setCameraFinishDate(exist.getCameraFinishDate());
        entity.setBroadbandIsReuse(exist.getBroadbandIsReuse());
        entity.setInfoEntryStatus(exist.getInfoEntryStatus());
        entity.setStatus(exist.getStatus());
        entity.setFullProcessDate(exist.getFullProcessDate());
        entity.setCreateTime(exist.getCreateTime());
        entity.setCreatedBy(exist.getCreatedBy());
        entity.setUpdateTime(new Date());
        entity.setUpdatedBy(currentUsername());
        entity.setIsValid(VALID);
        entity.setFlameGuardianSyn(exist.getFlameGuardianSyn() != null
            ? exist.getFlameGuardianSyn() : FLAME_SYN_PENDING);
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

    /** 回调结果字段：RPA 重发时整体置空，等待平台重新回填 */
    private static final String RPA_FAIL = "false";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncFlameGuardian(Long id, Boolean syncToFlameGuardian) {
        ZqztLedgerInfo exist = getValidEntity(id);
        // 已作废 / 已完成的台账不再同步火焰卫士平台
        if (STATUS_CANCELED.equals(exist.getStatus())) {
            throw new ServiceException("该台账已作废，不允许同步火焰卫士平台");
        }
        if (STATUS_FINISHED.equals(exist.getStatus())) {
            throw new ServiceException("该台账全流程已完成，不允许同步火焰卫士平台");
        }
        if (!RPA_FAIL.equals(exist.getAllSuccess())) {
            // 需求：只允许 all_success 有值且等于失败时才允许发起
            throw new ServiceException("仅允许对 RPA 执行失败的台账发起同步");
        }
        LambdaUpdateWrapper<ZqztLedgerInfo> update = Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                .eq(ZqztLedgerInfo::getId, id)
                .set(ZqztLedgerInfo::getAllSuccess, null)
                .set(ZqztLedgerInfo::getAllDesc, null)
                .set(ZqztLedgerInfo::getAllTime, null)
                .set(ZqztLedgerInfo::getStepList, null)
                .set(ZqztLedgerInfo::getFlameGuardianStartTime, null)
                .set(ZqztLedgerInfo::getFlameGuardianEndTime, null)
                .set(ZqztLedgerInfo::getUpdateTime, new Date());
        if (Boolean.TRUE.equals(syncToFlameGuardian)) {
            update.set(ZqztLedgerInfo::getFlameGuardianSyn, FLAME_SYN_PENDING);
        }
        return ledgerInfoMapper.update(null, update) > 0;
    }

    @Override
    public List<ZqztLedgerInfoExportVo> exportPageList(ZqztLedgerInfoBo bo, Page<ZqztLedgerInfoExportVo> page,
                                                       String areaCode, String custManagerOauser) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = buildQueryWrapper(bo);
        applyExportScope(lqw, areaCode, custManagerOauser);
        lqw.orderByDesc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> voPage = ledgerInfoMapper.selectPageList(
            new Page<>(page.getCurrent(), page.getSize(), false), lqw);
        List<ZqztLedgerInfoVo> records = voPage.getRecords();
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        fillNames(records);
        fillCameraFinishDate(records);
        List<ZqztLedgerInfoExportVo> exportList = new ArrayList<>(records.size());
        for (ZqztLedgerInfoVo vo : records) {
            ZqztLedgerInfoExportVo ev = BeanUtil.toBean(vo, ZqztLedgerInfoExportVo.class);
            ev.setStatus(statusText(vo.getStatus()));
            exportList.add(ev);
        }
        return exportList;
    }

    @Override
    public ZqztCloudConfigRespDTO queryCameraConfig(String cameraAccount) {
        // 组网号码（原摄像头账号）已迁到子表：按子表 camera_account 定位所属台账
        ZqztLedgerCloudConfig matched = cloudConfigMapper.selectOne(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getCameraAccount, cameraAccount)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getId)
            .last("limit 1"));
        if (matched == null || matched.getLedgerId() == null) {
            return null;
        }
        ZqztLedgerInfo info = ledgerInfoMapper.selectById(matched.getLedgerId());
        if (info == null || !VALID.equals(info.getIsValid())) {
            return null;
        }
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, info.getId())
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
        ZqztCloudConfigRespDTO dto = new ZqztCloudConfigRespDTO();
        dto.setCameraAccount(cameraAccount);
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
                item.setBroadbandAccount(c.getBroadbandAccount());
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
        verifySmsCode(smsCode);
        String username = currentUsername();
        return ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
            .eq(ZqztLedgerInfo::getId, ledgerId)
            .set(ZqztLedgerInfo::getCloudPassword, newPassword)
            .set(ZqztLedgerInfo::getUpdateTime, new Date())
            .set(StringUtils.isNotBlank(username), ZqztLedgerInfo::getUpdatedBy, username)) > 0;
    }

    @Override
    public String viewCloudAccountPassword(Long ledgerId, String smsCode) {
        ZqztLedgerInfo entity = getValidEntity(ledgerId);
        checkRegionAllowed(entity);
        verifySmsCode(smsCode);
        return entity.getCloudPassword();
    }

    @Override
    public String getCurrentUserMaskedPhone() {
        return maskPhone(currentUserPhone());
    }

    // ==================== 私有辅助 ====================

    /**
     * 校验当前登录用户手机收到的短信验证码
     * <p>校验通过后删除验证码与重发限流标记（防重放，且不阻塞下一次获取）。</p>
     */
    private void verifySmsCode(String smsCode) {
        if (StringUtils.isBlank(smsCode)) {
            throw new ServiceException("短信验证码不能为空");
        }
        String phone = currentUserPhone();
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
    }

    /** 当前登录用户手机号（未绑定则抛业务异常） */
    private String currentUserPhone() {
        LoginUser loginUser = currentLoginUser();
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
        // RPA执行成功
        if (StringUtils.isNotBlank(bo.getAllSuccess())) {
            lqw.eq(ZqztLedgerInfo::getAllSuccess, bo.getAllSuccess().trim());
        }
        // 宽带实际完成时间（范围 + 未完成含空值）
        applyBroadbandActualDateFilter(lqw, bo);
        return lqw;
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

    /** 登录用户区域范围：返回 台账区域 org_code 与层级；省级/管理员/无法解析返回 null */
    private RegionScope resolveRegionScope() {
        LoginUser loginUser = currentLoginUser();
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

    /** 全流程状态 → 导出 */
    private String statusText(String status) {
        if (STATUS_FINISHED.equals(status)) {
            return "已完成";
        }
        if (STATUS_CANCELED.equals(status)) {
            return "已作废";
        }
        if ("2".equals(status)) {
            return "派单失败";
        }
        return "未完成";
    }

    /**
     * 组网竣工时间：取各设备组竣工时间的最大值
     * 列表页与导出共用本方法
     */
    private void fillCameraFinishDate(List<ZqztLedgerInfoVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> ledgerIds = new HashSet<>();
        for (ZqztLedgerInfoVo vo : records) {
            if (vo.getId() != null) {
                ledgerIds.add(vo.getId());
            }
        }
        if (ledgerIds.isEmpty()) {
            return;
        }
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(
            Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
                .select(ZqztLedgerCloudConfig::getLedgerId, ZqztLedgerCloudConfig::getCameraFinishDate)
                .in(ZqztLedgerCloudConfig::getLedgerId, ledgerIds)
                .eq(ZqztLedgerCloudConfig::getIsValid, VALID));
        Map<Long, Date> maxByLedger = new HashMap<>();
        if (configs != null) {
            for (ZqztLedgerCloudConfig cfg : configs) {
                Date finishDate = cfg.getCameraFinishDate();
                if (cfg.getLedgerId() == null || finishDate == null) {
                    continue;
                }
                Date current = maxByLedger.get(cfg.getLedgerId());
                if (current == null || finishDate.after(current)) {
                    maxByLedger.put(cfg.getLedgerId(), finishDate);
                }
            }
        }
        for (ZqztLedgerInfoVo vo : records) {
            vo.setCameraFinishDate(maxByLedger.get(vo.getId()));
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
     * <p>仅云智眼账户密码固定打码，原值需短信验证码校验后单独查看；
     * 其余字段（摄像头施工账号、接入秘钥、设备密码、注册地址等）均明文展示。</p>
     */
    private void maskSensitive(ZqztLedgerInfoVo vo) {
        if (StringUtils.isNotBlank(vo.getCloudPassword())) {
            vo.setCloudPassword(PASSWORD_MASK);
        }
    }

    private String currentUsername() {
        LoginUser loginUser = currentLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 当前登录用户；无登录上下文（xxl-job 等非 HTTP 线程 / 未登录）返回 null
     */
    private LoginUser currentLoginUser() {
        try {
            return LoginHelper.getLoginUser();
        } catch (Exception e) {
            return null;
        }
    }

    /** 登录用户区域范围（内部辅助类） */
    private static class RegionScope {
        String level;
        String orgCode;
    }
}
