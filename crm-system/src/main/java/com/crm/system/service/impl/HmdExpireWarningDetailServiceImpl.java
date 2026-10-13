package com.crm.system.service.impl;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.HmdExpireWarningDetailMapper;
import com.crm.system.service.IHmdExpireWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户红名单到期预警(000030) 明细 Service 实现
 * <p>
 * 链路：工单 orderId → topp_order.info_id(=ftp_tc_user_cust_warning.id_) → main_order_flow_no
 * → 查 ftp_tc_user_common_warning（MAIN_ORDER_FLOW_NO 匹配）得到用户级明细。
 * 红名单生效/失效为 warning_info 第 2/3 段，逾期欠费合计/最早欠费账期为第 8/9 段（按 || 分隔），
 * 与旧系统 UserCustWarningAction 000030 导出 SQL + Java 解析段位一致。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HmdExpireWarningDetailServiceImpl implements IHmdExpireWarningDetailService {

    /** 处理页首屏默认页大小 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final HmdExpireWarningDetailMapper detailMapper;
    private final IToppOrderService orderService;

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        log.info("【hmd_warn】查询明细: orderId={}, mainOrderFlowNo={}, 记录数={}", orderId, mainOrderFlowNo, rows.size());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(convertRecord(row));
        }
        return result;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return TableDataInfo.build();
        }
        Page<Map<String, Object>> page = pageQuery.build();
        IPage<Map<String, Object>> pageResult = detailMapper.selectByMainOrderFlowNoPage(page, mainOrderFlowNo);
        log.info("【hmd_warn】分页查询明细: orderId={}, mainOrderFlowNo={}, 总数={}", orderId, mainOrderFlowNo, pageResult.getTotal());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : pageResult.getRecords()) {
            result.add(convertRecord(row));
        }
        TableDataInfo<Map<String, Object>> tableDataInfo = new TableDataInfo<>(result, pageResult.getTotal());
        tableDataInfo.setCode(HttpStatus.HTTP_OK);
        tableDataInfo.setMsg("查询成功");
        return tableDataInfo;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryFirstPageRecords(Long orderId, int pageSize) {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE);
        return queryPageRecords(orderId, pageQuery);
    }

    @Override
    public Map<String, Object> queryHeader(Long orderId) {
        Map<String, Object> header = new HashMap<>();
        if (orderId == null) {
            log.warn("【hmd_warn】查询表头入参 orderId 为空");
            return header;
        }
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【hmd_warn】工单不存在或无关联客户记录, orderId={}", orderId);
            return header;
        }
        Map<String, Object> cust = detailMapper.selectCustHeaderById(order.getInfoId());
        if (cust == null) {
            log.warn("【hmd_warn】未找到客户级预警表头, infoId={}", order.getInfoId());
            return header;
        }
        header.put("grpName", getString(cust.get("grp_name")));
        header.put("grpCode", getString(cust.get("grp_code")));
        header.put("userScale", toIntegerQuietly(cust.get("user_scale")));
        // 客户级 warning_info：第1段=逾期欠费合计，第2段=最早欠费账期(yyyyMM)，对齐老 infoByHMD.jsp（长度>=2才展示）
        String custWarningInfo = getString(cust.get("warning_info"));
        if (custWarningInfo != null) {
            String[] parts = custWarningInfo.split("\\|\\|");
            if (parts.length >= 2) {
                header.put("overdueFeeTotal", parts[0].trim());
                header.put("earliestArrearsCycle", formatYearMonth(parts[1]));
            }
        }
        return header;
    }

    /** orderId → 工单 infoId(=ftp_tc_user_cust_warning.id_) → main_order_flow_no；查不到返回 null */
    private String resolveMainOrderFlowNo(Long orderId) {
        if (orderId == null) {
            log.warn("【hmd_warn】解析主订单流水号入参 orderId 为空");
            return null;
        }
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【hmd_warn】工单不存在或无关联记录, orderId={}", orderId);
            return null;
        }
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【hmd_warn】未找到主订单流水号, infoId={}", order.getInfoId());
        }
        return mainOrderFlowNo;
    }

    /** 转前端友好字段；warning_info 段位与旧导出逻辑一致（第2/3段=红名单生效/失效，第8/9段=逾期欠费合计/最早欠费账期） */
    private Map<String, Object> convertRecord(Map<String, Object> row) {
        Map<String, Object> record = new HashMap<>();
        // 地市县分：旧 SQL 为 concat(w.area_name, w.county_id)，任一为 NULL 时 concat 结果为 NULL
        record.put("areaName", concatAreaName(row));
        record.put("developerName", getString(row.get("developer_name")));
        record.put("svcNum", getString(row.get("svc_num")));
        record.put("custName", getString(row.get("cust_name")));
        record.put("openDate", formatOpenDate(row.get("open_date")));
        record.put("prodName", getString(row.get("prod_name")));
        record.put("hqSvcType", getString(row.get("hq_svc_type")));
        record.put("userStatus", getString(row.get("user_status")));
        record.put("certType", getString(row.get("cert_type")));
        String isGrpKey = getString(row.get("is_grp_key"));
        record.put("isGrpKey", "1".equals(isGrpKey) ? "是" : "0".equals(isGrpKey) ? "否" : isGrpKey);
        // 复合列：红名单生效/失效为第2/3段；逾期欠费合计/最早欠费账期为第8/9段（格式对齐旧列表 JSP）
        String warningInfo = getString(row.get("warning_info"));
        if (warningInfo != null) {
            String[] parts = warningInfo.split("\\|\\|");
            record.put("redEffTime", parts.length > 1 ? formatCompactDate(parts[1]) : null);
            record.put("redExpTime", parts.length > 2 ? formatCompactDate(parts[2]) : null);
            record.put("overdueFee", parts.length > 7 ? parts[7] : null);
            record.put("arrearsCycle", parts.length > 8 ? formatYearMonth(parts[8]) : null);
        }
        String renewCreateDate = getString(row.get("new_red_create_date"));
        record.put("isRenew", StringUtils.isNotBlank(renewCreateDate) ? "是" : "否");
        record.put("renewCreateDate", renewCreateDate);
        record.put("renewEffDate", getString(row.get("new_eff_date")));
        record.put("renewExpDate", getString(row.get("new_red_exp_date")));
        record.put("cycleFlag", cycleName(getString(row.get("cycle_flag"))));
        record.put("warningLevel", levelName(getString(row.get("warning_level"))));
        return record;
    }

    private String cycleName(String v) {
        if (v == null) {
            return null;
        }
        switch (v) {
            case "1":
                return "续约";
            case "2":
                return "离网";
            case "3":
                return "超时";
            default:
                return v;
        }
    }

    private String levelName(String v) {
        if (v == null) {
            return null;
        }
        switch (v) {
            case "1":
                return "Ⅰ级";
            case "2":
                return "Ⅱ级";
            case "3":
                return "Ⅲ级";
            default:
                return v;
        }
    }

    /** 镜像旧 SQL concat(w.area_name, w.county_id)：任一部分为 null 则结果为 null，避免拼出 "null" 文本 */
    private String concatAreaName(Map<String, Object> row) {
        String area = getString(row.get("area_name"));
        String county = getString(row.get("county_id"));
        if (area == null || county == null) {
            return null;
        }
        return area + county;
    }

    /**
     * 入网时间格式化：Date/Timestamp → yyyy-MM-dd HH:mm:ss；JDBC 字符串带小数秒 → 截断到秒；其余原样。永不上抛。
     */
    private String formatOpenDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }
        String raw = value.toString();
        String s = raw.trim();
        if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")) {
            return s.substring(0, 19);
        }
        return raw;
    }

    /**
     * 红名单生效/失效时间（warning_info 段）：yyyyMMdd → yyyy-MM-dd HH:mm:ss；非 8 位数字或非法日期则原样返回。
     */
    private String formatCompactDate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (!s.matches("\\d{8}")) {
            return raw;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parseStrict(s, "yyyyMMdd"));
        } catch (ParseException e) {
            return s;
        }
    }

    /**
     * 最早欠费账期（warning_info 段）：yyyyMM → yyyy-MM；非 6 位数字或非法月份则原样返回。
     */
    private String formatYearMonth(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (!s.matches("\\d{6}")) {
            return raw;
        }
        try {
            return new SimpleDateFormat("yyyy-MM").format(parseStrict(s, "yyyyMM"));
        } catch (ParseException e) {
            return s;
        }
    }

    private Date parseStrict(String source, String pattern) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        return format.parse(source);
    }

    /** user_scale 转 Integer（数据库可能返回 Number 或字符串），非数字则返回 null */
    private Integer toIntegerQuietly(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        String s = v.toString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }
}
