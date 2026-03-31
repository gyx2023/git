package com.crm.flowable.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * @author hhz49
 */
@Component(value = "doSomethingExecutionListener")
public class DoSomethingExecutionListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        System.out.println(execution.getCurrentActivityId());
        System.out.println("执行执行监听器...");
    }
}
