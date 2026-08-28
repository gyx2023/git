package com.crm.system.service.impl;

import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.dto.WxCustUserListDTO;
import com.crm.system.mapper.WxCustUserListMapper;
import com.crm.system.service.IWxCustUserListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxCustUserListServiceImpl implements IWxCustUserListService {

    private final WxCustUserListMapper mapper;

    @Override
    public TableDataInfo<Map<String, Object>> queryList(Map<String, Object> params) {
        WxCustUserListDTO dto = buildDTO(params);
        int pageNum = dto.getStartRow() != null ? (dto.getStartRow() / dto.getPageSize() + 1) : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 10;
        dto.setStartRow((pageNum - 1) * pageSize);
        dto.setPageSize(pageSize);

        List<Map<String, Object>> rows = mapper.queryList(dto);
        Long total = mapper.queryCount(dto);

        TableDataInfo<Map<String, Object>> result = new TableDataInfo<>();
        result.setRows(rows != null ? rows : new ArrayList<>());
        result.setTotal(total != null ? total : 0L);
        return result;
    }

    private WxCustUserListDTO buildDTO(Map<String, Object> params) {
        WxCustUserListDTO dto = new WxCustUserListDTO();
        dto.setOrderType(getStr(params, "orderType"));
        dto.setTabType(getInt(params, "tabType"));
        dto.setSelectId(getStr(params, "selectId"));
        dto.setAreaId(getStr(params, "areaId"));
        dto.setCountryId(getStr(params, "countryId"));
        dto.setGrpCode(getStr(params, "grpCode"));
        dto.setOrderNumber(getStr(params, "orderNumber"));
        dto.setCreateTimeStart(getStr(params, "createTimeStart"));
        dto.setCreateTimeEnd(getStr(params, "createTimeEnd"));
        dto.setCustName(getStr(params, "custName"));
        dto.setBusinessDep(getStr(params, "businessDep"));
        dto.setIsImpPrt(getStr(params, "isImpPrt"));
        dto.setGrpType(getStr(params, "grpType"));
        dto.setSvcNum(getStr(params, "svcNum"));
        dto.setIsDistribute(getStr(params, "isDistribute"));
        // 客户经理过滤（预警客户列表点击到期量跳转，params 的 userId 为被筛选的客户经理）
        dto.setManagerId(getStr(params, "userId"));
        dto.setStartRow(getInt(params, "startRow"));
        dto.setPageSize(getInt(params, "pageSize"));
        if (dto.getPageSize() == null) dto.setPageSize(10);
        if (dto.getStartRow() == null) {
            Integer pn = getInt(params, "pageNum");
            dto.setStartRow(pn != null ? (pn - 1) * dto.getPageSize() : 0);
        }
        // 权限
        Long userId = LoginHelper.getUserId();
        dto.setUserId(userId);
        dto.setAuthAreaIds(queryUserAreaIds(userId));
        return dto;
    }

    /** 同老项目 PositionAuthorUtils: 查用户 org→area 权限 */
    private String queryUserAreaIds(Long userId) {
        try {
            List<String> list = mapper.queryUserAreaIds(userId);
            return list != null && !list.isEmpty() ? String.join(",", list) : "";
        } catch (Exception e) {
            log.warn("查询用户地市权限失败", e);
            return "";
        }
    }

    private String getStr(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : String.valueOf(v);
    }

    private Integer getInt(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }
}
