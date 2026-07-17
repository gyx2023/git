package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址疑似聚合或非真实预警 Mapper
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob 中 warning_type='C00023' 分支
 * 查询表：ftp_tc_user_cust_warning（由旧系统 FTP 下载任务入库）
 *
 * @author crm
 */
@Mapper
public interface KdAddrWarningMapper {

    /**
     * 查询待处理的宽带装机地址预警列表（去重：已通过 maintenance_detail 建单的记录不再生成）
     * <p>
     * 对应旧系统 TcUserCustWarningNewJob.getNotStartedTask() :
     * LEFT JOIN maintenance_detail m ON f.id_ = m.detail_id
     * AND m.order_category='C' AND m.order_type IN (...)
     * WHERE m.status IS NULL
     *
     * @param targetDate 目标日期（yyyyMMdd）
     * @return 预警记录列表
     */
    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "LEFT JOIN maintenance_detail m ON w.id_ = m.detail_id "
            + "AND m.order_category = 'C' AND m.order_type = 'kd_addr_warn' "
            + "WHERE w.warning_type = 'C00023' "
            + "AND m.status IS NULL "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectKdAddrList(@Param("targetDate") String targetDate);
}
