package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.mapper.ZqztLedgerCloudConfigMapper;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.service.IZqztFlameGuardianFinishDateService;
import com.crm.system.service.IZqztLedgerUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 火焰卫士竣工时间定时回填服务实现
 * <p>
 * 1) 扫「全流程状态 ∈ {0 未完成, 2 派单失败}」且子表宽带竣工时间 / 摄像头账号竣工时间为空的台账，
 *    据宽带账号与组网号码调「用户信息查询接口」取入网时间回填子表；
 * 2) 同一订单下所有组网号码（子表 camera_account）都竣工时，
 *    取组网号码竣工时间的最大值作为全流程完成时间，并置全流程状态=1。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianFinishDateServiceImpl implements IZqztFlameGuardianFinishDateService {

    private static final String VALID = "1";
    private static final String STATUS_UNFINISHED = "0";
    private static final String STATUS_FINISHED = "1";
    /** 全流程状态：2-派单失败（无待办、由自动取数直接落库的台账，同样要回填竣工时间并推进完成） */
    private static final String STATUS_DISPATCH_FAILED = "2";
    /** 用户信息查询接口 IN_DATE 的格式（与手工「刷新」路径同一口径，带时分秒） */
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 单轮最多处理的台账数，避免长事务 */
    private static final int BATCH_SIZE = 200;

    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final ZqztLedgerCloudConfigMapper cloudConfigMapper;
    private final IZqztLedgerUserInfoService ledgerUserInfoService;

    @Override
    public String runOnce() {
        List<ZqztLedgerInfo> ledgers = ledgerInfoMapper.selectList(Wrappers.<ZqztLedgerInfo>lambdaQuery()
                .eq(ZqztLedgerInfo::getIsValid, VALID)
                // 未完成 + 派单失败都参与（派单失败台账由自动取数落库，同样要回填竣工时间并推进完成）；
                // 已完成 / 已作废不参与
                .in(ZqztLedgerInfo::getStatus, STATUS_UNFINISHED, STATUS_DISPATCH_FAILED)
                // 按最后修改时间倒序取批：卡住的台账（账号为空/始终解析不出竣工时间）
                // 不会永久占据队首把新台账饿死；任何编辑或子表回填都会 bump update_time，自愈重排
                .orderByDesc(ZqztLedgerInfo::getUpdateTime)
                .orderByDesc(ZqztLedgerInfo::getId)
                .last("limit " + BATCH_SIZE));

        int filled = 0;
        int completed = 0;
        int failed = 0;
        for (ZqztLedgerInfo ledger : ledgers) {
            try {
                if (processLedger(ledger)) {
                    completed++;
                }
                filled++;
            } catch (Exception e) {
                failed++;
                log.error("【火焰卫士竣工回填】处理失败: ledgerId={}", ledger.getId(), e);
            }
        }
        return String.format("竣工回填完成: 扫描=%d, 处理=%d, 置完成=%d, 异常=%d",
                ledgers.size(), filled, completed, failed);
    }

    /**
     * 处理单条台账
     *
     * @return 是否本轮被置为「已完成」
     */
    private boolean processLedger(ZqztLedgerInfo ledger) {
        List<ZqztLedgerCloudConfig> configs = cloudConfigMapper.selectList(
                new LambdaQueryWrapper<ZqztLedgerCloudConfig>()
                        .eq(ZqztLedgerCloudConfig::getLedgerId, ledger.getId())
                        .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
                        .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
        if (configs == null || configs.isEmpty()) {
            return false;
        }

        // 1. 回填空的竣工时间
        for (ZqztLedgerCloudConfig config : configs) {
            if (StringUtils.isNotBlank(config.getBroadbandAccount()) && config.getBroadbandFinishDate() == null) {
                Date inDate = queryInDate(config.getBroadbandAccount());
                if (inDate != null) {
                    cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                            .eq(ZqztLedgerCloudConfig::getId, config.getId())
                            .set(ZqztLedgerCloudConfig::getBroadbandFinishDate, inDate)
                            .set(ZqztLedgerCloudConfig::getUpdateTime, new Date()));
                    config.setBroadbandFinishDate(inDate);
                }
            }
            if (StringUtils.isNotBlank(config.getCameraAccount()) && config.getCameraFinishDate() == null) {
                Date inDate = queryInDate(config.getCameraAccount());
                if (inDate != null) {
                    cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                            .eq(ZqztLedgerCloudConfig::getId, config.getId())
                            .set(ZqztLedgerCloudConfig::getCameraFinishDate, inDate)
                            .set(ZqztLedgerCloudConfig::getUpdateTime, new Date()));
                    config.setCameraFinishDate(inDate);
                }
            }
        }

        // 2. 全流程完成判定：所有有效子表行的组网号码都已竣工 → 取最大竣工时间为全流程完成时间
        Date maxFinish = null;
        for (ZqztLedgerCloudConfig config : configs) {
            if (StringUtils.isBlank(config.getCameraAccount()) || config.getCameraFinishDate() == null) {
                return false;
            }
            if (maxFinish == null || config.getCameraFinishDate().after(maxFinish)) {
                maxFinish = config.getCameraFinishDate();
            }
        }
        if (maxFinish == null) {
            return false;
        }
        ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                .eq(ZqztLedgerInfo::getId, ledger.getId())
                // 条件更新兜住并发：只把仍是「未完成 / 派单失败」的记录推进为已完成
                .in(ZqztLedgerInfo::getStatus, STATUS_UNFINISHED, STATUS_DISPATCH_FAILED)
                .set(ZqztLedgerInfo::getFullProcessDate, maxFinish)
                .set(ZqztLedgerInfo::getStatus, STATUS_FINISHED)
                .set(ZqztLedgerInfo::getUpdateTime, new Date()));
        log.info("【火焰卫士竣工回填】置全流程完成: ledgerId={}, fullProcessDate={}", ledger.getId(), maxFinish);
        return true;
    }

    /** 调用户信息查询接口取入网时间；失败/无值返回 null（保持为空，下轮重试） */
    private Date queryInDate(String serialNumber) {
        try {
            String inDate = ledgerUserInfoService.queryUserInDate(serialNumber);
            if (StringUtils.isBlank(inDate)) {
                return null;
            }
            return new SimpleDateFormat(DATE_PATTERN).parse(inDate.trim());
        } catch (ParseException e) {
            log.warn("【火焰卫士竣工回填】入网时间格式无法解析: serialNumber={}", serialNumber);
            return null;
        } catch (Exception e) {
            log.warn("【火焰卫士竣工回填】查询入网时间失败: serialNumber={}", serialNumber, e);
            return null;
        }
    }
}
