package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.BossLeaderDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 红名单到期预警(000030) 领导候选人查询 Mapper
 *
 * @author crm
 */
@Mapper
public interface HmdExpireLeaderMapper {

    /**
     * 按 code + 职位代码查询领导（区县或市本级组织部门）
     * 对应老系统 DwdRepSysPersonDService.getBossList()
     * <p>
     *
     * @param code     区县时为 BSS_CODE，市本级时为 COUNTY_ID / MDM_ORG_CODE
     * @param posCode  1=总经理，2=副总经理
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
     * 对应老系统 JingfenZeroLeaderOrgMatchService.getMatchOrgCode()
     */
    @DS("master")
    @Select({"SELECT MDM_ORG_CODE_NEW AS mdmOrgCodeNew "
            + "FROM jingfen_zero_leader_org_match "
            + "WHERE MDM_ORG_CODE = #{mdmOrgCode} "
            + "LIMIT 1"})
    Map<String, Object> selectMatchOrgCode(@Param("mdmOrgCode") String mdmOrgCode);

    /**
     * 按 OA 账号查询人员维度记录（取 COUNTY_ID）
     *
     * @param oaAccount OA 账号（大小写不敏感）
     * @return { countyId, countyName }，无记录返回 null
     */
    @DS("master")
    @Select({"SELECT a.COUNTY_ID AS countyId, a.COUNTY_NAME AS countyName "
            + "FROM dwd_rep_sys_person_d a "
            + "WHERE LOWER(a.OA_USER_NAME) = LOWER(#{oaAccount}) "
            + "LIMIT 1"})
    Map<String, Object> selectPersonCountyByOa(@Param("oaAccount") String oaAccount);
}
