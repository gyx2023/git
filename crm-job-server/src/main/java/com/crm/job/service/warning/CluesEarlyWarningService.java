package com.crm.job.service.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.mapper.warning.CluesEarlyWarningMapper;
import com.crm.job.mapper.warning.GroupClueOrderMapper;
import com.crm.job.mapper.warning.MessageRecordMapper;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.send.ISendDMessageService;
import com.crm.system.service.warning.WarningOrderCreateService;
import com.crm.system.service.warning.WarningRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 营销线索预警（定时任务）业务实现
 * <p>
 * 迁移自旧系统 {@code com.hollycrm.job.CluesEarlyWarning#insertWaring(String)}。
 * 业务语义与旧系统保持一致，共 7 段：
 * <ol>
 *     <li>专家评审超时 —— 消息提醒（去重：expert_review）</li>
 *     <li>领导审核超时 —— 消息提醒（去重：leader_review）</li>
 *     <li>未细化分配   —— 消息提醒（去重：clue_refine）</li>
 *     <li>走访不合格   —— 消息提醒（不去重，对齐旧系统 visit_not_qualified）</li>
 *     <li>线索未关单   —— 消息提醒（不去重，对齐旧系统 clue_not_close）</li>
 *     <li>走访不合格   —— 生成待办：领导审核（去重：leader_visit_not_qualified）</li>
 *     <li>线索未关单   —— 生成待办：领导审核（去重：leader_clue_not_close）</li>
 * </ol>
 * <p>
 * 与旧系统的实现差异（详见代码评审说明）：
 * <ul>
 *     <li>消息发送复用新框架 {@link ISendDMessageService#initNoticeMessage}（写 ding_message，逻辑与旧系统一致）</li>
 *     <li>待办建单走新框架 {@link WarningOrderCreateService}（Flowable + topp_order），
 *         流程复用通用单节点流程 {@code single_approval}——旧系统 {@code clue_warnning_order}
 *         本身就是单节点「领导审核」，两者语义等价</li>
 *     <li>地市参数经白名单校验后再拼入 SQL，日期参数改为预编译占位符</li>
 * </ul>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CluesEarlyWarningService {

    /** 工单类型，与旧系统 topp_order.order_type 保持一致 */
    private static final String ORDER_TYPE = "clue_warnning_order";

    /** 复用通用单节点审批流程（旧系统 clue_warnning_order 流程为单节点「领导审核」） */
    private static final String PROCESS_KEY = "single_approval";

    /** 首节点显示名（对齐旧系统节点名「领导审核」） */
    private static final String FIRST_NODE_NAME = "领导审核";

    /** 消息类型：专家评审超时 */
    private static final String TYPE_EXPERT_REVIEW = "expert_review";
    /** 消息类型：领导审核超时 */
    private static final String TYPE_LEADER_REVIEW = "leader_review";
    /** 消息类型：未细化分配 */
    private static final String TYPE_CLUE_REFINE = "clue_refine";
    /** 消息类型：走访不合格（消息） */
    private static final String TYPE_VISIT_NOT_QUALIFIED = "visit_not_qualified";
    /** 消息类型：线索未关单（消息） */
    private static final String TYPE_CLUE_NOT_CLOSE = "clue_not_close";

    /** 待办去重类型：走访不合格 */
    private static final String DEDUP_LEADER_VISIT = "leader_visit_not_qualified";
    /** 待办去重类型：线索未关单 */
    private static final String DEDUP_LEADER_NOT_CLOSE = "leader_clue_not_close";

    /** 需要按 message_record 去重的消息类型（对齐旧系统 saveDingMsg 中的判断） */
    private static final Set<String> DEDUP_MESSAGE_TYPES = new LinkedHashSet<>(Arrays.asList(
            TYPE_EXPERT_REVIEW, TYPE_LEADER_REVIEW, TYPE_CLUE_REFINE));

    /** 地市参数白名单：仅允许数字、逗号、空白（该值以 ${} 方式拼入 SQL，必须校验） */
    private static final String AREA_PARAM_PATTERN = "^[0-9,\\s]+$";

    /** 待办处理页（线索审核页仍在老框架，待线索模块迁移后再切换） */
    private static final String DEAL_URL = "/cluesEarly/toCluesEarlyAudit?urlType=deal";

    private final CluesEarlyWarningMapper clueMapper;
    private final MessageRecordMapper messageRecordMapper;
    private final GroupClueOrderMapper groupClueOrderMapper;
    private final ISysUsersService sysUsersService;
    private final SysUsersMapper sysUsersMapper;
    private final ISendDMessageService sendDMessageService;
    private final WarningOrderCreateService warningOrderCreateService;

    /**
     * 执行营销线索预警
     * <p>
     * XXL-Job 参数格式与旧系统一致：
     * <pre>
     * 空                              —— 不限地市、不限业务数据时间
     * 330100,330200                   —— 仅限制地市
     * 330100,330200;2021-01-01 00:00:00,2021-01-31 23:59:59  —— 地市 + 业务数据时间范围
     * </pre>
     *
     * @param jobParam XXL-Job 任务参数
     */
    public void insertWaring(String jobParam) {
        log.info("定时任务参数===={}", jobParam);

        String areaParam = "";
        String dateParam1 = "";
        String dateParam2 = "";
        if (StringUtils.isNotBlank(jobParam) && jobParam.contains(";")) {
            // 参数格式：地市;时间  例：330100,330200;2021-01-01 00:00:00,2021-01-01 00:00:00
            String[] arr = jobParam.split(";");
            areaParam = arr[0];
            if (arr.length > 1 && arr[1].contains(",")) {
                String[] dates = arr[1].split(",");
                dateParam1 = dates[0].trim();
                if (dates.length > 1) {
                    dateParam2 = dates[1].trim();
                }
            }
        } else {
            areaParam = jobParam;
        }
        areaParam = normalizeAreaParam(areaParam);

        // lambda 捕获要求 effectively final，这里固化为常量
        final String area = areaParam;
        final String date1 = dateParam1;
        final String date2 = dateParam2;

        List<String> errors = new ArrayList<>();

        // 1、专家评审超时：营销线索下发 24 小时内专家未完成评审
        runStep(errors, "专家评审超时", () -> {
            List<Map<String, Object>> rows = clueMapper.professorTimeout(area, date1, date2);
            if (!rows.isEmpty()) {
                saveDingMsg(rows, TYPE_EXPERT_REVIEW);
            }
        });

        // 2、领导审核超时：专家评审通过后 24 小时内未完成审核
        runStep(errors, "领导审核超时", () -> {
            List<Map<String, Object>> rows = clueMapper.leaderTimeout(area, date1, date2);
            if (!rows.isEmpty()) {
                saveDingMsg(rows, TYPE_LEADER_REVIEW);
            }
        });

        // 3、未细化分配：领导审核通过 3 天内未完成分配
        runStep(errors, "未细化分配", () -> {
            List<Map<String, Object>> rows = clueMapper.noRefine(area, date1, date2);
            if (!rows.isEmpty()) {
                saveDingMsg(rows, TYPE_CLUE_REFINE);
            }
        });

        // 4、走访不合格：客户经理接单后 7 天内目标客户走访率低于 100%
        runStep(errors, "走访不合格(消息)", () -> {
            List<Map<String, Object>> rows = clueMapper.visitUnQualified(area, date1, date2);
            if (!rows.isEmpty()) {
                saveDingMsg(rows, TYPE_VISIT_NOT_QUALIFIED);
            }
        });

        // 5、线索未关单：客户名单分配完成 60 天内线索关单率低于 100%
        runStep(errors, "线索未关单(消息)", () -> {
            List<Map<String, Object>> rows = clueMapper.custUnShutdown(area, date1, date2);
            if (!rows.isEmpty()) {
                saveDingMsg(rows, TYPE_CLUE_NOT_CLOSE);
            }
        });

        // ==================== 以下生成待办（对齐旧系统「以下生成待办,同步至OA」） ====================

        // 6、走访不合格：客户名单分配完成 10 天内走访率低于 100%（行业部领导 / 县分分管领导）
        runStep(errors, "走访不合格(待办)", () -> {
            List<Map<String, Object>> rows = clueMapper.clientVisitRate(area, date1, date2);
            for (Map<String, Object> row : rows) {
                Long userId = toLong(get(row, "userId"));
                Long clueId = toLong(get(row, "clueId"));
                if (userId == null || clueId == null) {
                    continue;
                }
                String dedupKey = clueId.toString();
                if (messageRecordMapper.countByRecord(userId, dedupKey, DEDUP_LEADER_VISIT) > 0) {
                    continue;
                }
                messageRecordMapper.insertRecord(userId, dedupKey, DEDUP_LEADER_VISIT);
                startProcessAndSaveOrder(userId, str(get(row, "content")),
                        toLong(get(row, "areaId")), toLong(get(row, "industryId")), clueId, "走访不合格");
            }
        });

        // 7、线索未关单：客户名单分配完成 70 天内关单率低于 100%（行业部领导 / 县分分管领导）
        runStep(errors, "线索未关单(待办)", () -> {
            List<Map<String, Object>> rows = clueMapper.custUnShutdown1(area, date1, date2);
            for (Map<String, Object> row : rows) {
                Long userId = toLong(get(row, "userId"));
                Long clueId = toLong(get(row, "clueId"));
                if (userId == null || clueId == null) {
                    continue;
                }
                String dedupKey = clueId.toString();
                if (messageRecordMapper.countByRecord(userId, dedupKey, DEDUP_LEADER_NOT_CLOSE) > 0) {
                    continue;
                }
                messageRecordMapper.insertRecord(userId, dedupKey, DEDUP_LEADER_NOT_CLOSE);
                startProcessAndSaveOrder(userId, str(get(row, "content")),
                        toLong(get(row, "areaId")), toLong(get(row, "industryId")), clueId, "线索未关单");
            }
        });

        if (!errors.isEmpty()) {
            throw new IllegalStateException("营销线索预警部分步骤执行失败(" + errors.size() + "个): "
                    + String.join("; ", errors));
        }
        log.info("【营销线索预警】执行完成");
    }

    /**
     * 消息入队（写 ding_message，由短信/钉钉发送任务消费）
     * <p>
     * 迁移自旧系统 {@code CluesEarlyWarning.saveDingMsg()}：
     * 用户无效则跳过；expert_review / leader_review / clue_refine 三种类型按
     * message_record 去重，其余类型每次都提醒。
     */
    private void saveDingMsg(List<Map<String, Object>> rows, String type) {
        boolean needDedup = DEDUP_MESSAGE_TYPES.contains(type);
        int queued = 0;
        for (Map<String, Object> row : rows) {
            Long userId = toLong(get(row, "userId"));
            if (userId == null) {
                continue;
            }
            SysUsersVo user = sysUsersService.queryById(userId);
            if (user == null || !isValid(user)) {
                continue;
            }
            String content = str(get(row, "content"));
            if (StringUtils.isBlank(content)) {
                continue;
            }
            String dedupKey = str(get(row, "clueNum"));
            if (needDedup && StringUtils.isNotBlank(dedupKey)) {
                if (messageRecordMapper.countByRecord(userId, dedupKey, type) > 0) {
                    continue;
                }
                messageRecordMapper.insertRecord(userId, dedupKey, type);
            }
            sendDMessageService.initNoticeMessage(newUserIdList(userId), content);
            queued++;
        }
        log.info("【营销线索预警】{} 消息入队 {} 条（候选 {} 条）", type, queued, rows.size());
    }

    /**
     * 启动「领导审核」待办流程并建单
     * <p>
     * 迁移自旧系统 {@code CluesEarlyWarning.startProcessAndSaveOrder()}：
     * 处理人有效 → 建工单 → 启动流程 → 保存线索关联 → 发送提醒。
     *
     * @param userId       处理人（行业部领导 / 县分分管领导）
     * @param sendMsgInfo  提醒内容
     * @param areaId       地市ID
     * @param industryId   行业ID
     * @param clueId       线索ID
     * @param orderSubName 工单子名称（走访不合格 / 线索未关单）
     */
    private void startProcessAndSaveOrder(Long userId, String sendMsgInfo, Long areaId,
                                          Long industryId, Long clueId, String orderSubName) {
        try {
            // 下一步处理人有效才行（对齐旧系统 if(!nextuser.getValid()) return;）
            SysUsersVo user = sysUsersService.queryById(userId);
            if (user == null || !isValid(user)) {
                return;
            }

            WarningRecord record = new WarningRecord();
            record.setOrderType(ORDER_TYPE);
            record.setOrderName(new SimpleDateFormat("yyyyMMdd").format(new Date()) + "_" + orderSubName + "预警");
            record.setCustManagerId(userId);
            // 对齐旧系统 order.setCustName(Constants.ACT_PHASE_GXKD_LEADER_AUDIT_NAME)
            record.setCustName(FIRST_NODE_NAME);
            record.setAreaId(areaId);
            record.setExtraVariable("orgId", user.getOrgId());
            record.setExtraVariable("nickName", user.getNameCh());
            record.setExtraVariable("phone", user.getRelationTel());
            // 生成工单流水号（老框架 Order 构造函数即调用 OppClueCodeUtil.getOrderCode()，
            // 不生成会导致 order_number 为空 → 推 OA 待办的 PROCESS_CODE 变成 zjzqyypt-null）
            record.setExtraVariable("_needOrderNumber", true);
            // 首节点显示名（single_approval 的 manager_handle 节点名为 ${manager_handle_name}）
            record.setExtraVariable("manager_handle_name", FIRST_NODE_NAME);
            record.setExtraVariable("industryId", industryId);
            record.setExtraVariable("clueId", clueId);
            // 对齐旧系统 order.setCreator(nextuser) / order.setOrg(nextuser.getOrg())：
            // creator 决定推 OA 待办时的发起人 OA 账号（PendingSyncTodoServiceImpl 读取）
            SysUsers creator = sysUsersMapper.selectById(userId);
            if (creator != null) {
                record.setSysUser(creator);
            }

            ToppOrderBo orderBo = warningOrderCreateService.createAndStartOrder(record, PROCESS_KEY, userId,
                    (orderId, extra) -> DEAL_URL + "&orderId=" + orderId
                            + "&areaId=" + (areaId == null ? "" : areaId)
                            + "&industryId=" + (industryId == null ? "" : industryId), null);
            if (orderBo == null) {
                log.warn("【营销线索预警】{} 工单创建失败, userId={}, clueId={}", orderSubName, userId, clueId);
                return;
            }

            // 保存流程订单与线索之间的关联关系，用于待办页面根据流程订单ID查找线索详情
            groupClueOrderMapper.insertLink(orderBo.getId(), clueId, areaId, industryId, ORDER_TYPE);

            sendDMessageService.initNoticeMessage(newUserIdList(userId), user.getNameCh() + "您好," + sendMsgInfo);
            log.info("{}{}预警待办流程启动完成, orderId={}, clueId={}", ORDER_TYPE, orderSubName,
                    orderBo.getId(), clueId);
        } catch (Exception e) {
            log.error("启动任务异常：userIds【" + userId + "】msg【" + sendMsgInfo + "】", e);
        }
    }

    // ==================== 工具方法 ====================

    @FunctionalInterface
    private interface Step {
        void run();
    }

    /**
     * 单步执行并隔离异常：某一段查询失败不影响其余几段，最后统一上报失败明细
     */
    private void runStep(List<String> errors, String stepName, Step step) {
        try {
            step.run();
        } catch (Exception e) {
            errors.add(stepName + "(" + e.getMessage() + ")");
            log.error("【营销线索预警】{} 执行异常", stepName, e);
        }
    }

    /**
     * 地市参数白名单校验：该值以 ${} 方式拼入 SQL，禁止出现数字、逗号、空白以外的字符
     */
    private String normalizeAreaParam(String areaParam) {
        if (StringUtils.isBlank(areaParam)) {
            return "";
        }
        String trimmed = areaParam.trim();
        if (!trimmed.matches(AREA_PARAM_PATTERN)) {
            throw new IllegalArgumentException("地市参数格式非法（只允许数字、逗号、空白）: " + areaParam);
        }
        return trimmed;
    }

    /**
     * 对齐旧系统 SysUser.getValid()：is_valid 为 0 视为无效，其余（1 或空）视为有效
     */
    private boolean isValid(SysUsersVo user) {
        String valid = user.getIsValid();
        return valid == null || !"0".equals(valid.trim());
    }

    /** initNoticeMessage 内部会对列表做 clear()/addAll()，必须传可变 List */
    private List<Integer> newUserIdList(Long userId) {
        return new ArrayList<>(Collections.singletonList(userId.intValue()));
    }

    /** 结果集取值（对列名大小写不敏感，避免不同数据库驱动大小写差异） */
    private Object get(Map<String, Object> row, String key) {
        if (row == null) {
            return null;
        }
        if (row.containsKey(key)) {
            return row.get(key);
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            int dot = text.indexOf('.');
            if (dot > 0) {
                try {
                    return Long.valueOf(text.substring(0, dot));
                } catch (NumberFormatException ignored) {
                    // 落到下面返回 null
                }
            }
            log.warn("【营销线索预警】无法转换为数值: {}", text);
            return null;
        }
    }
}
