package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 云业务到期预警 Mapper
 * <p>
 * 对应旧系统 TcUserCustWarningJobNew 中 warning_type='C00001' 分支，
 * 查询 ftp_tc_user_cust_warning 客户级预警数据。
 *
 * @author crm
 */
@Mapper
public interface CloudExpireWarningMapper {

    @Select({"SELECT w.id_, w.grp_name, w.grp_code, w.oa_id_dev, w.oa_name_dev, "
            + "w.serv_manager_id, w.warning_type, w.area_name, w.county_name, w.cust_grp_id "
            + "FROM ftp_tc_user_cust_warning w "
            + "WHERE w.warning_type = 'C00001' "
            + "AND DATE_FORMAT(w.create_date, '%Y%m%d') = #{targetDate} "
            + "ORDER BY w.id_"})
    List<Map<String, Object>> selectCloudList(@Param("targetDate") String targetDate);
}
