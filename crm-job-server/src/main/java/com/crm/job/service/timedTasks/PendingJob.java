package com.crm.job.service.timedTasks;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.service.IPendingService;
import com.crm.system.service.IPendingSyncTodoService;
import com.crm.system.service.sys.ISysParamService;
import com.oceanbase.jdbc.internal.util.StringCacheUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 待办推送门户（pendingJob）
 */
@Slf4j
@Component
public class PendingJob {

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private IPendingService pendingService;

    @Resource
    private IPendingSyncTodoService pendingSyncTodoService;

    private String addUrl;
    private String updateUrl;
    private String appId;
    private String token;

    /**
     * 从Redis缓存加载待办推送配置参数（sys_param表），避免每次改数据库配置需要重启
     */
    private void loadPendingConfig() {
        try {
            Map<String, String> param = sysParamService.findValueMapsByType("pending_job_prod");
            addUrl = param.get("addUrl");
            updateUrl = param.get("updateUrl");
            appId = param.get("appId");
            token = param.get("token");
        } catch (Exception e) {
            log.error("待办同步参数加载失败", e);
        }
    }

    @XxlJob(value = "pendingJob")
    public void start() {
        long startTime = System.currentTimeMillis();
        //加载配置
        loadPendingConfig();
        XxlJobHelper.log("====== 待办统一调度开始执行（按ID逐条执行）======");
        log.info("====== 待办统一调度开始执行（按ID逐条执行）======");
        String param = "5";//XxlJobHelper.getJobParam();
        // 1. 获取按 ID 排序好的 a + u 列表 默认取30条数据
        List<Pending> pendingList = pendingService.getPendingList(StringUtils.isEmpty( param)? "30": param);

        // 2. 逐条处理：a 新增，u 更新
        if (pendingList != null && !pendingList.isEmpty()) {
            for (Pending pending : pendingList) {
                try {
                    if ("a".equals(pending.getOperatorType())) {
                        pendingService.pushAddPending(pending, addUrl, appId, token);
                    } else if ("u".equals(pending.getOperatorType())) {
                        pendingService.pushUpdatePendingStatus(pending, updateUrl, appId, token);
                    }
                } catch (Exception e) {
                    log.error("单条待办处理失败，id=" + pending.getId(), e);
                }
            }
        }

        log.info("====== 主待办处理完成，开始同步集团运营平台 ======");
        XxlJobHelper.log("====== 主待办处理完成，开始同步集团运营平台 ======");

        // 同步给集团运营平台待办
        List<PendingGroupTodo> list = pendingService.getGroupTodoList();
        XxlJobHelper.log("待同步集团待办{}条", list.size());
        for (PendingGroupTodo task : list) {
            try {
                pendingSyncTodoService.syncTodoTask(task);
            } catch (Exception e) {
                log.error("同步集团待办任务失败, taskId:{}", task.getId(), e);
                XxlJobHelper.log("同步集团待办任务失败, taskId:{}", task.getId());
            }
            // 间隔1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
        log.info("待办同步Job执行完成,耗时:{}ms", costTime);
        XxlJobHelper.log("待办同步Job执行完成,耗时:{}ms", costTime);
    }
}
