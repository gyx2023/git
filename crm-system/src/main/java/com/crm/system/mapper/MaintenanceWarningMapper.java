package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.MaintenanceWarningReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 维系预警报表 Mapper 接口
 */
@Mapper
@DS("master")
public interface MaintenanceWarningMapper {

    /** 预警类型下拉列表 */
    List<Map<String, Object>> selectOrderTypeList();

    /** Tab1: 已发工单按地市统计 */
    List<Map<String, Object>> selectDispatchedByCity(MaintenanceWarningReqDTO req);

    /** Tab1: 已发工单合计 */
    List<Map<String, Object>> selectDispatchedTotal(MaintenanceWarningReqDTO req);

    /** Tab2: 按客户经理统计 */
    List<Map<String, Object>> selectManagerStats(MaintenanceWarningReqDTO req);

    /** Tab2: 按客户经理统计总数 */
    Long selectManagerStatsCount(MaintenanceWarningReqDTO req);

    /** Tab5: 未派发工单按地市统计 */
    List<Map<String, Object>> selectUnallocatedByCity(MaintenanceWarningReqDTO req);

    /** Tab5: 未派发工单合计 */
    List<Map<String, Object>> selectUnallocatedTotal(MaintenanceWarningReqDTO req);

    /** 未派发工单清单列表 */
    List<Map<String, Object>> selectUnallocatedList(MaintenanceWarningReqDTO req);

    /** 未派发工单清单总数 */
    Long selectUnallocatedListCount(MaintenanceWarningReqDTO req);

    /** 客户归属部门下拉 */
    List<Map<String, Object>> selectDeptList();

    /** 是否重保客户下拉 */
    List<Map<String, Object>> selectIsImpPrtList();

    /** 集团类别下拉 */
    List<Map<String, Object>> selectGrpTypeList();

    /** Tab3: 维系效果报表-预警维度（通用动态SQL） */
    List<Map<String, Object>> selectTab3Effect(MaintenanceWarningReqDTO req);
    /** Tab3 合计 */
    List<Map<String, Object>> selectTab3EffectTotal(MaintenanceWarningReqDTO req);
    /** Tab3: 特殊预警类型 (hmd_warn/Wide/double_wide/cloud_expire_alarm) 需要 UNION ALL */
    List<Map<String, Object>> selectTab3EffectSpecial(MaintenanceWarningReqDTO req);
    /** Tab3 特殊类型合计 */
    List<Map<String, Object>> selectTab3EffectSpecialTotal(MaintenanceWarningReqDTO req);
    /** Tab4: 维系效果报表-业务维度 */
    List<Map<String, Object>> selectTab4Effect(MaintenanceWarningReqDTO req);
    /** Tab4 合计 */
    List<Map<String, Object>> selectTab4EffectTotal(MaintenanceWarningReqDTO req);
    /** Tab6: 客户经理维系效果报表 */
    List<Map<String, Object>> selectTab6Effect(MaintenanceWarningReqDTO req);
}
