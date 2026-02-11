package com.crm.job.service.identifyScore;

import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.identify.IdentifyScoreCustomerDo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import com.crm.system.service.identify.IIdentifyScoreService;
import com.crm.system.service.identify.impl.ScoreApiService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

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

    @XxlJob("identifyScoreSyncJob")
    public void syncOppoUnbound() {
        List<IdentifyScoreDo> identifyScoreDos = identifyScoreService.queryWaitList();
        for (IdentifyScoreDo identifyScoreDo : identifyScoreDos) {
            SysUser sysUser = sysUserService.selectUserById(identifyScoreDo.getUploader());
            IdentifyScoreCustomerVo customerVo = iIdentifyScoreCustomerService.queryById(identifyScoreDo.getCustomerId());
            try {
                Map<String, Object> scoreResultMap = scoreApiService.queryFileByScore(identifyScoreDo.getFileText(), sysUser);
                identifyScoreService.updateScoreData(BeanCopyUtils.copy(customerVo, IdentifyScoreCustomerDo.class), identifyScoreDo, scoreResultMap, "1");
            } catch (Exception e) {
                String errorMsg = "三清单导入任务异常，文件URL: ";
                log.error(errorMsg, e);
                identifyScoreService.updateScoreData(BeanCopyUtils.copy(customerVo, IdentifyScoreCustomerDo.class), identifyScoreDo, null, SCORE_STATUS_FAILED);
            }
            log.info("评分数据同步完成");
        }
    }
}
