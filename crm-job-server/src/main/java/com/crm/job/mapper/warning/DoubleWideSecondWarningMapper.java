package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 双线二次预警 Mapper
 * <p>
 * 对应旧系统 SecondWarningJob.doubleWideSecondJob → getSecondWarnTask(date, "double_wide")：
 * 查询 ftp_tc_user_cust_warning（warning_type in ('C00007','C00008')，双线），
 * 通过子查询筛选 15 天后到期且未续约（exte_flag=0）的用户级预警，按 MAIN_ORDER_FLOW_NO 去重。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface DoubleWideSecondWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.serv_manager_id, w.warning_type "
            + "FROM ftp_tc_user_cust_warning w "
            + "JOIN ("
            + "  SELECT DISTINCT ftucw.MAIN_ORDER_FLOW_NO "
            + "  FROM ftp_tc_user_common_warning ftucw "
            + "  JOIN wide_sys_auto_warning wsaw ON ftucw.id_ = wsaw.u_id "
            + "  WHERE ftucw.WARNING_TYPE in ('C00007', 'C00008') "
            + "    AND wsaw.id IS NOT NULL "
            + "    AND wsaw.exte_flag = 0 "
            + "    AND DATE_FORMAT(wsaw.exp_time, '%Y%m%d') = #{targetDate} "
            + ") b ON w.MAIN_ORDER_FLOW_NO = b.MAIN_ORDER_FLOW_NO "
            + "WHERE w.WARNING_TYPE in ('C00007', 'C00008') "
            + "GROUP BY w.MAIN_ORDER_FLOW_NO "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectSecondWarnList(@Param("targetDate") String targetDate);
}
