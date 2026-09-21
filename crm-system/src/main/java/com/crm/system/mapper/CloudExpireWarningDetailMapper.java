package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    /** 明细查询公共字段（列名与 ftp_tc_grp_cloud_expire_alarm 一致，别名对齐前端 prop） */
    String SELECT_COLUMNS = "SELECT "
            + "a.id_ AS id, "
            + "a.area_name AS areaName, "
            + "a.county_name AS countyName, "
            + "a.serial_number AS serialNumber, "
            + "a.grp_name AS grpName, "
            + "DATE_FORMAT(a.open_date, '%Y-%m-%d %H:%i:%s') AS openDate, "
            + "a.developer_name AS developerName, "
            + "a.cloud_tag AS cloudTag, "
            + "a.consume_type AS consumeType, "
            + "DATE_FORMAT(a.consume_time, '%Y-%m-%d %H:%i:%s') AS consumeTime, "
            + "DATE_FORMAT(a.end_time, '%Y-%m-%d %H:%i:%s') AS endTime, "
            + "a.server_duration AS serverDuration, "
            + "a.amount_per_year AS amountPerYear, "
            + "CASE WHEN a.new_consume_time IS NULL THEN '否' ELSE '是' END AS isRenew, "
            + "a.new_cloud_tag AS newCloudTag, "
            + "a.new_consume_type AS newConsumeType, "
            + "DATE_FORMAT(a.new_consume_time, '%Y-%m-%d %H:%i:%s') AS renewalTime, "
            + "DATE_FORMAT(a.new_end_time, '%Y-%m-%d %H:%i:%s') AS newEndTime, "
            + "a.new_server_duration AS newServerDuration, "
            + "a.new_amount_per_year AS newAmountPerYear "
            + "FROM ftp_tc_grp_cloud_expire_alarm a ";

    /**
     * 按主订单流水号查询云业务到期预警明细列表
     *
     * @param mainOrderFlowNo 主订单流水号（来自 ftp_tc_user_cust_warning.main_order_flow_no）
     * @return 明细记录列表
     */
    @Select({SELECT_COLUMNS
            + "WHERE a.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY a.id_"})
    List<Map<String, Object>> selectByMainOrderFlowNo(@Param("mainOrderFlowNo") String mainOrderFlowNo);

    /**
     * 按主订单流水号分页查询云业务到期预警明细列表
     *
     * @param page            分页参数
     * @param mainOrderFlowNo 主订单流水号
     * @return 分页明细记录
     */
    @Select({SELECT_COLUMNS
            + "WHERE a.main_order_flow_no = #{mainOrderFlowNo} "
            + "ORDER BY a.id_"})
    IPage<Map<String, Object>> selectByMainOrderFlowNoPage(Page<?> page, @Param("mainOrderFlowNo") String mainOrderFlowNo);

}
