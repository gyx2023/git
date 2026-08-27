package com.crm.job.service.timedTasks;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.system.service.send.impl.ConOpenSendImpl;
import com.crm.system.domain.dao.EarlyWarningProcessingConfigDo;
import com.crm.system.mapper.EarlyWarningProcessingConfigMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警提醒
 *
 * @author crm
 */
@Slf4j
@Component
public class EarlyWarningProcessingJob {

    /** 查询结果"tongguo"表示不需要预警 */
    private static final String PASS_TAG = "tongguo";

    @Resource
    private EarlyWarningProcessingConfigMapper configMapper;

    @Resource
    private ConOpenSendImpl conOpenSendImpl;

    @XxlJob(value = "earlyWarningProcessing")
    public void doWarning() {
        long startTime = System.currentTimeMillis();
        log.info("开始预警提醒");
        XxlJobHelper.log("开始预警提醒");
        try {
            earlyWarningProcessing();
        } catch (Exception e) {
            log.error("本次预警提醒处理异常", e);
            XxlJobHelper.log("本次预警提醒处理异常:{}", e.getMessage());
            return;
        }
        long costTime = System.currentTimeMillis() - startTime;
        log.info("本次预警提醒处理完成, 耗时:{}ms", costTime);
        XxlJobHelper.log("本次预警提醒处理完成, 耗时:{}ms", costTime);
    }

    public void earlyWarningProcessing() {
        // 查询所有有效的预警配置
        QueryWrapper<EarlyWarningProcessingConfigDo> wrapper = new QueryWrapper<>();
        wrapper.eq("is_valid", 1);
        List<EarlyWarningProcessingConfigDo> list = configMapper.selectList(wrapper);

        if (CollUtil.isEmpty(list)) {
            log.error("未查询到预警处理配置");
            return;
        }

        for (EarlyWarningProcessingConfigDo config : list) {
            String dataSource = config.getDataSource();
            String warningSql = config.getWarningSql();
            try {
                String result;
                // 数据源判断
                if ("zj_sj".equals(dataSource)) {
                    result = getValueBySql(warningSql);
                } else if ("iob_gov2".equals(dataSource)) {
                    result = getValueBySqlForDm(warningSql);
                } else {
                    log.error("暂未支持该数据源的预警， dataSource={}", dataSource);
                    continue;
                }

                if (isWarning(result)) {
                    // 如果需要预警，则发送短信
                    String warningTarget = config.getWarningTarget();
                    String warningType = config.getWarningType();
                    String[] phoneArr = warningTarget.split("\\|");
                    for (String phone : phoneArr) {
                        this.sendMessage(phone, warningType, result);
                    }
                }

            } catch (Exception e) {
                log.error("该预警配置处理异常, config={}", config, e);
            }
        }
    }

    /**
     * 发送预警短信
     *
     * @param phone 手机号
     * @param warningType 预警类型
     * @param count 预警条数
     */
    private void sendMessage(String phone, String warningType, String count) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("TIME", DateUtils.dateTimeNow("yyyy年MM月dd日 HH时mm分ss秒"));
            map.put("SPECIAL", warningType);
            map.put("REGION", count + "条");
            conOpenSendImpl.sendMessageByTemplate("ALM_OPR07", map, phone);
        } catch (Exception e) {
            log.error("发送预警短信异常，phone={}, warningType={}, count={}", phone, warningType, count, e);
        }
    }

    /**
     * 执行动态SQL查询
     *
     * @param sql 动态SQL
     * @return 查询结果，未查出默认返回PASS_TAG
     */
    private String getValueBySql(String sql) {
        List<String> countList = configMapper.queryBySql(sql);
        if (CollUtil.isNotEmpty(countList)) {
            return countList.get(0);
        }
        // 如果未查出结果，默认通过，不需要预警
        return PASS_TAG;
    }

    /**
     * 执行动态SQL查询（达梦数据源）
     *
     * @param sql 动态SQL
     * @return 查询结果，未查出默认返回PASS_TAG
     */
    private String getValueBySqlForDm(String sql) {
        List<String> list = configMapper.queryDmBySql(sql);
        if (CollUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        // 如果未查出结果，默认通过，不需要预警
        return PASS_TAG;
    }

    /**
     * 根据查询结果判断是否需要预警
     *
     * @param result 查询结果
     * @return true: 需要预警
     */
    private boolean isWarning(String result) {
        // 如果未查出结果，默认通过，不需要预警
        if (StringUtils.isBlank(result)) {
            return false;
        }
        // 如果结果为PASS_TAG的值，则认为不需要预警
        if (PASS_TAG.equals(result)) {
            return false;
        }
        return true;
    }
}
