package com.crm.system.service.stats.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.service.stats.IStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类（升级预警专用）
 * <p>
 * 迁移自老系统 StatsService，提供宽带升级预警和红名单升级预警的统计查询功能
 * </p>
 *
 * @author crm
 * @date 2026-06-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements IStatsService {

    private final JdbcTemplate jdbcTemplate;
    private final SysAreaMapper sysAreaMapper;
    private final DwdRepSysPersonDMapper dwdRepSysPersonDMapper;

    @Override
    public String buildWideCustInfoStatsSql(String codeInfo, String period, String orderFlag) {
        StringBuilder sqlBody = new StringBuilder();
        String sqlStart = "";
        String sqlEnd = "";
        String sqlCommon = "SELECT m.user_id,w.exte_flag,w.exp_time,w.id" +
                " FROM maintenance_detail m LEFT JOIN topp_order t ON m.order_id = t.id_ LEFT JOIN" +
                " wide_sys_auto_warning w ON m.detail_id = w.id WHERE m.STATUS = 1 AND m.order_type = 'Wide' AND" +
                " w.exp_time LIKE '" + period + "%' AND m.order_category = 'U' UNION SELECT m.user_id,w.exte_flag,w.exp_time,w.id" +
                " FROM maintenance_detail m LEFT JOIN topp_order t ON" +
                " m.order_id = t.id_ LEFT JOIN ftp_tc_user_cust_warning c ON m.detail_id = c.id_ LEFT JOIN" +
                " wide_sys_auto_warning w ON c.MAIN_ORDER_FLOW_NO = w.main_order_flow_no WHERE m.STATUS = 1 AND" +
                " m.order_type = 'Wide' AND w.exp_time LIKE '" + period + "%' AND m.order_category = 'C'";

        if (StringUtils.isNotBlank(codeInfo) && StringUtils.isNotBlank(period) && StringUtils.isNotBlank(orderFlag)) {
            if ("1".equals(orderFlag)) {
                // 区县
                sqlStart = "SELECT tt.city,tt.country,tt.name_ch,tt.name_,tt.phone,tt.yjl,tt.xyl," +
                        "CONCAT(CAST(IF(ROUND(tt.xyl/tt.yjl*100,2) IS NULL,0,ROUND(tt.xyl/tt.yjl*100,2)) AS CHAR),'%') AS xyll," +
                        "(tt.xyl/tt.yjl) AS xyl2," +
                        "tt.user_id AS userId " +
                        "FROM (SELECT p.name_ AS city,a.name_ AS country,u.name_ch,u.name_,u.relation_tel AS phone,mm.user_id," +
                        "COUNT(1) yjl,COUNT(IF(mm.exte_flag=1,TRUE,NULL)) AS xyl FROM (";

                sqlEnd = ") mm " +
                        "LEFT JOIN sys_user u ON mm.user_id=u.id_ " +
                        "LEFT JOIN sys_org o ON u.org_id=o.id_ " +
                        "LEFT JOIN sys_area a ON a.id_=o.area_id " +
                        "LEFT JOIN sys_area p ON a.parent_=p.id_ " +
                        "WHERE a.bss_code='" + codeInfo + "' " +
                        "GROUP BY mm.user_id,p.name_,a.name_,u.name_ch,u.name_) tt " +
                        "ORDER BY xyl2 ASC";

            } else if ("2".equals(orderFlag)) {
                // 市本级下的组织部门
                sqlStart = "SELECT tt.city,tt.country,tt.name_ch,tt.name_,tt.phone,tt.yjl,tt.xyl," +
                        "CONCAT(CAST(IF(ROUND(tt.xyl/tt.yjl*100,2) IS NULL,0,ROUND(tt.xyl/tt.yjl*100,2)) AS CHAR),'%') AS xyll," +
                        "(tt.xyl/tt.yjl) AS xyl2," +
                        "tt.user_id AS userId " +
                        "FROM (SELECT jj.area_name AS city,jj.COUNTY_NAME AS country,mm.name_ch,mm.name_,IFNULL(u.relation_tel,'') AS phone,mm.user_id," +
                        "COUNT(1) AS yjl,COUNT(IF(ww.exte_flag=1,TRUE,NULL)) AS xyl FROM view_zjsj_manager mm LEFT JOIN sys_user u ON mm.user_id=u.id_ LEFT JOIN (";

                sqlEnd = ") ww ON mm.user_id=ww.user_id " +
                        "LEFT JOIN view_jingfen_person jj ON UPPER(mm.oa_account)=jj.OA_USER_NAME " +
                        "WHERE mm.country_name='市本级' AND mm.customer_no!='' AND " +
                        "ww.exp_time LIKE '" + period + "%' AND mm.oa_account IN (" +
                        "SELECT LOWER(OA_USER_NAME) FROM view_jingfen_person WHERE COUNTY_ID='" + codeInfo + "'" +
                        ") GROUP BY mm.user_id,jj.area_name,jj.COUNTY_NAME,mm.name_ch,mm.name_) tt " +
                        "ORDER BY xyl2 ASC";
            }
        }
        sqlBody.append(sqlStart).append(sqlCommon).append(sqlEnd);
        log.debug("【宽带升级预警SQL】{}", sqlBody);
        return sqlBody.toString();
    }

    @Override
    public IPage<Map<String, Object>> queryWideCustInfoStats(String sql, Integer pageNum, Integer pageSize) {
        if (StringUtils.isBlank(sql)) {
            return new Page<>();
        }

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS total";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null || total == 0) {
            return new Page<>();
        }

        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        String pageSql = sql + " LIMIT " + offset + ", " + pageSize;
        List<Map<String, Object>> records = jdbcTemplate.queryForList(pageSql);

        // 封装分页结果
        IPage<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Override
    public Map<String, Object> getWideUpSpeechcraftStats(String codeInfo, String period, String orderFlag) {
        Map<String, Object> speechcraftMap = new HashMap<>();

        if (StringUtils.isBlank(codeInfo) || StringUtils.isBlank(period) || StringUtils.isBlank(orderFlag)) {
            return speechcraftMap;
        }

        // 查询客户列表统计数据
        String sql = buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

        if (list.isEmpty()) {
            return speechcraftMap;
        }

        // 获取区域名称
        String codeName = getCodeName(codeInfo, orderFlag);
        speechcraftMap.put("codeName", codeName);

        // 计算统计数据
        int expPersonNum = 0;   // 到期用户量
        int continuePersonNum = 0;  // 续约用户量
        int underRatePersonNum = 0;     // 低于规定率的客户经理人数

        for (Map<String, Object> obj : list) {
            int yjl = obj.get("yjl") != null ? Integer.parseInt(obj.get("yjl").toString()) : 0;
            int xyl = obj.get("xyl") != null ? Integer.parseInt(obj.get("xyl").toString()) : 0;
            BigDecimal xyl2 = obj.get("xyl2") != null ? new BigDecimal(obj.get("xyl2").toString()) : BigDecimal.ZERO;

            expPersonNum += yjl;
            continuePersonNum += xyl;

            BigDecimal rate = new BigDecimal("0.5");
            if (xyl2.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(xyl2) > 0) {
                underRatePersonNum++;
            }
        }

        // 计算续约率
        String expRate;
        if (expPersonNum == 0) {
            expRate = "0.00%";
        } else {
            double sum = (double) continuePersonNum / expPersonNum;
            NumberFormat nt = NumberFormat.getPercentInstance();
            nt.setMinimumFractionDigits(2);
            expRate = nt.format(sum);
        }

        // 获取月份日期范围（简化版，直接使用账期）
        String month = period.substring(5) + "月";
        String leftTime = period + "-01";  // 月初
        String rightTime = period + "-31";  // 月末（简化处理）

        speechcraftMap.put("leftTime", leftTime);
        speechcraftMap.put("rightTime", rightTime);
        speechcraftMap.put("month", month);
        speechcraftMap.put("expPersonNum", expPersonNum);
        speechcraftMap.put("continuePersonNum", continuePersonNum);
        speechcraftMap.put("expRate", expRate);
        speechcraftMap.put("underRatePersonNum", underRatePersonNum);

        return speechcraftMap;
    }

    @Override
    public String buildHmdWarnCustSql(String codeInfo, String period, String orderFlag) {
        StringBuilder sqlBody = new StringBuilder();

        if (StringUtils.isBlank(codeInfo) || StringUtils.isBlank(period) || StringUtils.isBlank(orderFlag)) {
            return "";
        }

        String sqlStart = "select uu.area_name AS 'city',uu.COUNTY_NAME 'country',uu.name_ch,uu.relation_tel," +
                "o.cust_name,(select  COUNT(*) as  userCount FROM ftp_tc_user_common_warning cc where" +
                " cc.MAIN_ORDER_FLOW_NO=a.MAIN_ORDER_FLOW_NO and  cc.WARNING_TYPE = '000030') as userCount," +
                "(select date_format( SUBSTR( cc.WARNING_INFO, 21, 8 ), '%Y-%m-%d' ) as exp_time FROM" +
                " ftp_tc_user_common_warning cc where cc.MAIN_ORDER_FLOW_NO=a.MAIN_ORDER_FLOW_NO and" +
                " cc.WARNING_TYPE = '000030' LIMIT 1) as exp_time,DATE_FORMAT(o.create_time,'%Y-%m-%d') create_date," +
                "DATE_FORMAT(o.update_time,'%Y-%m-%d') update_time,o.status_ from topp_order o JOIN" +
                " ftp_tc_user_cust_warning a ON o.info_id = a.id_";

        String sqlEnd = "where o.order_type='hmd_warn' and (select count(1) from  ftp_tc_user_common_warning c" +
                " where  c.MAIN_ORDER_FLOW_NO=a.MAIN_ORDER_FLOW_NO and c.WARNING_TYPE = '000030' AND" +
                " DATE_FORMAT(IF(o.status_='end',o.update_time,now()), '%Y-%m-%d' )>DATE_FORMAT(SUBSTRING_INDEX(SUBSTRING_INDEX(c.WARNING_INFO, '||', 3 ), '||',- 1 ), '%Y-%m-%d')" +
                " AND date_format( SUBSTR( c.WARNING_INFO, 21, 8 ), '%Y-%m' )= '" + period + "')>0 ORDER BY o.id_ DESC";

        String sqlMiddle = "";
        if ("1".equals(orderFlag)) {
            // 区县
            sqlMiddle = " JOIN (select u.id_,u.name_ch,u.relation_tel,p.name_ AS area_name,area.name_ AS COUNTY_NAME" +
                    " from sys_user u,sys_org so,sys_area area,sys_area p where area.bss_code = '" + codeInfo + "' and" +
                    " so.id_=u.org_id and  so.area_id = area.id_ AND area.parent_ = p.id_) uu ON o.creator_=uu.id_ ";
        }
        if ("2".equals(orderFlag)) {
            // 市本级下的组织部门
            sqlMiddle = " JOIN (SELECT vz.user_id,vz.name_ch,vz.name_ relation_tel,vj.* FROM view_jingfen_person vj," +
                    "view_zjsj_manager vz WHERE vj.COUNTY_ID = '" + codeInfo + "' and lower(vj.OA_USER_NAME)=vz.oa_account) uu" +
                    " ON o.creator_=uu.user_id ";
        }

        sqlBody.append(sqlStart).append(sqlMiddle).append(sqlEnd);
        log.debug("【红名单升级预警SQL】{}", sqlBody);
        return sqlBody.toString();
    }

    @Override
    public IPage<Map<String, Object>> queryHmdWarnCust(String sql, Integer pageNum, Integer pageSize) {
        if (StringUtils.isBlank(sql)) {
            return new Page<>();
        }

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS total";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        if (total == null || total == 0) {
            return new Page<>();
        }

        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        String pageSql = sql + " LIMIT " + offset + ", " + pageSize;
        List<Map<String, Object>> records = jdbcTemplate.queryForList(pageSql);

        // 封装分页结果
        IPage<Map<String, Object>> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(records);
        return page;
    }

    @Override
    public Map<String, Object> getHmdUpSpeechcraftStats(String codeInfo, String period, String orderFlag) {
        Map<String, Object> speechcraftMap = new HashMap<>();

        if (StringUtils.isBlank(codeInfo) || StringUtils.isBlank(period) || StringUtils.isBlank(orderFlag)) {
            return speechcraftMap;
        }

        String month = period.substring(5) + "月";
        String codeName = getCodeName(codeInfo, orderFlag);
        speechcraftMap.put("month", month);
        speechcraftMap.put("codeName", codeName);

        // 查询红名单预警总量
        String totalSql;
        if ("1".equals(orderFlag)) {
            // 区县
            totalSql = "SELECT COUNT(1) FROM topp_order o LEFT JOIN sys_area a ON o.area_id=a.id_ WHERE" +
                    " o.order_type = 'hmd_warn' AND a.bss_code='" + codeInfo + "' AND o.create_time" +
                    " LIKE '" + period + "%'";
        } else if ("2".equals(orderFlag)) {
            // 市本级下的组织部门
            totalSql = "SELECT COUNT(1) FROM (select o.*,c.MAIN_ORDER_FLOW_NO,(select date_format( SUBSTR( cc.WARNING_INFO, 21, 8 ), '%Y-%m-%d' ) as exp_time" +
                    " FROM ftp_tc_user_common_warning cc where cc.MAIN_ORDER_FLOW_NO=c.MAIN_ORDER_FLOW_NO and" +
                    " cc.WARNING_TYPE = '000030' LIMIT 1) as exp_time from topp_order o JOIN ftp_tc_user_cust_warning c" +
                    " ON o.info_id=c.id_ WHERE o.creator_ IN (SELECT vz.user_id FROM view_jingfen_person vj,view_zjsj_manager vz" +
                    " WHERE vj.COUNTY_ID = '" + codeInfo + "' and lower(vj.OA_USER_NAME)=vz.oa_account) AND o.order_type='hmd_warn') bb" +
                    " WHERE bb.exp_time LIKE '" + period + "%' ";
        } else {
            speechcraftMap.put("hmdWarnTotal", 0);
            speechcraftMap.put("overTimeTotal", 0);
            return speechcraftMap;
        }

        Integer hmdWarnTotal = jdbcTemplate.queryForObject(totalSql, Integer.class);
        speechcraftMap.put("hmdWarnTotal", hmdWarnTotal != null ? hmdWarnTotal : 0);

        // 查询超时未续约总量（需要执行实际查询获取pageTotal）
        String overTimeSql = buildHmdWarnCustSql(codeInfo, period, orderFlag);
        String overTimeCountSql = "SELECT COUNT(*) FROM (" + overTimeSql + ") AS total";
        Integer overTimeTotal = jdbcTemplate.queryForObject(overTimeCountSql, Integer.class);
        speechcraftMap.put("overTimeTotal", overTimeTotal != null ? overTimeTotal : 0);

        return speechcraftMap;
    }

    @Override
    public String getCodeName(String codeInfo, String orderFlag) {
        if (StringUtils.isBlank(codeInfo) || StringUtils.isBlank(orderFlag)) {
            return "";
        }

        try {
            if ("1".equals(orderFlag)) {
                // 区县：通过BSS编码查询区域名称
                return sysAreaMapper.selectAreaNameByBssCode(codeInfo);
            } else if ("2".equals(orderFlag)) {
                // 市本级：通过MDM组织编码查询名称
                return dwdRepSysPersonDMapper.selectMdmCodeName(codeInfo);
            }
        } catch (Exception e) {
            log.error("【获取区域名称失败】codeInfo={}, orderFlag={}", codeInfo, orderFlag, e);
        }

        return "";
    }
}
