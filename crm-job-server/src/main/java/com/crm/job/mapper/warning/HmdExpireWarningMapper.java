package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户红名单到期预警(000030) 建单 Mapper
 * <p>
 * 对应老框架 TcUserCustWarningNewJob 中 warning_type='000030' 的建单源，
 * 建单去重交给 WarningStartServiceImpl 的 infoId 去重，本 SQL 不判 order。
 *
 * @author crm
 */
@Mapper
public interface HmdExpireWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.area_name, w.county_name, w.cust_grp_id, w.main_order_flow_no "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = '000030' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectHmdList(@Param("targetDate") String targetDate);
}
