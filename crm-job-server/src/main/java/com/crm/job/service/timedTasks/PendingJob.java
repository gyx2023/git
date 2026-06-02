package com.crm.job.service.timedTasks;

import com.crm.job.service.timedTasks.services.IPendingSyncTodoService;
import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private static String addUrl;
    private static String updateUrl;
    private static String appId;
    private static String token;

    @PostConstruct
    public void initStaticParam() {
        try {
            Map<String, String> param = sysParamService.findValueMapsByType("pending_job_prod");
            addUrl = param.get("addUrl");
            updateUrl = param.get("updateUrl");
            appId = param.get("appId");
            token = param.get("token");
            log.info("待办同步参数初始化完成");
        } catch (Exception e) {
            log.error("待办同步参数初始化失败", e);
        }
    }

    @XxlJob(value = "pendingJob")
    public void start() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("开始执行待办同步Job");
        log.info("开始执行待办同步Job");

        try {
            List<Pending> addList = new ArrayList<>();
            List<Pending> updateList = new ArrayList<>();
            List<Pending> pending = pendingService.getPendingList();

            for (Pending p : pending) {
                if ("a".equals(p.getOperatorType())) {
                    addList.add(p);
                } else if ("u".equals(p.getOperatorType())) {
                    updateList.add(p);
                }
            }

            log.info("待推送新增待办{}条, 待更新待办{}条", addList.size(), updateList.size());
            XxlJobHelper.log("待推送新增待办{}条, 待更新待办{}条", addList.size(), updateList.size());

            // 推送新增待办
            pendingService.pushAddPending(addList, addUrl, appId, token);
            // 推送待办状态更新
            pendingService.pushUpdatePendingStatus(updateList, updateUrl, appId, token);

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

        } catch (Exception e) {
            log.error("待办同步Job执行异常", e);
            XxlJobHelper.log("待办同步Job执行异常:{}", e.getMessage());
        }
    }

    /**
     * 待办历史数据迁移Job
     */
    @XxlJob(value = "moveHistoryPendingJob")
    public void moveHistory() {
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("开始执行待办历史数据迁移Job");
        log.info("开始执行待办历史数据迁移Job");

        try {
            pendingService.movePending();

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("待办历史数据迁移Job执行完成,耗时:{}ms", costTime);
            XxlJobHelper.log("待办历史数据迁移Job执行完成,耗时:{}ms", costTime);

        } catch (Exception e) {
            log.error("待办历史数据迁移Job执行异常", e);
            XxlJobHelper.log("待办历史数据迁移Job执行异常:{}", e.getMessage());
        }
    }
}
