package com.crm.job.mapper.warning;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.BossLeaderDto;
import com.crm.system.domain.dto.LeaderDto;
import com.crm.system.domain.dto.OrgDepartmentDto;
import com.crm.system.domain.vo.SysAreaVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带升级预警 Mapper
 * <p>
 * 对应旧系统 WarningUpJob 中的 areaService.getWideWarningArealist、
 * dwdRepPersonService.getPersonOrgList、dwdRepPersonService.getBossList
 */
@Mapper
public interface WideUpWarningMapper extends WarningUpCommonMapper {

    /**
     * 查询有宽带到期预警数据的区域（区县级）
     * 对应旧系统 SysAreaService.getWideWarningArealist()
     */
    @Select({"SELECT sa.id_, sa.name_, sa.bss_code "
            + "FROM view_zjsj_manager mm "
            + "JOIN sys_area sa ON mm.country_name = sa.name_ "
            + "JOIN ("
            + "  SELECT m.user_id, w.exp_time "
            + "  FROM maintenance_detail m "
            + "  LEFT JOIN topp_order t ON m.order_id = t.id_ "
            + "  LEFT JOIN ftp_tc_user_cust_warning c ON m.detail_id = c.id_ "
            + "  LEFT JOIN wide_sys_auto_warning w ON c.MAIN_ORDER_FLOW_NO = w.main_order_flow_no "
            + "  WHERE m.STATUS = 1 AND m.order_type = 'Wide' "
            + "    AND w.exp_time LIKE CONCAT(#{yyyyMM}, '%') AND m.order_category = 'C' "
            + ") ww ON mm.user_id = ww.user_id "
            + "WHERE mm.area_name != '省本级' AND mm.country_name != '市本级' AND mm.customer_no != '' "
            + "GROUP BY mm.country_name "
            + "ORDER BY sa.id_"})
    List<SysAreaVo> selectWarningAreas(@Param("yyyyMM") String yyyyMM);

    /**
     * 查询市本级组织部门（有预警数据的）
     * 对应旧系统 DwdRepSysPersonDService.getPersonOrgList()
     */
    @Select({"SELECT a.COUNTY_ID countyId, a.COUNTY_NAME countyName, "
            + "       a.AREA_NAME areaName "
            + "FROM dwd_rep_sys_person_d a "
            + "LEFT JOIN dwd_rep_gw_pst_d b ON a.EMP_ID = b.EMP_ID "
            + "WHERE a.OA_USER_NAME IN ("
            + "  SELECT UPPER(mm.oa_account) FROM view_zjsj_manager mm "
            + "  LEFT JOIN ("
            + "    SELECT m.user_id, w.exp_time "
            + "    FROM maintenance_detail m "
            + "    LEFT JOIN topp_order t ON m.order_id = t.id_ "
            + "    LEFT JOIN wide_sys_auto_warning w ON m.detail_id = w.id "
            + "    WHERE m.STATUS = 1 AND m.order_type = 'Wide' "
            + "      AND w.exp_time LIKE CONCAT(#{yyyyMM}, '%') AND m.order_category = 'U' "
            + "    UNION "
            + "    SELECT m.user_id, w.exp_time "
            + "    FROM maintenance_detail m "
            + "    LEFT JOIN topp_order t ON m.order_id = t.id_ "
            + "    LEFT JOIN ftp_tc_user_cust_warning c ON m.detail_id = c.id_ "
            + "    LEFT JOIN wide_sys_auto_warning w ON c.MAIN_ORDER_FLOW_NO = w.main_order_flow_no "
            + "    WHERE m.STATUS = 1 AND m.order_type = 'Wide' "
            + "      AND w.exp_time LIKE CONCAT(#{yyyyMM}, '%') AND m.order_category = 'C' "
            + "  ) ww ON mm.user_id = ww.user_id "
            + "  WHERE mm.country_name = '市本级' AND mm.customer_no != '' "
            + "    AND ww.exp_time LIKE CONCAT(#{yyyyMM}, '%') "
            + "  GROUP BY mm.user_id "
            + ") "
            + "GROUP BY a.COUNTY_ID"})
    List<OrgDepartmentDto> selectOrgDepartments(@Param("yyyyMM") String yyyyMM);

    /**
     * 按 code + 职位代码查询领导（区县或市本级）
     * 对应旧系统 DwdRepSysPersonDService.getBossList()
     *
     * @param code    区县时为 BSS_CODE，市本级时为 MDM_ORG_CODE
     * @param posCode 1=总经理，2=副总经理
     * @param bossFlag 1=区县领导，2=市本级组织部门领导
     */
    @DS("master")
    @Select({"<script>"
            + "<choose>"
            + "  <when test='bossFlag == \"1\"'>"
            + "    SELECT b.EMP_ID, b.OA_USER_NAME, b.FULL_NAME, b.AREA_NAME, b.COUNTY_NAME "
            + "    FROM dwd_rep_gw_pst_d a "
            + "    LEFT JOIN dwd_rep_sys_person_d b ON a.EMP_ID = b.EMP_ID "
            + "    WHERE a.MDM_ORG_CODE = (SELECT EMP_COUNTY_ID FROM dim_county_id WHERE COUNTY_ID = #{code}) "
            + "      AND a.POS_CODE = #{posCode}"
            + "  </when>"
            + "  <when test='bossFlag == \"2\"'>"
            + "    SELECT b.EMP_ID, b.OA_USER_NAME, b.FULL_NAME, b.AREA_NAME, b.COUNTY_NAME "
            + "    FROM dwd_rep_gw_pst_d a "
            + "    LEFT JOIN dwd_rep_sys_person_d b ON a.EMP_ID = b.EMP_ID "
            + "    WHERE a.MDM_ORG_CODE = #{code} AND a.POS_CODE = #{posCode}"
            + "  </when>"
            + "</choose>"
            + "</script>"})
    List<BossLeaderDto> selectBossLeaders(@Param("code") String code,
                                          @Param("posCode") int posCode,
                                          @Param("bossFlag") String bossFlag);

    /**
     * 查询领导匹配表（org code 映射 fallback）
     * 对应旧系统 JingfenZeroLeaderOrgMatchService.getMatchOrgCode()
     */
    @DS("slave_2")
    @Select({"SELECT MDM_ORG_CODE_NEW AS mdmOrgCodeNew "
            + "FROM jingfen_zero_leader_org_match "
            + "WHERE MDM_ORG_CODE = #{mdmOrgCode} "
            + "LIMIT 1"})
    Map<String, Object> selectMatchOrgCode(@Param("mdmOrgCode") String mdmOrgCode);

    /**
     * 查询所有有 bss_code 的区域（fallback 用）
     */
    @Select({"SELECT DISTINCT a.id_, a.name_, a.bss_code "
            + "FROM sys_area a "
            + "WHERE a.bss_code IS NOT NULL "
            + "ORDER BY a.id_"})
    List<SysAreaVo> selectAllAreas();

    /**
     * 查询所有 boss/vice_boss 角色用户（兜底用，地市接口人 fallback）
     */
    @Select({"SELECT DISTINCT u.user_id, u.dept_id, u.nick_name, u.phonenumber "
            + "FROM sys_user u "
            + "JOIN sys_user_role ur ON u.user_id = ur.user_id "
            + "JOIN sys_role r ON ur.role_id = r.role_id "
            + "WHERE r.role_key IN ('boss', 'vice_boss') AND u.del_flag = '0'"})
    List<LeaderDto> selectLeaders();
}
