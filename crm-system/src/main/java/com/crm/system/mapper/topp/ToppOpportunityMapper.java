package com.crm.system.mapper.topp;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.dto.OpportunityDTO;
import com.crm.system.domain.dto.OldOpportunityDTO;
import com.crm.system.domain.dto.SignOppoFileExportDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.OpportunityReportVO;
import com.crm.system.domain.vo.topp.ToppOpportunityExcelVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface ToppOpportunityMapper extends BaseMapperPlus<ToppOpportunityMapper, ToppOpportunityDo, ToppOpportunityVo> {
    @InterceptorIgnore(dataPermission = "true")
    List<SignOppoFileExportDTO> selectToppOpportunity();

    @Select("SELECT oppo_state_life_cycle(#{oppId})")
    String callGetOppoState(@Param("oppId") Long oppId);


    List<SignOppoFileExportDTO> selectOpportunityUnbound();

    /**
     * 执行动态SQL查询完整商机导出数据
     * @param sql 动态SQL语句
     * @return 完整导出数据列表
     */
    @Select("${sql}")
    List<OldOpportunityDTO> selectOldOppoExportData(@Param("sql") String sql);

    /**
     * 批量查询客户经理组织机构信息
     * @param userIds 客户经理用户ID列表
     * @return 包含用户ID和组织名称的结果列表
     */
    List<Map<String, Object>> selectCustManagerOrgInfoBatch(@Param("userIds") List<Long> userIds);

    /**
     * 批量查询客户经理地区信息
     * @param userIds 客户经理用户ID列表
     * @return 包含用户ID和地区信息的结果列表
     */
    List<Map<String, Object>> selectCustManagerAreaInfoBatch(@Param("userIds") List<Long> userIds);

    /**
     * 批量查询支撑经理完整信息
     * @param designers 支撑经理用户ID列表
     * @return 支撑经理完整信息列表
     */
    List<Map<String, Object>> selectSupportManagerInfoBatch(@Param("designers") List<Long> designers);


    /**
     * 批量查询创建人完整信息
     * @param creators 创建人用户 ID 列表
     * @return 创建人完整信息列表
     */
    List<Map<String, Object>> selectCreatorInfoBatch(@Param("creators") List<Long> creators);

    List<OpportunityDTO> listOpportunity(@Param("areaId") String areaId);


    /**
     * 查询商机导出数据
     *
     * @param areaId 地区ID
     * @return 商机列表
     */
    List<ToppOpportunityExcelVo> selectOpportunityExportList(@Param("areaId") Integer areaId);

    /**
     * 分页查询商机导出数据
     */
    List<ToppOpportunityExcelVo> selectOpportunityExportListPaged(@Param("areaId") Integer areaId,
                                                                  @Param("offset") int offset,
                                                                  @Param("limit") int limit);

    IPage<OpportunityReportVO> selectReportData(Page<OpportunityReportVO> page);

}
