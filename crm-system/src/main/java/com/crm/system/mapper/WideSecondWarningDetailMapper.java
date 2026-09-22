package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 宽带二次预警明细 Mapper
 * <p>
 * 查询 wide_sys_auto_warning 表（宽带到期用户明细），
 * 关联 ftp_tc_user_cycle_warning 获取用户状态。
 * 对应旧框架 SecondWarningJob / UserCustWarningAction 中宽带二次预警查询逻辑。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface WideSecondWarningDetailMapper {

    /**
     * 按主订单流水号 + 到期日期查询用户明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 宽带到期用户明细列表
     */
    List<Map<String, Object>> selectDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                                         @Param("targetDate") String targetDate);

    /**
     * 按主订单流水号查询所有用户明细（导出用，不限制 exte_flag）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 全部宽带到期用户明细
     */
    List<Map<String, Object>> selectAllDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                                           @Param("targetDate") String targetDate);


    /**
     * 统计未续约用户明细数量（exte_flag = 0）
     *
     * @param mainOrderFlowNo 主订单流水号
     * @param targetDate      到期日期，格式 yyyyMMdd
     * @return 记录数
     */
    int countDetailByOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo,
                                  @Param("targetDate") String targetDate);
}
