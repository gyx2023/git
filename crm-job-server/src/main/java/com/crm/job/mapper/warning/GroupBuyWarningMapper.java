package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 团购折扣到期预警 Mapper
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob 中 warning_type='000031' 分支。
 * 数据取自客户级预警表 ftp_tc_user_cust_warning，建单/派单方式与沃TV到期预警
 * （wotv_expire_warning）、特殊活动到期预警（special_activity_warn）一致：
 * 按 oa_id_dev 解析真实用户、创建 single_approval 流程、写 maintenance_detail +
 * maintenance_detail_new（order_category='C'）。
 * </p>
 *
 * @author crm
 */
@Mapper
public interface GroupBuyWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = '000031' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectGroupBuyList(@Param("targetDate") String targetDate);
}
