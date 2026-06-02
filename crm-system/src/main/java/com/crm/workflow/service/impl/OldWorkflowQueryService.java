package com.crm.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import com.crm.workflow.mapper.OldActivitiMapper;
import com.crm.workflow.domain.vo.WfTaskVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老系统 Activiti 工作流查询服务
 * 直连老系统的 ACT_ 表（同库，仅表名前缀不同），
 * 用于新老系统并行期间查询老系统历史流程数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OldWorkflowQueryService {

    private final OldActivitiMapper oldActivitiMapper;

    /**
     * 查询老系统待办列表，返回与 WfTaskVo 兼容的数据
     */
    public List<Map<String, Object>> queryOldTodoList(String userId) {
        return oldActivitiMapper.selectOldTodoList(userId);
    }

    /**
     * 查询老系统待办数量
     */
    public long queryOldTodoCount(String userId) {
        return oldActivitiMapper.selectOldTodoCount(userId);
    }

    /**
     * 查询老系统已办列表
     */
    public List<Map<String, Object>> queryOldFinishedList(String userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return oldActivitiMapper.selectOldFinishedList(userId, offset, pageSize);
    }

    /**
     * 查询老系统已办数量
     */
    public long queryOldFinishedCount(String userId) {
        return oldActivitiMapper.selectOldFinishedCount(userId);
    }

    /**
     * 查询老系统我发起的流程
     */
    public List<Map<String, Object>> queryOldOwnList(String userId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return oldActivitiMapper.selectOldOwnList(userId, offset, pageSize);
    }

    /**
     * 查询老系统我发起的数量
     */
    public long queryOldOwnCount(String userId) {
        return oldActivitiMapper.selectOldOwnCount(userId);
    }

    /**
     * 判断是否为老系统的流程实例
     */
    public boolean isOldProcess(String procInsId) {
        if (procInsId == null) return false;
        Map<String, Object> procInst = oldActivitiMapper.selectOldProcInst(procInsId);
        return procInst != null && !procInst.isEmpty();
    }

    /**
     * 查询老系统流程详情
     */
    public Map<String, Object> queryOldProcessDetail(String procInsId) {
        return oldActivitiMapper.selectOldProcInst(procInsId);
    }

    /**
     * 查询老系统流程活动节点（处理轨迹）
     */
    public List<Map<String, Object>> queryOldHistoricActivityInsts(String procInsId) {
        return oldActivitiMapper.selectOldHistoricActivityInsts(procInsId);
    }

    /**
     * 查询老系统流程历史任务
     */
    public List<Map<String, Object>> queryOldHistoricTaskInsts(String procInsId) {
        return oldActivitiMapper.selectOldHistoricTaskInsts(procInsId);
    }

    /**
     * 查询老系统流程变量
     */
    public List<Map<String, Object>> queryOldProcVariables(String procInsId) {
        return oldActivitiMapper.selectOldProcVariables(procInsId);
    }

    /**
     * 获取老系统BPMN XML
     * 从 ACT_GE_BYTEARRAY 中查找
     */
    public String getOldBpmnXml(String procInsId) {
        List<Map<String, Object>> resources = oldActivitiMapper.selectOldBpmnResources(procInsId);
        if (resources == null || resources.isEmpty()) {
            return null;
        }
        // 查找BPMN XML文件（排除PNG图片）
        for (Map<String, Object> resource : resources) {
            String name = Convert.toStr(resource.get("NAME_"));
            byte[] bytes = (byte[]) resource.get("BYTES_");
            if (name != null && bytes != null
                    && (name.endsWith(".bpmn") || name.endsWith(".bpmn20.xml"))) {
                return new String(bytes);
            }
        }
        // 如果没有.bpmn文件，尝试使用XML二进制数据
        for (Map<String, Object> resource : resources) {
            byte[] bytes = (byte[]) resource.get("BYTES_");
            if (bytes != null && bytes.length > 0) {
                return new String(bytes);
            }
        }
        return null;
    }

    /**
     * 获取老系统流程图图片输入流
     */
    public InputStream getOldProcessDiagram(String procInsId) {
        List<Map<String, Object>> resources = oldActivitiMapper.selectOldBpmnResources(procInsId);
        if (resources == null || resources.isEmpty()) {
            return null;
        }
        for (Map<String, Object> resource : resources) {
            String name = Convert.toStr(resource.get("NAME_"));
            byte[] bytes = (byte[]) resource.get("BYTES_");
            if (name != null && bytes != null && name.endsWith(".png")) {
                return new ByteArrayInputStream(bytes);
            }
        }
        return null;
    }

    /**
     * 查询老系统流程审批意见
     */
    public List<Map<String, Object>> queryOldComments(String procInsId) {
        return oldActivitiMapper.selectOldComments(procInsId);
    }

    /**
     * 查询老系统参与者
     */
    public List<Map<String, Object>> queryOldIdentityLinks(String procInsId) {
        return oldActivitiMapper.selectOldIdentityLinks(procInsId);
    }

    /**
     * 查询老系统流程定义 Key
     */
    public String queryOldProcDefKey(String procInsId) {
        return oldActivitiMapper.selectOldProcDefKey(procInsId);
    }
}
