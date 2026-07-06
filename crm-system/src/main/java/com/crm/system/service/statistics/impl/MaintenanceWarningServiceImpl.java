package com.crm.system.service.statistics.impl;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dto.MaintenanceWarningReqDTO;
import com.crm.system.mapper.MaintenanceWarningMapper;
import com.crm.system.service.statistics.MaintenanceWarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 维系预警报表服务实现
 * 全部 Tab 统一使用 MyBatis Mapper XML 动态 SQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceWarningServiceImpl implements MaintenanceWarningService {

    @Resource
    private MaintenanceWarningMapper maintenanceWarningMapper;

    @Override
    public List<Map<String, Object>> selectTabList(MaintenanceWarningReqDTO req) {
        Integer tabType = req.getTabType();
        if (tabType == null) {
            tabType = 5;
        }

        switch (tabType) {
            case 1: return queryTab1(req);
            case 2: return queryTab2(req);
            case 3: return queryTab3(req);
            case 4: return queryTab4(req);
            case 5: return queryTab5(req);
            case 6: return queryTab6(req);
            default: return new ArrayList<>();
        }
    }

    @Override
    public TableDataInfo<Map<String, Object>> selectTab2Paged(MaintenanceWarningReqDTO reqDTO) {
        reqDTO.setTabType(2);
        // 计算分页起始行
        int pageNum = reqDTO.getPageNum() != null ? reqDTO.getPageNum() : 1;
        int pageSize = reqDTO.getPageSize() != null ? reqDTO.getPageSize() : 10;
        reqDTO.setStartRow((pageNum - 1) * pageSize);
        reqDTO.setPageSize(pageSize);

        List<Map<String, Object>> list = maintenanceWarningMapper.selectManagerStats(reqDTO);
        Long total = maintenanceWarningMapper.selectManagerStatsCount(reqDTO);
        TableDataInfo<Map<String, Object>> rsp = new TableDataInfo<>();
        rsp.setRows(ObjectUtil.isEmpty(list) ? new ArrayList<>() : list);
        rsp.setTotal(total != null ? total : 0);
        return rsp;
    }

    @Override
    public List<Map<String, Object>> selectTabExportList(MaintenanceWarningReqDTO reqDTO) {
        Integer tabType = reqDTO.getTabType();
        if (tabType == null) {
            tabType = 5;
        }
        // 导出不分页，全量返回
        switch (tabType) {
            case 1: return queryTab1(reqDTO);
            case 2: return queryTab2(reqDTO);
            case 3: return queryTab3(reqDTO);
            case 4: return queryTab4(reqDTO);
            case 5: return queryTab5(reqDTO);
            case 6: return queryTab6(reqDTO);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getOrderTypeList() {
        return maintenanceWarningMapper.selectOrderTypeList();
    }

    @Override
    public TableDataInfo<Map<String, Object>> selectUnallocatedList(MaintenanceWarningReqDTO req) {
        // 计算分页起始行
        int pageNum = req.getPageNum() != null ? req.getPageNum() : 1;
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 10;
        req.setStartRow((pageNum - 1) * pageSize);
        req.setPageSize(pageSize);

        List<Map<String, Object>> list = maintenanceWarningMapper.selectUnallocatedList(req);
        // 脱敏：客户名称(grpName) 和 用户号码(svcNum)
        maskList(list);
        Long total = maintenanceWarningMapper.selectUnallocatedListCount(req);
        TableDataInfo<Map<String, Object>> rsp = new TableDataInfo<>();
        rsp.setRows(ObjectUtil.isEmpty(list) ? new ArrayList<>() : list);
        rsp.setTotal(total != null ? total : 0);
        return rsp;
    }

    /**
     * 对清单数据做脱敏：客户名称(前3后3)、用户号码(前3后3)
     * 与老项目 dmask:dataMask type=cname / accNbr 行为一致
     */
    private void maskList(List<Map<String, Object>> list) {
        if (ObjectUtil.isEmpty(list)) return;
        for (Map<String, Object> row : list) {
            Object grpName = row.get("grpName");
            if (grpName != null) row.put("grpName", strMask(grpName.toString(), 3, 3));
            Object svcNum = row.get("svcNum");
            if (svcNum != null) row.put("svcNum", strMask(svcNum.toString(), 3, 3));
        }
    }

    private String strMask(String str, int pre, int suf) {
        if (str == null || str.isEmpty()) return str;
        int len = str.length();
        if (len <= pre + suf) {
            // 太短则全掩
            return repeat('*', len);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str, 0, pre);
        for (int i = pre; i < len - suf; i++) sb.append('*');
        sb.append(str, len - suf, len);
        return sb.toString();
    }

    private String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }

    @Override
    public List<Map<String, Object>> exportUnallocatedList(MaintenanceWarningReqDTO req) {
        req.setStartRow(null);
        req.setPageSize(null);
        return maintenanceWarningMapper.selectUnallocatedList(req);
    }

    @Override
    public List<Map<String, Object>> getDeptList() {
        return maintenanceWarningMapper.selectDeptList();
    }

    @Override
    public List<Map<String, Object>> getIsImpPrtList() {
        return maintenanceWarningMapper.selectIsImpPrtList();
    }

    @Override
    public List<Map<String, Object>> getGrpTypeList() {
        return maintenanceWarningMapper.selectGrpTypeList();
    }

    // ==================== Tab1: maintenance_detail_new (对应 getMaintenanceNewStatSql + getMaintenanceTotalNewSql) ====================

    private List<Map<String, Object>> queryTab1(MaintenanceWarningReqDTO req) {
        List<Map<String, Object>> list = maintenanceWarningMapper.selectDispatchedByCity(req);
        if (ObjectUtil.isNotEmpty(list) && list.size() > 1) {
            List<Map<String, Object>> total = maintenanceWarningMapper.selectDispatchedTotal(req);
            if (ObjectUtil.isNotEmpty(total)) {
                list.addAll(total);
            }
        }
        return list;
    }

    // ==================== Tab2: maintenance_detail_new (对应 getMaintenanceManagerNewStatSql) ====================

    private List<Map<String, Object>> queryTab2(MaintenanceWarningReqDTO req) {
        return maintenanceWarningMapper.selectManagerStats(req);
    }

    // ==================== Tab5: maintenance_detail (对应 getMaintenanceStatSql + getMaintenanceTotalSql) ====================

    private List<Map<String, Object>> queryTab5(MaintenanceWarningReqDTO req) {
        List<Map<String, Object>> list = maintenanceWarningMapper.selectUnallocatedByCity(req);
        if (ObjectUtil.isNotEmpty(list) && list.size() > 1) {
            List<Map<String, Object>> total = maintenanceWarningMapper.selectUnallocatedTotal(req);
            if (ObjectUtil.isNotEmpty(total)) {
                list.addAll(total);
            }
        }
        return list;
    }

    // ==================== Tab3: 维系效果报表-预警维度 ====================

    /** 需要 UNION ALL 处理两类订单的特殊预警类型 */
    private static final java.util.Set<String> TAB3_SPECIAL_ORDER_TYPES =
        java.util.Collections.unmodifiableSet(new java.util.HashSet<>(java.util.Arrays.asList(
            "hmd_warn", "Wide", "double_wide", "cloud_expire_alarm"
        )));

    private List<Map<String, Object>> queryTab3(MaintenanceWarningReqDTO req) {
        if (req.getOrderType() == null || req.getOrderType().isEmpty()) {
            return new ArrayList<>();
        }
        boolean isSpecial = TAB3_SPECIAL_ORDER_TYPES.contains(req.getOrderType());
        List<Map<String, Object>> list = isSpecial
            ? maintenanceWarningMapper.selectTab3EffectSpecial(req)
            : maintenanceWarningMapper.selectTab3Effect(req);
        if (ObjectUtil.isNotEmpty(list) && list.size() > 1) {
            List<Map<String, Object>> totalList = isSpecial
                ? maintenanceWarningMapper.selectTab3EffectSpecialTotal(req)
                : maintenanceWarningMapper.selectTab3EffectTotal(req);
            if (ObjectUtil.isNotEmpty(totalList)) {
                list.addAll(totalList);
            }
        }
        return list;
    }

    // ==================== Tab4: 维系效果报表-业务维度 ====================

    private List<Map<String, Object>> queryTab4(MaintenanceWarningReqDTO req) {
        if (req.getOrderType() == null || req.getOrderType().isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = maintenanceWarningMapper.selectTab4Effect(req);
        if (ObjectUtil.isNotEmpty(list) && list.size() > 1) {
            List<Map<String, Object>> totalList = maintenanceWarningMapper.selectTab4EffectTotal(req);
            if (ObjectUtil.isNotEmpty(totalList)) {
                list.addAll(totalList);
            }
        }
        return list;
    }

    // ==================== Tab6: 客户经理维系效果报表 ====================

    private List<Map<String, Object>> queryTab6(MaintenanceWarningReqDTO req) {
        if (req.getOrderType() == null || req.getOrderType().isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = maintenanceWarningMapper.selectTab6Effect(req);
        return addRank(list);
    }

    /** Tab6 分页查询 */
    public TableDataInfo<Map<String, Object>> selectTab6Paged(MaintenanceWarningReqDTO req) {
        if (req.getOrderType() == null || req.getOrderType().isEmpty()) {
            TableDataInfo<Map<String, Object>> rsp = new TableDataInfo<>();
            rsp.setRows(new ArrayList<>());
            rsp.setTotal(0L);
            return rsp;
        }
        List<Map<String, Object>> fullList = maintenanceWarningMapper.selectTab6Effect(req);
        addRank(fullList);
        int total = fullList.size();
        int pageNum = req.getPageNum() != null ? req.getPageNum() : 1;
        int pageSize = req.getPageSize() != null ? req.getPageSize() : 10;
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Map<String, Object>> pageList = start < total ? fullList.subList(start, end) : new ArrayList<>();
        TableDataInfo<Map<String, Object>> rsp = new TableDataInfo<>();
        rsp.setRows(pageList);
        rsp.setTotal((long) total);
        return rsp;
    }

    /** 计算排名 */
    private List<Map<String, Object>> addRank(List<Map<String, Object>> list) {
        int rowNum = 0;
        int rank = 0;
        String prevRate = null;
        for (Map<String, Object> row : list) {
            rowNum++;
            String curRate = (String) row.getOrDefault("xyRate", row.get("onlineRate"));
            if (curRate == null) curRate = "";
            if (!curRate.equals(prevRate)) {
                rank = rowNum;
                prevRate = curRate;
            }
            row.put("rank", rank);
        }
        return list;
    }
}
