package com.crm.flowable.listener;

import com.crm.common.utils.spring.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author hhz49
 */
@Component(value = "setAssigneeTaskListener")
public class SetAssigneeTaskListener implements TaskListener {

    private TaskService taskService = SpringUtils.getBean(TaskService.class);
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("执行任务监听器...");
        String processInstanceId = delegateTask.getProcessInstanceId();
        String processDefinitionId = delegateTask.getProcessDefinitionId();
        String taskId = delegateTask.getId();
        //表单里面的值,相关逻辑可以根据自己需要进行处理
//        String formvalue = delegateTask.getVariable("assigneeId").toString().trim();
//
//        if (StringUtils.equals(formvalue, "admin")) {
//            newAssignee = "admin";
//        }
//        else if (StringUtils.equals(formvalue, "zhangsan")) {
//            newAssignee = "zhangsan";
//        }
//        else {
//            newAssignee = "jeecg";
//        }
        Map<String, Object> map = taskService.getVariables(taskId);
        System.out.println("task-variables:"+map);
        taskService.setAssignee(taskId, map.get("userList").toString());

//        map.put("SetAssigneeTaskListener", newAssignee);
//        taskService.setVariables(taskId, map);


    }
}
