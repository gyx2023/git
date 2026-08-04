package com.crm.job.mapper.warning;

import com.crm.system.domain.dto.BossLeaderDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 升级预警公共 Mapper 接口
 * <p>
 * HmdUpWarningMapper 和 WideUpWarningMapper 共享的领导查询方法。
 * WarningLeaderService 通过此接口操作不同的 Mapper 实现，消除重复代码。
 * </p>
 *
 * @author crm
 */
public interface WarningUpCommonMapper {

    /**
     * 按 code + 职位代码查询领导（区县或市本级）
     *
     * @param code    区县时为 BSS_CODE，市本级时为 MDM_ORG_CODE
     * @param posCode 1=总经理，2=副总经理
     * @param bossFlag 1=区县领导，2=市本级组织部门领导
     */
    List<BossLeaderDto> selectBossLeaders(@Param("code") String code,
                                          @Param("posCode") int posCode,
                                          @Param("bossFlag") String bossFlag);

    /**
     * 查询领导匹配表（org code 映射 fallback）
     */
    Map<String, Object> selectMatchOrgCode(@Param("mdmOrgCode") String mdmOrgCode);
}
