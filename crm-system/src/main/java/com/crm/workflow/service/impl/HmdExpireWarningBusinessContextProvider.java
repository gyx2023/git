package com.crm.workflow.service.impl;

import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IHmdExpireWarningDetailService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.domain.vo.WfBusinessSection;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 用户红名单到期预警（hmd_warn）
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HmdExpireWarningBusinessContextProvider extends AbstractWarningBusinessContextProvider {

    /** 处理页明细首屏页大小（与前端默认分页大小保持一致） */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 领导节点（audit_handle）的区块标题；经理节点沿用 {@link #getSectionTitle()} */
    private static final String LEADER_SECTION_TITLE = "红名单到期审核";

    @Autowired
    private IHmdExpireWarningDetailService detailService;

    @Override
    public boolean supports(String procDefKey, String source, ToppOrderVo order) {
        // hmd_warn_approval 为新框架双节点流程；保留 single/double 兼容历史（改流程定义前启动的）在途工单
        if (!("hmd_warn_approval".equals(procDefKey)
                || "single_approval".equals(procDefKey)
                || "double_approval".equals(procDefKey))) {
            return false;
        }
        return order != null && getOrderTypes().contains(order.getOrderType());
    }

    @Override
    protected Set<String> getOrderTypes() {
        return Collections.singleton("hmd_warn");
    }

    @Override
    protected String getSectionKey() {
        return "records";
    }

    @Override
    protected String getSectionTitle() {
        return "红名单到期预警详情";
    }

    @Override
    protected String getReminderText() {
        return "红名单即将到期，请及时联系客户续约";
    }

    /**
     * 区块标题按当前任务节点区分：领导节点（audit_handle）→ 红名单到期审核，经理节点 → {@link #getSectionTitle()}。
     * 基类先 add(sections) 再调 {@link #enrichExtraData}，扩展点无法改标题，故在此处（super 之后）改写。
     */
    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey, ToppOrderVo order) {
        WfBusinessContext ctx = super.provide(procInsId, source, procDefKey, order);
        if (ctx != null && isLeaderNodeCurrentTask(procInsId)) {
            List<WfBusinessSection> sections = ctx.getSections();
            if (sections != null) {
                for (WfBusinessSection section : sections) {
                    if (getSectionKey().equals(section.getSectionKey())) {
                        section.setTitle(LEADER_SECTION_TITLE);
                        break;
                    }
                }
            }
        }
        return ctx;
    }

    /** 当前任务是否在领导节点（taskDefinitionKey == audit_handle）；无当前任务（已结束）或查询异常返回 false */
    private boolean isLeaderNodeCurrentTask(String procInsId) {
        try {
            Task currentTask = findCurrentTask(procInsId);
            return currentTask != null && "audit_handle".equals(currentTask.getTaskDefinitionKey());
        } catch (Exception e) {
            log.warn("查询当前任务节点失败: procInsId={}", procInsId, e);
            return false;
        }
    }

    @Override
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        Long orderId = order != null ? order.getId() : null;

        // 客户级表头；失败给空 Map，保证 extraData 非空
        Map<String, Object> header = new HashMap<>();
        try {
            Map<String, Object> h = detailService.queryHeader(orderId);
            if (h != null) {
                header = h;
            }
        } catch (Exception e) {
            log.warn("查询红名单预警表头失败: orderId={}", orderId, e);
        }
        extraData.put("header", header);

        List<Map<String, Object>> records = new ArrayList<>();
        long recordsTotal = 0L;
        try {
            TableDataInfo<Map<String, Object>> firstPage =
                detailService.queryFirstPageRecords(orderId, DEFAULT_PAGE_SIZE);
            if (firstPage != null && firstPage.getRows() != null) {
                records = firstPage.getRows();
                recordsTotal = firstPage.getTotal();
            }
        } catch (Exception e) {
            log.warn("查询红名单预警明细失败: orderId={}", orderId, e);
        }
        extraData.put("records", records);
        extraData.put("recordsTotal", recordsTotal);

        // 领导是否已审过（本流程历史中是否出现过 audit_handle 任务）——二次“不延期”据此隐藏选领导下拉
        extraData.put("hmdLeaderReviewed", hasHmdLeaderReviewed(order));
    }

    /**
     * 历史任务中出现过 taskDefinitionKey='audit_handle' 即视为“领导已审过”。
     * 依赖 ToppOrderVo.piId 作为 Flowable 流程实例ID；piId 为空或查询异常一律返回 false。
     */
    private boolean hasHmdLeaderReviewed(ToppOrderVo order) {
        try {
            String piId = order != null ? order.getPiId() : null;
            if (piId == null) {
                return false;
            }
            long cnt = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(piId)
                .taskDefinitionKey("audit_handle")
                .count();
            return cnt > 0;
        } catch (Exception e) {
            log.warn("查询 audit_handle 历史任务失败: orderId={}", order != null ? order.getId() : null, e);
            return false;
        }
    }
}
