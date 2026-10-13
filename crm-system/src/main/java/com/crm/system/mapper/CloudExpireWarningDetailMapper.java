package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 云业务到期预警明细 Mapper
 * <p>
 * 查询 ftp_tc_grp_cloud_expire_alarm（云业务到期预警明细表，由旧系统
 * TcUserCommonWarningNewJob 从 ftp_tc_user_common_warning.warning_info 解析写入），
 * 按 main_order_flow_no 关联。
 *
 * @author crm
 */
@Mapper
public interface CloudExpireWarningDetailMapper {

    /**
     * 按主订单流水号查询云业务到期预警明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询云业务到期预警明细列表
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号
     * @return 分页明细记录
     */
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);

}
