package com.crm.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询预警明细列表
     *
     * @param page            分页对象（MyBatis-Plus 分页插件自动追加 COUNT 和 LIMIT）
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录分页结果
     */
    Page<Map<String, Object>> selectPageByMainOrderFlowNo(Page<Map<String, Object>> page,
                                                          @Param("mainOrderFlowNo") String mainOrderFlowNo);


    /**
     * 更新单条明细的反馈状态
     *
     * @param id             明细记录ID
     * @param feedbackFlag   反馈标识：0-正常，1-不正常
     * @param feedbackRemark 反馈备注（不正常时的整改措施）
     */
    int updateFeedback(@Param("id") Long id,
                       @Param("feedbackFlag") Integer feedbackFlag,
                       @Param("feedbackRemark") String feedbackRemark);

    /**
     * 检查主订单流水号下是否存在未反馈的记录
     *
     * @param mainOrderFlowNo 主订单流水号
     * @return 未反馈记录数
     */
    int countUnfeedbackRecords(@Param("mainOrderFlowNo") String mainOrderFlowNo);
}
