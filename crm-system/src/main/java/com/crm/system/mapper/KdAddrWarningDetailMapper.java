package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址预警明细 Mapper
 * <p>
 * 查询 ftp_tc_user_common_warning 表（由旧系统 FTP 下载任务入库的明细记录），
 * 提供反馈更新能力。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface KdAddrWarningDetailMapper {

    /**
     * 按主订单流水号查询预警明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    @Select({"SELECT id_, svc_num, cust_name, prod_id, prod_name, open_date, user_status, "
            + "warning_info, feedback_flag, feedback_remark "
            + "FROM ftp_tc_user_common_warning "
            + "WHERE main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY id_"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号查询主记录获取 main_order_flow_no
     *
     * @param id ftp_tc_user_cust_warning 主键
     * @return main_order_flow_no
     */
    @Select({"SELECT main_order_flow_no FROM ftp_tc_user_cust_warning WHERE id_ = #{id}"})
    String selectMainOrderFlowNoById(@Param("id") Long id);

    /**
     * 更新单条明细的反馈状态
     *
     * @param id             明细记录ID
     * @param feedbackFlag   反馈标识：0-正常，1-不正常
     * @param feedbackRemark 反馈备注（不正常时的整改措施）
     */
    @Update({"UPDATE ftp_tc_user_common_warning SET feedback_flag = #{feedbackFlag}, "
            + "feedback_remark = #{feedbackRemark} WHERE id_ = #{id}"})
    int updateFeedback(@Param("id") Long id,
                       @Param("feedbackFlag") Integer feedbackFlag,
                       @Param("feedbackRemark") String feedbackRemark);

    /**
     * 检查主订单流水号下是否存在未反馈的记录
     *
     * @param mainOrderFlowNo 主订单流水号
     * @return 未反馈记录数
     */
    @Select({"SELECT COUNT(1) FROM ftp_tc_user_common_warning "
            + "WHERE main_order_flow_no = #{mainOrderFlowNo} "
            + "AND (feedback_flag IS NULL OR feedback_flag = '')"})
    int countUnfeedbackRecords(@Param("mainOrderFlowNo") String mainOrderFlowNo);
}
