package com.crm.flowable.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * @author hhz49
 */
@Slf4j
@Component(value = "doSomethingExecutionListener")
public class DoSomethingExecutionListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        log.info(execution.getCurrentActivityId());
        log.info("执行执行监听器...");
    }
}
