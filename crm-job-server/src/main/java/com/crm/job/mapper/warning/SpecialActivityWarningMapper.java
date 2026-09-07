package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 特殊活动到期预警 Mapper
 * <p>
 * 对应旧系统 TcUserCommonWarningNewJob 中 warning_type='000032' 分支。
 * 数据仍取自用户级通用预警表 ftp_tc_user_common_warning，建单/派单方式迁移后
 * 统一改为用户客户级模式（参照沃TV C00024）。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface SpecialActivityWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = '000032' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectSpecialActivityList(@Param("targetDate") String targetDate);
}
