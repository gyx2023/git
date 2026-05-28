package com.crm.system.mapper.topp;

import com.crm.common.core.domain.dto.WorkOrderDTO;
import com.crm.system.domain.topp.ToppOrder;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-18
 */
public interface ToppOrderMapper extends BaseMapperPlus<ToppOrderMapper, ToppOrder, ToppOrderVo> {

    /**
     * 查询支持订单列表（关联任务表）
     *
     * @return 支持订单列表
     */
    List<ToppOrder> selectSupportOrders();

    List<WorkOrderDTO> queryWorkOrder(
        @Param("orderType") String orderType,
        @Param("yyMMdd") String yyMMdd,
        @Param("lastId") Long  lastId,  // 游标
        @Param("size") Integer size
    );

    Integer getToppOrderCount(@Param("orderType") String orderType,
                              @Param("endTime") String endTime);

    Integer countByOrderTypeAndDate(
        @Param("orderType") String orderType,
        @Param("date") String date
    );

    /**
     * 查询闭环工单数量
     */
    Integer queryClosedOrderCount(
        @Param("orderType") String orderType,
        @Param("date") String date
    );


    List<Long> listOrderIdByCursor(
        @Param("orderType") String orderType,
        @Param("yyMMdd") String yyMMdd,
        @Param("lastId") Long lastId,
        @Param("size") int size
    );

    // ====================== 根据 ID 列表查询完整数据 ======================
    List<WorkOrderDTO> listWorkOrderByIds(
        @Param("orderType") String orderType,
        @Param("idList") List<Long> idList
    );
}
