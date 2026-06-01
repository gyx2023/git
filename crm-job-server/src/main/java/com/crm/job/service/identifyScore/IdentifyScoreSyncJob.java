package com.crm.job.service.identifyScore;

import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.constant.RedisKeyConstants;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.dao.IdentifyScoreCustomerDo;
import com.crm.system.domain.dao.IdentifyScoreDo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import com.crm.system.service.identify.IIdentifyScoreService;
import com.crm.system.service.identify.impl.ScoreApiService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 未关联商机，表数据更新为终止
 */
@Slf4j
@Component
public class IdentifyScoreSyncJob {
    @Resource
    private ScoreApiService scoreApiService;
    @Resource
    private IIdentifyScoreService identifyScoreService;
    @Resource
    private IIdentifyScoreCustomerService iIdentifyScoreCustomerService;
    @Resource
    private ISysUserService sysUserService;


    // 评分状态常量（可抽取到枚举类）
    private static final String SCORE_STATUS_FAILED = "2";
    private static final String SCORE_STATUS_SUCCESS = "1";

    @Resource
    private RedissonClient redissonClient;

    @XxlJob("identifyScoreSyncJob")
    public void syncOppoUnbound() {
        String redisKey = RedisKeyConstants.EXECUTE_IDENTIFY_SCORE_TASK;
        RLock lock = redissonClient.getLock(redisKey);
        try {
            lock.lock(RedisKeyConstants.MAX_LOCK_TIME, TimeUnit.SECONDS);

            List<IdentifyScoreDo> identifyScoreDos = identifyScoreService.queryWaitList();
            if (identifyScoreDos == null || identifyScoreDos.isEmpty()) {
                log.info("没有待处理的评分数据");
                return;
            }

            for (IdentifyScoreDo identifyScoreDo : identifyScoreDos) {
                try {
                    SysUser sysUser = sysUserService.selectUserById(identifyScoreDo.getUploader());
                    if (sysUser == null) {
                        log.warn("未找到上传用户，用户ID: {}", identifyScoreDo.getUploader());
                        continue;
                    }

                    IdentifyScoreCustomerVo customerVo = iIdentifyScoreCustomerService.queryById(identifyScoreDo.getCustomerId());
                    if (customerVo == null) {
                        log.warn("未找到客户信息，客户ID: {}", identifyScoreDo.getCustomerId());
                        continue;
                    }

                    Map<String, Object> scoreResultMap = scoreApiService.queryFileByScore(identifyScoreDo.getFileText(), sysUser);
                    identifyScoreService.updateScoreData(
                        BeanCopyUtils.copy(customerVo, IdentifyScoreCustomerDo.class),
                        identifyScoreDo,
                        scoreResultMap,
                        SCORE_STATUS_SUCCESS
                    );
                    log.info("评分数据同步成功，客户ID: {}", identifyScoreDo.getCustomerId());
                } catch (Exception e) {
                    log.error("评分数据同步失败，客户ID: {}, 异常信息: ", identifyScoreDo.getCustomerId(), e);
                    try {
                        IdentifyScoreCustomerVo customerVo = iIdentifyScoreCustomerService.queryById(identifyScoreDo.getCustomerId());
                        if (customerVo != null) {
                            identifyScoreService.updateScoreData(
                                BeanCopyUtils.copy(customerVo, IdentifyScoreCustomerDo.class),
                                identifyScoreDo,
                                null,
                                SCORE_STATUS_FAILED
                            );
                        }
                    } catch (Exception innerException) {
                        log.error("更新评分状态为失败时发生异常，客户ID: {}", identifyScoreDo.getCustomerId(), innerException);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{} -> identifyScoreSyncJob 执行失败, errorMsg: [{}]", getClass().getSimpleName(), ex.getMessage(), ex);
        } finally {
            lock.unlock();
        }
    }
}
