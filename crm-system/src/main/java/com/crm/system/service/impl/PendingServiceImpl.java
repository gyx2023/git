package com.crm.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.PendingGroupTodoMapper;
import com.crm.system.mapper.PendingMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import com.crm.workflow.service.impl.OldWorkflowQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 待办Service业务层处理
 */
@Slf4j
@Service
@Transactional
public class PendingServiceImpl implements IPendingService {

    @Resource
    private PendingMapper pendingMapper;

    @Resource
    private PendingGroupTodoMapper pendingGroupTodoMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private OldWorkflowQueryService oldWorkflowQueryService;

    private static final SimplePropertyPreFilter ADD_PENDING_FILTER = new SimplePropertyPreFilter(Pending.class,
            "pendingCode", "pendingTitle", "pendingDate", "pendingUserID", "pendingURL", "pendingStatus",
            "pendingLevel", "pendingCityCode", "pendingSourceUserID", "pendingSource", "pendingNote");

    private static final SimplePropertyPreFilter UPDATE_PENDING_FILTER = new SimplePropertyPreFilter(Pending.class,
            "pendingCode", "pendingStatus", "lastUpdateDate", "pendingURL", "pendingDate", "pendingTitle",
            "pendingSourceUserID", "pendingSource", "pendingLevel", "pendingNote");

    @Override
    public List<Pending> getPendingList(String limit) {
        return pendingMapper.selectPendingList(limit);
    }

    @Override
    public void pushAddPending(Pending pending, String addUrl, String appId, String token) {
        if (pending == null) {
            return;
        }
        try {
            addPending(pending, addUrl, appId, token);
        } catch (Exception e) {
            pending.setSendTime(new Date());
            pending.setResponseMsg(e.getMessage() == null ? null : e.getMessage().substring(0, Math.min(e.getMessage().length(), 100)));
            updateFailTimes(pending);
            log.error("推送新增待办异常", e);
        }
    }

