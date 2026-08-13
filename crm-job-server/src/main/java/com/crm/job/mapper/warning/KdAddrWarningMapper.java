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
     * 查询待处理的宽带装机地址预警列表（不做 maintenance_detail 去重，对齐旧系统行为）。
     * <p>
     * 旧系统 TcUserCustWarningNewJob.getNotStartedTask() 的 LEFT JOIN 中
     * getOrderType() 返回的类型列表不包含 kd_addr_warn，因此旧系统对该类型不做去重。
     *
     * @param targetDate 目标日期（yyyyMMdd）
     * @return 预警记录列表
     */
    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = 'C00023' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectKdAddrList(@Param("targetDate") String targetDate);
}
