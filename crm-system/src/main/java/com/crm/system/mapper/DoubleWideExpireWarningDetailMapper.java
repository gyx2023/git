package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 双线到期预警明细 Mapper
 * <p>
 * 查询 wide_sys_auto_warning（双线到期预警明细表），按 main_order_flow_no 关联，
 * 用户状态取自 ftp_tc_user_cycle_warning（按 order_flow_no）。SQL 见同名 XML。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface DoubleWideExpireWarningDetailMapper {

    /**
     * 按主订单流水号查询双线到期预警明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询双线到期预警明细列表
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号
     * @return 分页明细记录
     */
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page,
                                                           @Param("mainOrderFlowNo") String mainOrderFlowNo);
}