    private void addPending(Pending pending, String addUrl, String appId, String token) {
        if (!initPendingUrl(pending)) {
            return;
        }
        String url = addUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(Collections.singletonList(pending), ADD_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("新增待办请求URL:{}", url);
        String result = HttpUtil.post(url, jsonStr, 60000);
        log.info("新增待办请求URL:{}, 返回结果:{}", url, result);

        if (StringUtils.isEmpty(result)) {
            updateFailTimes(pending);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending p = findByPendingCodeAndType(pendingCode, "a");
                p.setResponseMsg(msg);
                log.info("待办新增返回值:{} 待办ID={}", msg, p.getId());
                if (msg.startsWith("100-")) {
                    p.setSendStatus("1");
                    p.setSendTime(new Date());
                } else {
                    p.setSendStatus("1");
                    p.setFailTimes(3);
                    p.setResponseMsg(msg);
                }
                saveOrUpdate(p);
            }
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private boolean initPendingUrl(Pending pending) {
        log.info("{}开始处理", pending.getId());
        if (StringUtils.isNotEmpty(pending.getPendingURL())) {
            log.info("{}处理结束", pending.getId());
            return true;
        }
        try {
            Map<String, Object> task = oldWorkflowQueryService
                    .queryOldHistoricTaskByTaskId(pending.getPendingCode());
            if (task == null) {
                log.info("订单" + pending.getId() + "task为空");
                pending.setFailTimes(3);
                pending.setResponseMsg("订单" + pending.getId() + "task为空");
                saveOrUpdate(pending);
                return false;
            }
            String taskDefKey = (String) task.get("taskDefKey");

            Map<String, Object> dealUrl = oldWorkflowQueryService
                    .queryOldHistoricVariable(pending.getPendingCode(), taskDefKey + "_dealUrl");
            if (dealUrl == null) {
                log.info("订单" + pending.getId() + "varinst表dealUrl为空");
                pending.setFailTimes(3);
                pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                saveOrUpdate(pending);
                return false;
            }
            String backlog = sysParamService.findValueByCode("Backlog");
            String dealUrlStr = dealUrl.get("textValue").toString().replace("?", "&");
            pending.setPendingURL(backlog + dealUrlStr + "&taskId=" + pending.getPendingCode());

            String procInsId = (String) task.get("procInsId");
            List<Map<String, Object>> hisList = oldWorkflowQueryService
                    .queryOldHistoricTaskInsts(procInsId);
            hisList.sort((a, b) -> {
                long idA = Long.parseLong((String) a.get("id"));
                long idB = Long.parseLong((String) b.get("id"));
                return Long.compare(idB, idA);
            });
            Map<String, Object> prevTask = null;
            long currentTaskId = Long.parseLong((String) task.get("id"));
            for (Map<String, Object> his : hisList) {
                if (Long.parseLong((String) his.get("id")) < currentTaskId) {
                    prevTask = his;
                }
            }
            int userId;
            if (prevTask != null) {
                userId = Integer.parseInt(((String) prevTask.get("assignee")).replaceAll(",", ""));
            } else {
                userId = Integer.parseInt(((String) task.get("assignee")).replaceAll(",", ""));
            }

            SysUsers prev = sysUsersMapper.selectById(userId);
            if (prev != null) {
                if (StringUtils.isNotEmpty(prev.getNameCh())) {
                    pending.setPendingSource(prev.getNameCh());
                } else {
                    pending.setPendingSource("未知用户");
                }
                try {
                    pending.setPendingSourceUserID(prev.getEmail().split("@")[0]);
                } catch (Exception e) {
                    pending.setPendingSourceUserID("未知用户");
                    log.info("{}未知用户异常", pending.getId());
                }
            } else {
                pending.setPendingSource("未知用户");
                pending.setPendingSourceUserID("未知用户");
            }
            saveOrUpdate(pending);
            log.info("{}处理结束", pending.getId());
            return true;
        } catch (Exception e) {
            log.info("(报错后)订单{}dealUrl为空", pending.getId());
            pending.setFailTimes(3);
            pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
            log.error(e.getMessage(), e);
            saveOrUpdate(pending);
            return false;
        }
    }

    @Override
    public void pushUpdatePendingStatus(Pending pending, String updateUrl, String appId, String token) {
        if (pending == null) {
            return;
        }
        try {
            if (checkUpdateData(pending)) {
                updatePendingStatus(pending, updateUrl, appId, token);
            }
        } catch (Exception e) {
            updateFailTimes(pending);
            log.error("推送待办状态更新异常", e);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private boolean checkUpdateData(Pending pending) {
        Pending a = findByPendingCodeAndType(pending.getPendingCode(), "a");
        if (a == null) {
            pending.setFailTimes(3);
            pending.setResponseMsg("a记录找不到");
            saveOrUpdate(pending);
            return false;
        }
        if (a.getResponseMsg() != null && a.getResponseMsg().startsWith("100-")) {
            return true;
        } else {
            pending.setFailTimes(3);
            pending.setResponseMsg("a发送失败");
            saveOrUpdate(pending);
            return false;
        }
    }

    private void updatePendingStatus(Pending pending, String updateUrl, String appId, String token) {
        log.info("开始推送OA待办更新");
        String url = updateUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(Collections.singletonList(pending), UPDATE_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("待办更新地址：{}，待办更新请求数据:{}", url, jsonStr);
        String result = HttpUtil.post(url, jsonStr, 60000);
        log.info("推送OA待办更新,响应数据:{}", result);

        if (StringUtils.isEmpty(result)) {
            updateFailTimes(pending);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending p = findByPendingCodeAndType(pendingCode, "u");
                if (p == null) {
                    return;
                }
                p.setResponseMsg(msg);
                log.info("待办更新返回值:{} 待办ID={}", msg, p.getId());
                if (msg.startsWith("100-")) {
                    p.setSendStatus("1");
                    p.setSendTime(new Date());
                } else {
                    p.setSendStatus("1");
                    p.setResponseMsg(msg);
                    p.setFailTimes(3);
                }
                saveOrUpdate(p);
            }
        }
    }

    @Override
    public Pending findByPendingCodeAndType(String pendingCode, String operatorType) {
        LambdaQueryWrapper<Pending> wrapper = new LambdaQueryWrapper<Pending>()
                .eq(Pending::getPendingCode, pendingCode)
                .eq(Pending::getOperatorType, operatorType)
                .last("LIMIT 1");
        return pendingMapper.selectOne(wrapper);
    }

    @Override
    public void saveOrUpdate(Pending pending) {
        if (pending.getId() != null) {
            pendingMapper.updateById(pending);
        } else {
            pendingMapper.insert(pending);
        }
    }

    @Override
    public int updateFailTimes(Pending pending) {
        if (pending == null) {
            return 0;
        }
        UpdateWrapper<Pending> wrapper = new UpdateWrapper<>();
        wrapper.eq("id_", pending.getId());
        wrapper.set("fail_times",3);
        wrapper.set("send_status","2");
        return pendingMapper.update(pending, wrapper);
    }

    @Override
    public List<PendingGroupTodo> getGroupTodoList() {
        return pendingGroupTodoMapper.selectList(
                new LambdaQueryWrapper<PendingGroupTodo>()
                        .eq(PendingGroupTodo::getSendStatus, "0")
                        .or(w -> w.eq(PendingGroupTodo::getSendStatus, "2")
                                .lt(PendingGroupTodo::getFailTimes, 3))
                        .orderByAsc(PendingGroupTodo::getId)
                        .last("LIMIT 100")
        );
    }

    @Override
    public void updateGroupTodoTask(PendingGroupTodo pendingTodo) {
        pendingTodo.setSendTime(new Date());
        pendingGroupTodoMapper.updateById(pendingTodo);
    }
}
