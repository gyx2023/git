package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 特殊活动到期预警明细 Mapper。
 * <p>
 * 与沃TV同构：客户级建单主表 ftp_tc_user_cust_warning(warning_type=000032) 的 main_order_flow_no，
 * 对应用户级通用预警表 ftp_tc_user_common_warning(warning_type=000032) 同主订单流水的多条用户行。
 * 明细链：工单 info_id(客户级行id) → main_order_flow_no → ftp_tc_user_common_warning 多条。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface SpecialActivityWarningDetailMapper {


    /**
     * 历史数据兜底：按主键回查用户级预警表 ftp_tc_user_common_warning 的 main_order_flow_no。
     *
     * @param id 用户级预警行主键（历史工单 info_id）
     * @return main_order_flow_no，无则 null
     */
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 按主订单流水号查询同一批用户级明细（多条）
     */
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询同一批用户级明细（多条）
     */
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);
}
