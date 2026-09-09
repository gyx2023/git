package com.crm.job.service.warning.strategy;

import com.crm.job.mapper.warning.HmdUpWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningLeaderService;
import com.crm.system.service.warning.WarningRecord;
import com.crm.system.domain.dto.LeaderInfoDto;
import com.crm.system.domain.dto.OrgDepartmentDto;
import com.crm.system.domain.vo.SysAreaVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 红名单升级预警查询策略
 * <p>
 * 对应旧系统 WarningUpJob.hmdWarningUpJob → startTaskWarnUp
 * 查询有红名单到期预警数据的区县 + 市本级组织部门 → 查找各级领导 → 为每位领导生成一条 WarningRecord
 * </p>
 *
 * @author crm
 */
@Component
@RequiredArgsConstructor
public class HmdUpWarningStrategy extends AbstractWarningQueryStrategy {

    private static final String BOSS_FLAG_AREA = "1";
    private static final String BOSS_FLAG_ORG = "2";
    private static final String LEADER_TASK_KEY = "hmd_leader";

    @Resource
    private HmdUpWarningMapper mapper;

    private final WarningLeaderService leaderService;

    @Override
    public String getOrderType() {
        return "new_hmd_up";
    }

    @Override
    protected String getOrderTypeName() {
        return "红名单到期升级预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String yyyyMM) {
        List<WarningRecord> records = new ArrayList<>();
        String yyyyMMFormatted = yyyyMM.substring(0, 4) + "-" + yyyyMM.substring(4);

        // 一、区县维度
        List<SysAreaVo> areas = mapper.selectWarningAreas(yyyyMMFormatted);
        log.info("【{}】查询到预警区县: {}个", getOrderTypeName(), areas.size());

        for (SysAreaVo area : areas) {
            Long areaId = area.getId();
            String areaName = area.getName();
            String bssCode = area.getBssCode();

            List<LeaderInfoDto> areaLeaders = leaderService.findAreaLeaders(
                    mapper, bssCode, areaName, areaId, getOrderType(), LEADER_TASK_KEY);
            if (areaLeaders.isEmpty()) {
                log.warn("【{}】区县未找到领导: {}", getOrderTypeName(), areaName);
                continue;
            }
            for (LeaderInfoDto leader : areaLeaders) {
                records.add(leaderService.buildRecord(areaId, areaName, bssCode, null,
                        leader, yyyyMMFormatted, getOrderType(), getOrderTypeName(), BOSS_FLAG_AREA));
            }
        }

        // 二、市本级组织部门维度
        List<OrgDepartmentDto> orgs = mapper.selectOrgDepartments(yyyyMMFormatted);
        log.info("【{}】查询到预警市本级部门: {}个", getOrderTypeName(), orgs.size());

        for (OrgDepartmentDto org : orgs) {
            String countyId = org.getCountyId();
            String countyName = org.getCountyName();
            String areaName = org.getAreaName();

            List<LeaderInfoDto> orgLeaders = leaderService.findOrgLeaders(
                    mapper, countyId, countyName, areaName, getOrderType(), LEADER_TASK_KEY);
            if (orgLeaders.isEmpty()) {
                log.warn("【{}】市本级部门未找到领导: countyId={}, name={}", getOrderTypeName(), countyId, countyName);
                continue;
            }
            for (LeaderInfoDto leader : orgLeaders) {
                records.add(leaderService.buildRecord(Long.parseLong(countyId), countyName, null, countyId,
                        leader, yyyyMMFormatted, getOrderType(), getOrderTypeName(), BOSS_FLAG_ORG));
            }
        }

        return records;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/hmdWarningUp/deal?orderId=" + orderId;
    }
}
