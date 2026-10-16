package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 营销线索预警 Mapper
 * <p>
 * 迁移自旧系统 {@code com.hollycrm.job.CluesEarlyWarning} →
 * {@code com.hollycrm.service.business.GroupClueService} 的 7 段查询。
 * SQL 与旧系统保持一致，查询的表（dispatch_order / dispatch_task /
 * dispatch_order_business_data / view_user / view_clue_order /
 * industry_area_user / dispatch_visit）在迁移期由新旧框架共用，故未做改写。
 * <p>
 * 参数说明（对齐旧系统 jobParam 格式：{@code 地市;开始时间,结束时间}）：
 * <ul>
 *     <li>areaParam：地市编码列表，如 {@code 330100,330200}，为空表示不限制</li>
 *     <li>dateParam1 / dateParam2：业务数据时间范围（yyyy-MM-dd HH:mm:ss），可为空</li>
 * </ul>
 *
 * @author crm
 */
@Mapper
public interface CluesEarlyWarningMapper {

    /**
     * 1、专家评审超时：线索下发 24 小时内未完成评审
     * <p>返回列：userId、content、clueNum（线索编号，用作去重键）</p>
     */
    List<Map<String, Object>> professorTimeout(@Param("areaParam") String areaParam,
                                               @Param("dateParam1") String dateParam1,
                                               @Param("dateParam2") String dateParam2);

    /**
     * 2、领导审核超时：专家评审通过后 24 小时内未完成审核
     * <p>返回列：userId、content、clueNum</p>
     */
    List<Map<String, Object>> leaderTimeout(@Param("areaParam") String areaParam,
                                            @Param("dateParam1") String dateParam1,
                                            @Param("dateParam2") String dateParam2);

    /**
     * 3、未细化分配：领导审核通过 3 天内未完成分配
     * <p>返回列：userId、content、clueNum、areaId、industryId</p>
     */
    List<Map<String, Object>> noRefine(@Param("areaParam") String areaParam,
                                       @Param("dateParam1") String dateParam1,
                                       @Param("dateParam2") String dateParam2);

    /**
     * 4、走访不合格：客户经理接单后 7 天内目标客户走访率低于 100%
     * <p>返回列：userId、content</p>
     */
    List<Map<String, Object>> visitUnQualified(@Param("areaParam") String areaParam,
                                               @Param("dateParam1") String dateParam1,
                                               @Param("dateParam2") String dateParam2);

    /**
     * 5、线索未关单：客户名单分配完成 60 天内线索关单率低于 100%
     * <p>返回列：userId、content</p>
     */
    List<Map<String, Object>> custUnShutdown(@Param("areaParam") String areaParam,
                                             @Param("dateParam1") String dateParam1,
                                             @Param("dateParam2") String dateParam2);

    /**
     * 6、走访不合格（行业部领导 / 县分分管领导）——生成待办
     * <p>返回列：userId、content、areaId、industryId、clueId</p>
     */
    List<Map<String, Object>> clientVisitRate(@Param("areaParam") String areaParam,
                                              @Param("dateParam1") String dateParam1,
                                              @Param("dateParam2") String dateParam2);

    /**
     * 7、线索未关单（行业部领导 / 县分分管领导）——生成待办
     * <p>返回列：userId、content、areaId、industryId、clueId</p>
     */
    List<Map<String, Object>> custUnShutdown1(@Param("areaParam") String areaParam,
                                              @Param("dateParam1") String dateParam1,
                                              @Param("dateParam2") String dateParam2);
}
