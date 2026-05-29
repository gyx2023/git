package com.crm.job.service.send;

import com.crm.system.domain.DingMessage;
import com.crm.system.domain.vo.SysUsersVo;
import com.crm.system.domain.vo.send.ConOpenResult;
import com.crm.system.service.IDingMessageService;
import com.crm.system.service.ISysUsersService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 管理中台短信发送定时任务
 */
@Slf4j
@Component
public class ConOpenSendShortMsgJob implements InitializingBean, DisposableBean {

    @Resource
    private IDingMessageService dingMessageService;

    @Resource
    private ISysUsersService iSysUsersService;

    private static final int THREAD_NUM = 10;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;

    private ExecutorService executorService;

    /**
     * 初始化线程池
     */
    @Override
    public void afterPropertiesSet() {
        executorService = new ThreadPoolExecutor(
            THREAD_NUM,
            THREAD_NUM,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private int count = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "msg-send-thread-" + count++);
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 销毁线程池
     */
    @Override
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * XXL-JOB 短信发送任务
     */
    @XxlJob("send10010MsgJob")
    public void send10010MsgJob() {
        log.info("【短信发送任务】开始调度短信发送");
        XxlJobHelper.log("【短信发送任务】开始调度短信发送");

        List<DingMessage> dingMessageList = dingMessageService.findNotSendMsgs();
        if (dingMessageList == null || dingMessageList.isEmpty()) {
            log.info("【短信发送任务】暂无待发送短信");
            XxlJobHelper.log("【短信发送任务】暂无待发送短信");
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
        List<List<DingMessage>> splitLists = averageAssign(dingMessageList, THREAD_NUM);

        for (List<DingMessage> subList : splitLists) {
            executorService.submit(new SendMsgTask(subList, countDownLatch));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("【短信发送任务】短信发送任务等待中断", e);
            XxlJobHelper.handleFail("【短信发送任务】短信发送任务等待中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        }
        log.info("【短信发送任务】本次短信发送完成");
        XxlJobHelper.log("【短信发送任务】本次短信发送完成");
        XxlJobHelper.handleSuccess("【短信发送任务】本次短信发送完成");
    }

    /**
     * 短信发送线程
     */
    private class SendMsgTask implements Runnable {

        private final List<DingMessage> messages;
        private final CountDownLatch latch;

        SendMsgTask(List<DingMessage> messages, CountDownLatch latch) {
            this.messages = messages;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                if (messages == null || messages.isEmpty()) {
                    return;
                }

                for (DingMessage message : messages) {
                    sendSingleMessage(message);
                }
            } finally {
                log.info("线程 {} 短信发送完成，剩余计数 {}",
                    Thread.currentThread().getName(), latch.getCount());
                latch.countDown();
            }
        }

        private void sendSingleMessage(DingMessage message) {
            String content = message.getContent();
            String userIds = message.getUserIds();

            if (!StringUtils.hasText(userIds)) {
                return;
            }

            String[] ids = userIds.split(",");
            for (String id : ids) {
                if (!StringUtils.hasText(id)) {
                    continue;
                }
                SysUsersVo user = iSysUsersService.queryById(Long.valueOf(id.trim()));
                if (user == null || user.getRelationTel() == null) {
                    continue;
                }
                XxlJobHelper.log("【短信发送任务】短信发送用户ID：{}，短信内容：{}", id, content);
                ConOpenResult conOpenResult = ConOpenSendImpl.sendConOpenMessage(content, user.getRelationTel());
                if (conOpenResult.getCode().equals("00000")) {
                    message.setShortSend("1");
                    log.info("【短信发送任务】发送成功：{}", conOpenResult.getMsg());
                    XxlJobHelper.log("【短信发送任务】发送成功：{}", conOpenResult.toString());
                } else {
                    message.setShortSend("2");
                    log.error("【短信发送任务】发送失败：{}", conOpenResult.getMsg());
                    XxlJobHelper.log("【短信发送任务】发送失败：{}", conOpenResult.toString());
                }
            }

            message.setShortSendTime(new Date());
            dingMessageService.saveOrUpdate(message);
        }
    }

    /**
     * 平均拆分 List
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();

        int remainder = source.size() % n;
        int size = source.size() / n;
        int offset = 0;

        for (int i = 0; i < n; i++) {
            int from = i * size + offset;
            int to;

            if (remainder > 0) {
                to = from + size + 1;
                remainder--;
                offset++;
            } else {
                to = from + size;
            }

            result.add(source.subList(from, to));
        }
        return result;
    }
}
