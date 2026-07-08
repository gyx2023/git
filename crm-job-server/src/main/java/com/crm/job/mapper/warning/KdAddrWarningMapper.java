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
     * 查询待处理的宽带装机地址预警列表
     * <p>
     * 对应旧系统 TcUserCustWarningNewJob.getNotStartedTask() + startTask() 中 C00023 分支
     *
     * @param targetDate 目标日期（yyyyMMdd）
     * @return 预警记录列表
     */
    @Select({"SELECT w.id, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = 'C00023' "
            + "AND w.is_valid = '1' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id"})
    List<Map<String, Object>> selectKdAddrList(@Param("targetDate") String targetDate);

    /**
     * 按 OA 账号查询本地系统用户信息
     *
     * @param oaId OA 账号（对应 sys_user.user_name）
     * @return 用户信息（user_id, dept_id, nick_name, phonenumber）
     */
    @Select({"SELECT user_id, dept_id, nick_name, phonenumber FROM sys_user "
            + "WHERE user_name = #{oaId} AND del_flag = '0' LIMIT 1"})
    Map<String, Object> selectUserByOaId(@Param("oaId") String oaId);
}
