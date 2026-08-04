package com.crm.job.mapper.warning;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.BossLeaderDto;
import com.crm.system.domain.dto.OrgDepartmentDto;
import com.crm.system.domain.vo.SysAreaVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 红名单升级预警 Mapper
 * <p>
 * 对应旧系统 WarningUpJob 中的 areaService.getHmdWarnArealist、
 * dwdRepPersonService.getPersonOrgListByHmd、dwdRepPersonService.getBossList
 *
 * @author crm
 */
@Mapper
public interface HmdUpWarningMapper {

    /**
     * 查询有红名单到期升级预警数据的区域（区县级）
     * 对应旧系统 SysAreaService.getHmdWarnArealist()
     * <p>
     * 数据来源：topp_order(order_type='hmd_warn') + ftp_tc_user_cust_warning
     * + ftp_tc_user_common_warning(WARNING_TYPE='000030')
     * <p>
     * 过期判断：从 WARNING_INFO 字段中用 || 分隔符解析到期日期，
     * 到期后若订单仍为 running/tracking 或 end 但 update_time > 到期日，则为"到期未处理"
     */
    @Select({"SELECT sa.id_, sa.name_, sa.bss_code "
            + "FROM sys_area sa "
            + "JOIN ("
            + "  SELECT mm.bss_code "
            + "  FROM view_zjsj_manager mm "
            + "  LEFT JOIN ("
            + "    SELECT o.creator_ user_id "
            + "    FROM topp_order o "
            + "    JOIN ftp_tc_user_cust_warning c ON o.info_id = c.id_ "
            + "    JOIN ftp_tc_user_common_warning u ON c.MAIN_ORDER_FLOW_NO = u.MAIN_ORDER_FLOW_NO "
            + "    WHERE o.order_type = 'hmd_warn' "
            + "      AND o.order_category = 'C' "
            + "      AND u.WARNING_TYPE = '000030' "
            + "      AND DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m') = #{yyyyMM} "
            + "      AND ("
            + "        (o.status_ = 'end' AND DATE_FORMAT(o.update_time, '%Y-%m-%d') > "
            + "         DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m-%d')) "
            + "        OR (o.status_ IN ('running', 'tracking') AND DATE_FORMAT(NOW(), '%Y-%m-%d') > "
            + "            DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m-%d'))"
            + "      )"
            + "  ) ww ON mm.user_id = ww.user_id "
            + "  WHERE mm.country_name != '市本级' "
            + "    AND mm.area_name != '省本级' "
            + "    AND mm.customer_no != '' "
            + "    AND ww.user_id IS NOT NULL "
            + "  GROUP BY mm.bss_code"
            + ") a ON sa.bss_code = a.bss_code "
            + "ORDER BY sa.id_"})
    List<SysAreaVo> selectWarningAreas(@Param("yyyyMM") String yyyyMM);

    /**
     * 查询红名单升级预警市本级组织部门
     * 对应旧系统 DwdRepSysPersonDService.getPersonOrgListByHmd()
     * <p>
     * 与区县查询的区别：country_name = '市本级'，按 COUNTY_ID 分组，返回组织部门信息
     */
    @Select({"SELECT a.COUNTY_ID countyId, a.COUNTY_NAME countyName, "
            + "       a.AREA_NAME areaName "
            + "FROM dwd_rep_sys_person_d a "
            + "WHERE a.OA_USER_NAME IN ("
            + "  SELECT UPPER(mm.oa_account) "
            + "  FROM view_zjsj_manager mm "
            + "  LEFT JOIN ("
            + "    SELECT o.creator_ user_id "
            + "    FROM topp_order o "
            + "    JOIN ftp_tc_user_cust_warning c ON o.info_id = c.id_ "
            + "    JOIN ftp_tc_user_common_warning u ON c.MAIN_ORDER_FLOW_NO = u.MAIN_ORDER_FLOW_NO "
            + "    WHERE o.order_type = 'hmd_warn' "
            + "      AND o.order_category = 'C' "
            + "      AND u.WARNING_TYPE = '000030' "
            + "      AND DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m') = #{yyyyMM} "
            + "      AND ("
            + "        (o.status_ = 'end' AND DATE_FORMAT(o.update_time, '%Y-%m-%d') > "
            + "         DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m-%d')) "
            + "        OR (o.status_ IN ('running', 'tracking') AND DATE_FORMAT(NOW(), '%Y-%m-%d') > "
            + "            DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(u.WARNING_INFO, '||', 3), '||', -1), '%Y-%m-%d'))"
            + "      )"
            + "  ) ww ON mm.user_id = ww.user_id "
            + "  WHERE mm.country_name = '市本级' "
            + "    AND mm.customer_no != '' "
            + "    AND ww.user_id IS NOT NULL "
            + "  GROUP BY mm.user_id "
            + ") "
            + "GROUP BY a.COUNTY_ID"})
    List<OrgDepartmentDto> selectOrgDepartments(@Param("yyyyMM") String yyyyMM);

    /**
     * 按 code + 职位代码查询领导（区县或市本级）
     * 对应旧系统 DwdRepSysPersonDService.getBossList()
     * <p>
     * 与 WideUpWarningMapper 完全相同的查询逻辑。
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
     * <p>
     * 与 WideUpWarningMapper 完全相同的查询逻辑。
     */
    @DS("slave_2")
    @Select({"SELECT MDM_ORG_CODE_NEW AS mdmOrgCodeNew "
            + "FROM jingfen_zero_leader_org_match "
            + "WHERE MDM_ORG_CODE = #{mdmOrgCode} "
            + "LIMIT 1"})
    Map<String, Object> selectMatchOrgCode(@Param("mdmOrgCode") String mdmOrgCode);
}
