package com.crm.system.service.impl;

import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.ZqztLedgerCloudConfigBo;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;
import com.crm.system.mapper.DwdRepFzPersonDMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.ZqztOrderInfoObMapper;
import com.crm.system.service.IZqztFlameGuardianOrderService;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.send.ISendDMessageService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.warning.WarningOrderCreateService;
import com.crm.system.service.warning.WarningRecord;
import com.crm.system.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 火焰卫士交付订单自动生成服务实现
 * <p>
 * 链路：OB 两表（sl08md / 04049f）按「创建时间窗口 + 地市白名单」增量扫描
 * → 组装 WarningRecord（含待办表单快照流程变量）
 * → WarningOrderCreateService 起 single_approval 流程 + 建 topp_order
 * → 按客户经理聚合发短信。
 * </p>
 * <p>
 * 断点续跑：上次扫到哪儿由 sys_param.flame_guardian_scan_end_time（上一次窗口的结束时间）记录，
 * 间隔由 sys_param.flame_guardian_scan_interval_minutes 配置。
 * 整体无异常才把本次窗口结束时间写回；异常时不写，下轮从同一窗口重扫 ——
 * 重复由 createOrder 前的去重挡掉。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianOrderServiceImpl implements IZqztFlameGuardianOrderService {

    /**
     * 服务大厅工单号流程变量名 —— 既是待办页初值来源之一，也是作废 job 反查在途工单的键。
     * 命名遵循 WarningOrderCreateService 的 {taskDefKey}_ 前缀约定。
     */
    public static final String VAR_ORDER_NO = "manager_handle_orderNo";

    private static final String PARAM_TYPE = "flame_guardian";
    /** 允许发起待办的地市 */
    private static final String PARAM_CITY = "flame_guardian_city_allow";
    /** 扫描时间间隔（分钟） */
    private static final String PARAM_INTERVAL = "flame_guardian_scan_interval_minutes";
    /** 上一次扫描周期的结束时间（断点续跑用） */
    private static final String PARAM_LAST_SCAN_END = "flame_guardian_scan_end_time";

    private static final int DEFAULT_INTERVAL_MINUTES = 10;
    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final long ONE_MINUTE_MILLIS = 60_000L;
    private static final String ORDER_TYPE = Constants.ORDER_TYPE_FLAME_GUARDIAN;
    private static final String PROCESS_KEY = "single_approval";
    /** 工单号列长度上限，超长直接跳过（OB 侧工单号形如 UCSC-36-260616-000008449） */
    private static final int ORDER_NO_MAX_LENGTH = 50;

    /** 台账全流程状态：2-派单失败（OB 客户经理在本系统用户表不存在 / OB 未指定客户经理） */
    private static final String STATUS_DISPATCH_FAILED = "2";

    /** 宽带是否利旧：利旧 / 新装（与 ZqztLedgerObMergeUtil 同口径） */
    private static final String REUSE_OLD = "OLD";
    private static final String REUSE_NEW = "NEW";

    private final ZqztOrderInfoObMapper orderInfoObMapper;
    private final DwdRepFzPersonDMapper personDMapper;
    private final WarningOrderCreateService warningOrderCreateService;
    private final SysUsersMapper sysUsersMapper;
    private final ISysParamService sysParamService;
    private final ISendDMessageService sendDMessageService;
    private final IZqztLedgerInfoService ledgerInfoService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public int runOnce() {
        int interval = resolveIntervalMinutes();
        Date now = new Date();
        Date lastEnd = readLastScanEndTime();
        // 上一次结束时间即本轮窗口起点；首次运行（参数为空）则取近 interval 分钟
        Date windowStart = lastEnd != null ? lastEnd : new Date(now.getTime() - interval * ONE_MINUTE_MILLIS);
        Date windowEnd = new Date(windowStart.getTime() + interval * ONE_MINUTE_MILLIS);
        if (windowEnd.after(now)) {
            log.info("【火焰卫士建单】窗口未到点，本轮不扫: start={}, end={}", windowStart, windowEnd);
            return 0;
        }

        List<String> cities = resolveAllowCities();
        int scanCount = 0;
        int createdCount = 0;
        int dispatchFailedCount = 0;
        try {
            List<ZqztOrderInfoObVo> rows = new ArrayList<>();
            rows.addAll(orderInfoObMapper.scanSl08md(windowStart, windowEnd, cities));
            rows.addAll(orderInfoObMapper.scan04049f(windowStart, windowEnd, cities));
            scanCount = rows.size();

            // 同一窗口内按工单号去重（同一单可能同时落在两表）
            Map<String, ZqztOrderInfoObVo> byOrderNo = new HashMap<>();
            for (ZqztOrderInfoObVo row : rows) {
                String orderNo = trim(row.getOrderNo());
                if (orderNo == null) {
                    continue;
                }
                // 地市白名单逐行兜底校验：city 为空或不在白名单 → 跳过
                // （SQL 的 IN 条件只是收窄，故此处必须再判一次；白名单缺失时已在解析处 fail-closed）
                String city = trim(row.getCity());
                if (city == null || !cities.contains(city)) {
                    log.warn("【火焰卫士建单】city 为空或不在白名单，跳过: orderNo={}, city={}", orderNo, city);
                    continue;
                }
                // 两表都有时表1（sl08md）优先：先放进去的不覆盖
                byOrderNo.putIfAbsent(orderNo, row);
            }

            Set<String> created = findCreatedOrderNos(byOrderNo.keySet());
            Map<Long, Integer> managerCount = new HashMap<>();

            for (Map.Entry<String, ZqztOrderInfoObVo> entry : byOrderNo.entrySet()) {
                String orderNo = entry.getKey();
                if (created.contains(orderNo)) {
                    log.info("【火焰卫士建单】工单已建过待办，跳过: orderNo={}", orderNo);
                    continue;
                }
                CreateResult result;
                try {
                    result = createOrder(entry.getValue(), orderNo);
                } catch (Exception e) {
                    // 单条处理失败不阻断本轮：记日志后跳过该单，本轮仍照常推进断点时间
                    // （scan_count 与 created_count 的差值即失败条数，见尾部汇总日志）
                    log.warn("【火焰卫士建单】处理异常，跳过该单: orderNo={}", orderNo, e);
                    continue;
                }
                if (result.managerId != null) {
                    createdCount++;
                    managerCount.merge(result.managerId, 1, Integer::sum);
                } else if (result.dispatchFailRemark != null
                        && saveDispatchFailedLedger(entry.getValue(), orderNo, result.dispatchFailRemark)) {
                    // 派单失败也落台账（status=2），数据不丢，留给火焰卫士负责人后续修正
                    dispatchFailedCount++;
                }
            }

            if (!managerCount.isEmpty() && sendDMessageService != null) {
                for (Map.Entry<Long, Integer> entry : managerCount.entrySet()) {
                    List<Integer> users = new ArrayList<>();
                    users.add(entry.getKey().intValue());
                    sendDMessageService.initNoticeMessage(users,
                            "温馨提醒：您收到" + entry.getValue() + "个"
                                    + Constants.ORDER_TYPE_FLAME_GUARDIAN_NAME
                                    + "工单，已生成待处理任务，请至创新头条-省分专区-运营平台。");
                }
            }

            // 记录本轮扫描周期的结束时间（下次从这里接着扫）；异常时走不到这里 → 断点时间不更新
            saveLastScanEndTime(windowEnd);
            log.info("【火焰卫士建单】本轮完成: 窗口=[{}, {}), 取到={}, 建待办={}, 派单失败落台账={}",
                    windowStart, windowEnd, scanCount, createdCount, dispatchFailedCount);
            return createdCount;
        } catch (Exception e) {
            log.error("【火焰卫士建单】本轮异常，不更新上次结束时间: 窗口=[{}, {})",
                    windowStart, windowEnd, e);
            throw e;
        }
    }

    // ==================== 单条建单 ====================

    /** 单条处理结果：建待办成功（managerId）或派单失败（dispatchFailRemark），两者互斥 */
    private static final class CreateResult {

        /** 建待办成功时的客户经理 userId；未建待办为 null */
        private final Long managerId;

        /** 派单失败原因；非 null 表示调用方需把自动取数结果直接落台账（status=2） */
        private final String dispatchFailRemark;

        private CreateResult(Long managerId, String dispatchFailRemark) {
            this.managerId = managerId;
            this.dispatchFailRemark = dispatchFailRemark;
        }

        static CreateResult created(Long managerId) {
            return new CreateResult(managerId, null);
        }

        static CreateResult dispatchFailed(String remark) {
            return new CreateResult(null, remark);
        }

        /** 既不建待办也不落台账（如工单号超长） */
        static CreateResult skipped() {
            return new CreateResult(null, null);
        }
    }

    /**
     * 处理一条 OB 单：定位得到客户经理就建待办；定位不到（operator 为空 / 在本系统用户表查不到）
     * 就交由调用方把自动取数结果直接落成台账（全流程状态=2 派单失败），数据不丢。
     */
    private CreateResult createOrder(ZqztOrderInfoObVo row, String orderNo) {
        if (orderNo.length() > ORDER_NO_MAX_LENGTH) {
            log.warn("【火焰卫士建单】工单号超长，跳过: orderNo={}", orderNo);
            return CreateResult.skipped();
        }
        String operator = trim(row.getOperator());
        Map<String, Object> userRow = operator == null ? null : sysUsersMapper.selectUserByOaId(operator);
        Long managerId = userRow == null ? null : toLong(userRow.get("user_id"));
        if (managerId == null) {
            // 派单失败：没有待办可派，但 OB 取到的台账信息不能丢，直接落库并标注原因
            String remark = operator == null
                    ? "派单失败：OB工单未指定客户经理（operator为空）"
                    : "派单失败：OB客户经理[" + operator + "]在系统中不存在";
            log.warn("【火焰卫士建单】{}, orderNo={}", remark, orderNo);
            return CreateResult.dispatchFailed(remark);
        }

        WarningRecord record = new WarningRecord();
        record.setOrderType(ORDER_TYPE);
        String companyName = trim(row.getCompanyName());
        record.setOrderName(new SimpleDateFormat("yyyyMMdd").format(new Date())
                + "_" + (companyName == null ? orderNo : companyName)
                + "_" + Constants.ORDER_TYPE_FLAME_GUARDIAN_NAME);
        record.setCustName(companyName);
        record.setCustManagerId(managerId);
        // 建单信号键：由 WarningOrderCreateService 用 Map.get 读取（缺省与 null 等价），
        // 且都在 FILTER_KEYS 内，不会进流程变量表，故照旧无条件写入
        record.setExtraVariable("_needOrderNumber", true);
        record.setExtraVariable("orderCategory", "C");
        record.setExtraVariable("orgId", toLong(userRow.get("dept_id")));
        record.setExtraVariable("nickName", getString(userRow.get("nick_name")));
        record.setExtraVariable("phone", getString(userRow.get("phonenumber")));

        // ---- 待办表单快照（写流程变量，供待办页 /workflow/process/detail 回显）----
        // 取值为空（null 或空串）时干脆不写该变量：WarningOrderCreateService 会把这些键原样
        // 透传给 Flowable，写 null 与不写对消费方（Task 8 表单初值、Task 19 前端回填）等价
        record.setExtraVariable(VAR_ORDER_NO, orderNo);
        putVar(record, "fg_orderNo", orderNo);
        putVar(record, "fg_orderCreateTime", row.getOrderCreateTime());
        putVar(record, "fg_cityId", trim(row.getCity()));
        putVar(record, "fg_cityName", trim(row.getCityName()));
        putVar(record, "fg_companyName", companyName);
        putVar(record, "fg_creditCode", trim(row.getCreditCode()));
        putVar(record, "fg_regAddress", trim(row.getRegAddress()));
        putVar(record, "fg_actualAddress", trim(row.getActualAddress()));
        putVar(record, "fg_companyPrincipal", trim(row.getContactName()));
        putVar(record, "fg_principalPhone", trim(row.getContactPhone()));
        putVar(record, "fg_safetyOfficer1",
                firstNonBlank(row.getSafetyOfficerName1(), row.getSafetyOfficerName2()));
        putVar(record, "fg_safetyOfficer1Phone",
                firstNonBlank(row.getSafetyOfficerPhone1(), row.getSafetyOfficerPhone2()));
        putVar(record, "fg_unicomManager", trim(row.getDevelopName()));
        putVar(record, "fg_unicomManagerPhone", trim(row.getDevelopPhone()));
        putVar(record, "fg_unicomManagerCounty", resolveCountyName(row.getDevelopStaffId()));
        putVar(record, "fg_signRoutes", firstNonBlank(row.getSignRoutes1(), row.getSignRoutes2()));
        putVar(record, "fg_cloudAccount", firstNonBlank(row.getCloudAccount1(), row.getCloudAccount2()));
        putVar(record, "fg_cloudVisionPackageId",
                firstNonBlank(row.getCloudVisionPackageId1(), row.getCloudVisionPackageId2()));
        putVar(record, "fg_networkAccountList",
                firstNonBlank(row.getCameraAccount1(), row.getCameraAccount2()));

        // 宽带账号 + 是否利旧：沿用「利旧优先于新装」口径（与 ZqztLedgerObMergeUtil 一致）
        String[] broadband = resolveBroadband(row);
        if (broadband != null) {
            putVar(record, "fg_broadbandIsReuse", broadband[0]);
            putVar(record, "fg_broadbandAccount", broadband[1]);
        }

        // 零星工程三项：已并入扫描 SQL，与其他快照字段同款直取；
        // 「零星工程」OB 侧存「是/否」、本系统台账存 1/0，此处归一
        putVar(record, "fg_broadbandIsSporadic", normalizeYesNo(row.getBroadbandIsSporadic()));
        putVar(record, "fg_broadbandSporadicOrderNo", trim(row.getBroadbandSporadicOrderNo()));
        putVar(record, "fg_broadbandAddNum", toIntOrNull(row.getBroadbandAddNum()));

        // 签约日期（工作表关联列确认不了时把这段与 queryAuditDealTime 一起注释掉，允许为空）
        Date dealTime = queryAuditDealTime(orderNo);
        if (dealTime != null) {
            record.setExtraVariable("fg_signDate", dealTime);
        }

        ToppOrderBo orderBo = warningOrderCreateService.createAndStartOrder(
                record, PROCESS_KEY, managerId,
                (orderId, extra) -> "/flameGuardian/deal?orderId=" + orderId, null);
        if (orderBo == null) {
            log.warn("【火焰卫士建单】流程启动或建单失败: orderNo={}", orderNo);
            return CreateResult.skipped();
        }
        log.info("【火焰卫士建单】已建待办: orderNo={}, orderId={}, 客户经理={}", orderNo, orderBo.getId(), managerId);
        return CreateResult.created(managerId);
    }

    // ==================== 派单失败落台账 ====================

    /**
     * 派单失败落台账：把 OB 自动取数结果直接写成一条台账（全流程状态=2 派单失败），
     * 主表字段 + 1 条子表行（组网号码 / 宽带账号 / 是否利旧）。
     * <p>
     * 必须单独查重：派单失败**不产生 Flowable 流程实例**，建单前的
     * {@link #findCreatedOrderNos} 是按流程变量去重的，挡不住重复窗口重扫，
     * 而 {@code zqzt_ledger_info.order_no} 是唯一键，重复插入会直接抛异常。
     * </p>
     *
     * @return 是否成功写入了新台账
     */
    private boolean saveDispatchFailedLedger(ZqztOrderInfoObVo row, String orderNo, String remark) {
        try {
            if (Boolean.TRUE.equals(ledgerInfoService.existsByOrderNo(orderNo))) {
                log.info("【火焰卫士建单】派单失败台账已存在，跳过: orderNo={}", orderNo);
                return false;
            }
            ZqztLedgerInfoBo bo = new ZqztLedgerInfoBo();
            bo.setOrderNo(orderNo);
            bo.setOrderCreateTime(row.getOrderCreateTime());
            bo.setCityId(trim(row.getCity()));
            bo.setCompanyName(trim(row.getCompanyName()));
            bo.setCreditCode(trim(row.getCreditCode()));
            bo.setRegAddress(trim(row.getRegAddress()));
            bo.setActualAddress(trim(row.getActualAddress()));
            bo.setSignRoutes(toIntOrNull(firstNonBlank(row.getSignRoutes1(), row.getSignRoutes2())));
            bo.setCompanyPrincipal(trim(row.getContactName()));
            bo.setPrincipalPhone(trim(row.getContactPhone()));
            bo.setSafetyOfficer1(firstNonBlank(row.getSafetyOfficerName1(), row.getSafetyOfficerName2()));
            bo.setSafetyOfficer1Phone(firstNonBlank(row.getSafetyOfficerPhone1(), row.getSafetyOfficerPhone2()));
            bo.setUnicomManager(trim(row.getDevelopName()));
            bo.setUnicomManagerPhone(trim(row.getDevelopPhone()));
            bo.setUnicomManagerCounty(resolveCountyName(row.getDevelopStaffId()));
            bo.setCloudAccount(firstNonBlank(row.getCloudAccount1(), row.getCloudAccount2()));
            bo.setCloudVisionPackageId(
                    firstNonBlank(row.getCloudVisionPackageId1(), row.getCloudVisionPackageId2()));
            bo.setBroadbandIsSporadic(normalizeYesNo(row.getBroadbandIsSporadic()));
            bo.setBroadbandSporadicOrderNo(trim(row.getBroadbandSporadicOrderNo()));
            bo.setBroadbandAddNum(toIntOrNull(row.getBroadbandAddNum()));
            bo.setSignDate(queryAuditDealTime(orderNo));
            bo.setCloudVisionConfigs(Collections.singletonList(buildDispatchFailedConfig(row)));
            return Boolean.TRUE.equals(ledgerInfoService.insertByBo(bo, STATUS_DISPATCH_FAILED, remark));
        } catch (Exception e) {
            log.error("【火焰卫士建单】派单失败落台账异常: orderNo={}", orderNo, e);
            return false;
        }
    }

    /**
     * 派单失败的子表行：只带 OB 能取到的组网号码 / 宽带账号 / 是否利旧。
     * <p>设备类字段由客户经理在待办页填，而本单没有待办故必然为空；写这一行是为了让
     * 「竣工时间回填 job」与「同步火焰卫士 job」对派单失败台账也还能生效。</p>
     */
    private ZqztLedgerCloudConfigBo buildDispatchFailedConfig(ZqztOrderInfoObVo row) {
        ZqztLedgerCloudConfigBo cfg = new ZqztLedgerCloudConfigBo();
        cfg.setCameraAccount(firstNonBlank(row.getCameraAccount1(), row.getCameraAccount2()));
        String[] broadband = resolveBroadband(row);
        if (broadband != null) {
            cfg.setBroadbandIsReuse(broadband[0]);
            cfg.setBroadbandAccount(broadband[1]);
        }
        cfg.setSortOrder(1);
        return cfg;
    }

    /** 按工单号取「中台稽核」完成时间作为签约日期；取不到/异常返回 null */
    private Date queryAuditDealTime(String orderNo) {
        try {
            return orderInfoObMapper.selectAuditDealTime(orderNo);
        } catch (Exception e) {
            log.warn("【火焰卫士建单】取签约日期失败（忽略）: orderNo={}", orderNo);
            return null;
        }
    }

    /**
     * 宽带账号与是否利旧取值（与 {@code ZqztLedgerObMergeUtil} 同口径）：
     * 利旧口径有值 → OLD + 该账号；否则新装口径有值 → NEW + 该账号；都无值 → null。
     *
     * @return {@code [0]}=是否利旧(OLD/NEW)，{@code [1]}=宽带账号；无值时返回 null
     */
    private String[] resolveBroadband(ZqztOrderInfoObVo row) {
        String reuseAccount = firstNonBlank(row.getBroadbandAccountOld1(), row.getBroadbandAccountOld2());
        if (reuseAccount != null) {
            return new String[]{REUSE_OLD, reuseAccount};
        }
        String newAccount = firstNonBlank(row.getBroadbandAccountNew1(), row.getBroadbandAccountNew2());
        if (newAccount != null) {
            return new String[]{REUSE_NEW, newAccount};
        }
        return null;
    }

    // ==================== 零星工程三项 ====================

    /**
     * 「零星工程」取值归一：OB 侧 radioField_* 存的是「是/否」，本系统台账存 1/0。
     * <p>不做归一会让待办页/修改页的「1=是 / 0=否」下拉框选不中。无法识别的取值按空处理
     * （不写快照），由客户经理在表单里手选，避免把脏值带进表单。</p>
     */
    private String normalizeYesNo(String value) {
        String v = trim(value);
        if (v == null) {
            return null;
        }
        if ("是".equals(v) || "1".equals(v)) {
            return "1";
        }
        if ("否".equals(v) || "0".equals(v)) {
            return "0";
        }
        log.warn("【火焰卫士建单】零星工程取值无法识别，按空处理: {}", v);
        return null;
    }

    /** OB 文本列 → Integer；空/非数字返回 null（与 ZqztLedgerObMergeUtil.firstNonBlankInt 同口径） */
    private Integer toIntOrNull(String v) {
        String t = trim(v);
        if (t == null) {
            return null;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 归属县分：把 OB 的 develop_staffId 当 CHANNEL_HQ_CODE 查达梦 dwd_rep_fz_person_d
     * （需求 §5.3.1④ 原文口径），取不到留空
     */
    private String resolveCountyName(String developStaffId) {
        String hqCode = trim(developStaffId);
        if (hqCode == null) {
            return null;
        }
        try {
            return trim(personDMapper.selectCountyNameByChannelHqCode(hqCode));
        } catch (Exception e) {
            log.warn("【火焰卫士建单】取归属县分失败（忽略）: developStaffId={}", hqCode);
            return null;
        }
    }

    // ==================== 去重 ====================

    /**
     * 查已建过待办的工单号集合
     * <p>
     * 以流程变量 VAR_ORDER_NO 为准（历史变量表在流程结束后仍保留）。
     * 不限定时间范围：FLO_ACT_HI_VARINST 上有 (NAME_, VAR_TYPE_) 索引，NAME_ 等值即可索引查找；
     * 若额外用「近 N 天启动的流程实例」收窄，任务停跑或连续失败超过 N 天后重扫同一窗口就会漏判，
     * 正是断点续跑要防的重复建单场景。
     * </p>
     */
    private Set<String> findCreatedOrderNos(Set<String> candidateOrderNos) {
        if (candidateOrderNos == null || candidateOrderNos.isEmpty()) {
            return new HashSet<>();
        }
        String sql = "SELECT TEXT_ FROM FLO_ACT_HI_VARINST WHERE NAME_ = ? AND TEXT_ IS NOT NULL";
        List<String> exists = jdbcTemplate.queryForList(sql, String.class, VAR_ORDER_NO);
        Set<String> result = new HashSet<>();
        for (String orderNo : exists) {
            if (orderNo != null && candidateOrderNos.contains(orderNo)) {
                result.add(orderNo);
            }
        }
        return result;
    }

    // ==================== 上次扫描结束时间（sys_param 断点） ====================

    /** 读「上一次扫描周期的结束时间」；未配置或格式非法返回 null（按首次运行处理） */
    private Date readLastScanEndTime() {
        SysParamDo param = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_LAST_SCAN_END);
        if (param == null || StringUtils.isBlank(param.getValue())) {
            return null;
        }
        try {
            return new SimpleDateFormat(TIME_PATTERN).parse(param.getValue().trim());
        } catch (ParseException e) {
            log.warn("【火焰卫士建单】上次结束时间无法解析，按首次运行处理: {}", param.getValue());
            return null;
        }
    }

    /** 写「本次扫描周期的结束时间」；参数不存在时自动补建一行 */
    private void saveLastScanEndTime(Date windowEnd) {
        String value = new SimpleDateFormat(TIME_PATTERN).format(windowEnd);
        SysParamDo param = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_LAST_SCAN_END);
        if (param == null) {
            SysParamDo row = new SysParamDo();
            row.setCode(PARAM_LAST_SCAN_END);
            row.setName("火焰卫士上一次扫描结束时间");
            row.setValue(value);
            row.setType(PARAM_TYPE);
            row.setInputType("string");
            row.setIsSystem("1");
            row.setRemark("断点续跑用，格式 yyyy-MM-dd HH:mm:ss；只在整体无异常时更新");
            sysParamService.insertParam(row);
            log.info("【火焰卫士建单】上次结束时间已补建: {}", value);
            return;
        }
        param.setValue(value);
        sysParamService.updateParam(param);
        log.info("【火焰卫士建单】上次结束时间已更新: {}", value);
    }

    // ==================== 配置 ====================

    private int resolveIntervalMinutes() {
        SysParamDo param = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_INTERVAL);
        if (param == null || StringUtils.isBlank(param.getValue())) {
            return DEFAULT_INTERVAL_MINUTES;
        }
        try {
            int v = Integer.parseInt(param.getValue().trim());
            return v > 0 ? v : DEFAULT_INTERVAL_MINUTES;
        } catch (NumberFormatException e) {
            return DEFAULT_INTERVAL_MINUTES;
        }
    }

    /**
     * 地市白名单；未配置（或配置里没有有效地市值）时 fail-closed：直接抛业务异常。
     * <p>空集合会让 SQL 的 IN 条件与 runOnce 的逐行兜底校验同时失效，等于全省扫描，
     * 会把待办派给错误的客户经理；白名单缺失属部署遗漏而非「不过滤」意图。
     * 本方法在 runOnce 的轮次级 try 之前调用，异常由此向外抛出 → 本轮不扫、断点不推进，
     * 配置补齐后下一轮自愈。要放开地市过滤请显式列出地市。</p>
     */
    private List<String> resolveAllowCities() {
        SysParamDo param = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_CITY);
        if (param == null || StringUtils.isBlank(param.getValue())) {
            throw new ServiceException("未配置地市白名单参数 " + PARAM_TYPE + "." + PARAM_CITY + "，本轮不扫描");
        }
        List<String> cities = Arrays.stream(param.getValue().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (cities.isEmpty()) {
            throw new ServiceException("地市白名单参数 " + PARAM_TYPE + "." + PARAM_CITY + " 内无有效地市值，本轮不扫描");
        }
        return cities;
    }

    // ==================== 小工具 ====================

    /**
     * 写一条流程变量快照；值为 null 或空白串时直接不写该键
     * <p>
     * 这些键会被 WarningOrderCreateService 原样透传进 Flowable 运行时变量表，
     * 不写与写 null 对读取方等价，故不依赖引擎的 null 语义。
     * </p>
     */
    private void putVar(WarningRecord record, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String && ((String) value).trim().isEmpty()) {
            return;
        }
        record.setExtraVariable(key, value);
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String firstNonBlank(String a, String b) {
        return trim(a) != null ? trim(a) : trim(b);
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            if (v instanceof Number) {
                return ((Number) v).longValue();
            }
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
