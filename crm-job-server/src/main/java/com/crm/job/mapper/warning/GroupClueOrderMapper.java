package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 线索-工单关联表 Mapper（表 groupclue_order）
 * <p>
 * 迁移自旧系统 {@code com.hollycrm.service.order.GroupClueOrderService}，
 * 对应旧系统 {@code CluesEarlyWarning.startProcessAndSaveOrder()} 中保存 GroupClueOrder 的步骤；
 * 待办/详情页面依据该关联用流程订单ID反查营销线索详情。
 *
 * @author crm
 */
@Mapper
public interface GroupClueOrderMapper {

    /**
     * 保存流程工单与营销线索的关联关系
     *
     * @param orderId    新框架工单ID（topp_order.id_）
     * @param clueId     线索ID（dispatch_order.root_order_id）
     * @param areaId     地市ID
     * @param industryId 行业ID
     * @param orderType  工单类型
     */
    @Insert("insert into groupclue_order(order_id, clue_id, area_id, industry_id, create_time, order_type) "
            + "values(#{orderId}, #{clueId}, #{areaId}, #{industryId}, now(), #{orderType})")
    int insertLink(@Param("orderId") Long orderId,
                   @Param("clueId") Long clueId,
                   @Param("areaId") Long areaId,
                   @Param("industryId") Long industryId,
                   @Param("orderType") String orderType);
}
