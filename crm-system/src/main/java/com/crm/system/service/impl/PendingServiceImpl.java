package com.crm.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public List<Pending> getPendingList() {
        return pendingMapper.selectPendingList();
    }

    @Override
    public void pushAddPending(List<Pending> addList, String addUrl, String appId, String token) {
        if (addList == null || addList.isEmpty()) {
            return;
        }
        //推送待办新增
        //每次推送15条新增  若不超过15条直接推送
        try {
            int sendNums = addList.size() / 15 + 1;
            for (int i = 0; i < sendNums; i++) {
                List<Pending> newList;
                if ((i + 1) == sendNums) {
                    newList = new ArrayList<>(addList.subList(i * 15, addList.size()));
                } else {
                    newList = new ArrayList<>(addList.subList(i * 15, (i + 1) * 15));
                }
                addPending(newList, addUrl, appId, token);
            }
        } catch (Exception e) {
            log.error("推送新增待办异常", e);
        }
    }

    private void addPending(List<Pending> pendingList, String addUrl, String appId, String token) {
        // 第二个job时 第一个job的请求还在超时 数据库未更新会重复
        // 初始化还需获取一些缺失字段
        initPendingUrl(pendingList);
        if (pendingList.isEmpty()) {
            return;
        }
        String url = addUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(pendingList, ADD_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("新增待办请求URL:{}", url);
        String result = HttpUtil.post(url, jsonStr, 60000);
        log.info("新增待办请求URL:{}, 返回结果:{}", url, result);

        if (StringUtils.isEmpty(result)) {
            // 3次超时
            updateFailTimes(pendingList);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending pending = findByPendingCodeAndType(pendingCode, "a");
                pending.setResponseMsg(msg);
                // 示例 {"19935":"203-新增待办参数不正确"}
                log.info("待办新增返回值:{} 待办ID={}", msg, pending.getId());
                // 100 成功 其它 失败
                if (msg.startsWith("100-")) {
                    // 发送成功
                    pending.setSendStatus("1");
                    pending.setSendTime(new Date());
                } else {
                    // 发送失败
                    pending.setSendStatus("1");
                    //发送失败不再进行尝试
                    pending.setFailTimes(3);
                    pending.setResponseMsg(msg);
                }
                saveOrUpdate(pending);
            }
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void initPendingUrl(List<Pending> pendingList) {
        Iterator<Pending> it = pendingList.iterator();
        while (it.hasNext()) {
            Pending pending = it.next();
            log.info("{}开始处理", pending.getId());
            if (StringUtils.isNotEmpty(pending.getPendingURL())) {
                log.info("{}处理结束", pending.getId());
                continue;
            }
            try {
                // 查询历史任务实例（老流程）
                Map<String, Object> task = oldWorkflowQueryService
                        .queryOldHistoricTaskByTaskId(pending.getPendingCode());
                if (task == null) {
                    it.remove();
                    log.info("订单" + pending.getId() + "task为空");
                    pending.setFailTimes(3);
                    pending.setResponseMsg("订单" + pending.getId() + "task为空");
                    saveOrUpdate(pending);
                    continue;
                }
                String taskDefKey = (String) task.get("taskDefKey");

                // 查询流程变量 dealUrl（老流程）
                Map<String, Object> dealUrl = oldWorkflowQueryService
                        .queryOldHistoricVariable(pending.getPendingCode(), taskDefKey + "_dealUrl");
                if (dealUrl == null) {
                    it.remove();
                    log.info("订单" + pending.getId() + "varinst表dealUrl为空");
                    pending.setFailTimes(3);
                    pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                    saveOrUpdate(pending);
                    continue;
                }
                //待办处理页面地址
                String backlog = sysParamService.findValueByCode("Backlog");
                //举例 /order/toOrderDeal&urlType=deal&orderId=758760
                String dealUrlStr = dealUrl.get("textValue").toString().replace("?", "&");
                pending.setPendingURL(backlog + dealUrlStr + "&taskId=" + pending.getPendingCode());

                // 查询上一步处理人（老流程）
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
                        saveOrUpdate(pending);
                    }
                } else {
                    pending.setPendingSource("未知用户");
                    pending.setPendingSourceUserID("未知用户");
                }
                saveOrUpdate(pending);
            } catch (Exception e) {
                it.remove();
                log.info("(报错后)订单{}dealUrl为空", pending.getId());
                pending.setFailTimes(3);
                pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                log.error(e.getMessage(), e);
                saveOrUpdate(pending);
            }
            log.info("{}处理结束", pending.getId());
        }
    }

    @Override
    public void pushUpdatePendingStatus(List<Pending> updateList, String updateUrl, String appId, String token) {
        if (updateList == null || updateList.isEmpty()) {
            return;
        }

        try {
            //去掉无效数据
            checkUpdateData(updateList);

            int sendNums = updateList.size() / 15 + 1;
            for (int i = 0; i < sendNums; i++) {
                List<Pending> newList;
                if ((i + 1) == sendNums) {
                    newList = new ArrayList<>(updateList.subList(i * 15, updateList.size()));
                } else {
                    newList = new ArrayList<>(updateList.subList(i * 15, (i + 1) * 15));
                }
                updatePendingStatus(newList, updateUrl, appId, token);
            }
        } catch (Exception e) {
            log.error("推送待办状态更新异常", e);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void checkUpdateData(List<Pending> updateList) {
        //待办状态更新
        updateList.removeIf(pending -> {
            Pending a = findByPendingCodeAndType(pending.getPendingCode(), "a");
            if (a == null) {
                pending.setFailTimes(3);
                pending.setResponseMsg("a记录找不到");
                saveOrUpdate(pending);
                return true;
            }
            if (a.getResponseMsg() != null && a.getResponseMsg().startsWith("100-")) {
                return false;
            } else {
                pending.setFailTimes(3);
                pending.setResponseMsg("a发送失败");
                saveOrUpdate(pending);
                return true;
            }
        });
    }

    private void updatePendingStatus(List<Pending> pendingList, String updateUrl, String appId, String token) {
        // 第二个job时 第一个job的请求还在超时 数据库未更新会重复
        // 初始化还需获取一些缺失字段
        //init(pendingList);
        log.info("开始推送OA待办更新,本次推送{}条数据", pendingList.size());
        String url = updateUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(pendingList, UPDATE_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("待办更新地址：{}，待办更新请求数据:{}", url, jsonStr);
        String result = HttpUtil.post(url, jsonStr, 60000);
        log.info("推送OA待办更新,响应数据:{}", result);

        if (StringUtils.isEmpty(result)) {
            // 3次超时
            updateFailTimes(pendingList);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending pending = findByPendingCodeAndType(pendingCode, "u");
                if (pending == null) {
                    return;
                }
                pending.setResponseMsg(msg);
                log.info("待办更新返回值:{} 待办ID={}", msg, pending.getId());
                // 示例 {"19935":"203-新增待办参数不正确"}
                // 100 成功 其它 失败
                if (msg.startsWith("100-")) {
                    // 发送成功
                    pending.setSendStatus("1");
                    pending.setSendTime(new Date());
                } else {
                    // 发送失败
                    pending.setSendStatus("1");
                    pending.setResponseMsg(msg);
                    pending.setFailTimes(3);
                }
                saveOrUpdate(pending);
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
    public int updateFailTimes(List<Pending> pendingList) {
        if (pendingList.size() == 0) {
            return 0;
        }
        Map<String, Object> param = new HashMap<>();
        List<Long> integerList = new ArrayList<>();
        for (Pending p : pendingList) {
            integerList.add(p.getId());
        }
        param.put("ids", integerList);
        return pendingMapper.updateFailTimes(param);
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
        pendingGroupTodoMapper.updateById(pendingTodo);
    }
}
