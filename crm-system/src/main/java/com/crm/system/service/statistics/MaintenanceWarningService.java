package com.crm.system.service.statistics;

import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dto.MaintenanceWarningReqDTO;

import java.util.List;
import java.util.Map;

/**
 * 维系预警报表服务接口
 */
public interface MaintenanceWarningService {

    /**
     * 根据Tab类型查询报表数据列表（不分页，对应老项目）
     */
    List<Map<String, Object>> selectTabList(MaintenanceWarningReqDTO reqDTO);

    /**
     * 查询Tab2客户经理预警工单处理情况（带分页）
     */
    TableDataInfo<Map<String, Object>> selectTab2Paged(MaintenanceWarningReqDTO reqDTO);

    /**
     * 获取全量导出数据
     */
    List<Map<String, Object>> selectTabExportList(MaintenanceWarningReqDTO reqDTO);

    /**
     * 获取预警类型列表
     */
    List<Map<String, Object>> getOrderTypeList();

    /**
     * 查询未派发工单清单（带分页）
     */
    TableDataInfo<Map<String, Object>> selectUnallocatedList(MaintenanceWarningReqDTO reqDTO);

    /**
     * 获取客户归属部门列表
     */
    List<Map<String, Object>> getDeptList();

    /**
     * 获取是否重保客户列表
     */
    List<Map<String, Object>> getIsImpPrtList();

    /** Tab6: 客户经理维系效果报表（分页） */
    TableDataInfo<Map<String, Object>> selectTab6Paged(MaintenanceWarningReqDTO reqDTO);

    /** 未派发工单清单导出（不分页，不脱敏） */
    List<Map<String, Object>> exportUnallocatedList(MaintenanceWarningReqDTO reqDTO);

    /**
     * 获取集团类别列表
     */
    List<Map<String, Object>> getGrpTypeList();
}
