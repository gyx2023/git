package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用预警启动任务
 * <p>
 * 从 sys_param（type='warn_type', code='warn_switch'）读取逗号分隔的预警类型列表，
 * 依次执行。XXL-Job 参数传入日期（yyyyMM），不传则默认上个月。
 * </p>
 * <pre>
 * 配置示例：INSERT INTO sys_param (code_, name_, value_, remark_, type_)
 * VALUES ('warn_switch', '宽带装机地址疑似聚合或非真实预警', 'kd_addr_warn,new_wide_warn', '...', 'warn_type');
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningJob {

    private static final String PARAM_TYPE = "warn_type";
    private static final String PARAM_CODE = "warning_job";

    private final IWarningStartService warningStartService;
    private final ISysParamService sysParamService;

    @XxlJob("warningJob")
    public void start() {
        long startTime = System.currentTimeMillis();
        String overrideDate = XxlJobHelper.getJobParam();
        log.info("【通用预警任务】开始执行, dateParam={}", overrideDate);
        XxlJobHelper.log("通用预警任务开始执行, dateParam=" + overrideDate);

        SysParamDo config = sysParamService.findValueByTypeCode(PARAM_TYPE, PARAM_CODE);
        if (config == null || StringUtils.isBlank(config.getValue())) {
            log.warn("【通用预警任务】未找到 type={}, code={} 的配置", PARAM_TYPE, PARAM_CODE);
            XxlJobHelper.handleSuccess("未找到预警任务配置");
            return;
        }

        List<String> orderTypes = Arrays.stream(config.getValue().split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        // 日期：XXL-Job 参数优先，否则默认上个月
        String targetDate;
        if (StringUtils.isNotBlank(overrideDate)) {
            targetDate = overrideDate.trim();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            targetDate = new java.text.SimpleDateFormat("yyyyMM").format(calendar.getTime());
        }

        log.info("【通用预警任务】启用类型: {}, 日期={}", orderTypes, targetDate);
        XxlJobHelper.log("启用类型: " + String.join(",", orderTypes) + ", 日期: " + targetDate);

        int successCount = 0;
        int failCount = 0;

        for (String orderType : orderTypes) {
            try {
                log.info("【通用预警任务】处理类型: {}, 日期: {}", orderType, targetDate);
                warningStartService.startByType(orderType, targetDate);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("【通用预警任务】类型 {} 处理异常", orderType, e);
                XxlJobHelper.log("类型 " + orderType + " 处理异常: " + e.getMessage());
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        String resultMsg = String.format("通用预警任务执行完成: 成功=%d, 失败=%d, 耗时=%dms",
                successCount, failCount, costTime);
        log.info("【通用预警任务】{}", resultMsg);
        XxlJobHelper.handleSuccess(resultMsg);
    }
}
