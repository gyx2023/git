package com.crm.job.job.warning;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 通用预警启动任务（配置驱动）
 * <p>
 * 从 sys_param 表（type='warning_job'）读取配置，按各自日期设置逐一处理每种预警类型。
 * XXL-Job 参数传入日期（yyyyMMdd）则所有类型统一使用该日期，不传则按配置默认偏移计算。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningJob {

    private static final String PARAM_TYPE = "warning_job";

    private final IWarningStartService warningStartService;
    private final ISysParamService sysParamService;

    @XxlJob("warningJob")
    public void start() {
        long startTime = System.currentTimeMillis();
        String overrideDate = XxlJobHelper.getJobParam();
        log.info("【通用预警任务】开始执行, dateParam={}", overrideDate);
        XxlJobHelper.log("通用预警任务开始执行, dateParam=" + overrideDate);

        Map<String, String> paramMap = sysParamService.findValueMapsByType(PARAM_TYPE);
        if (paramMap == null || paramMap.isEmpty()) {
            log.warn("【通用预警任务】未找到 type={} 的配置", PARAM_TYPE);
            XxlJobHelper.handleSuccess("未找到预警任务配置");
            return;
        }

        List<JobConfig> configs = new ArrayList<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            try {
                JSONObject json = JSON.parseObject(entry.getValue());
                JobConfig cfg = new JobConfig();
                cfg.orderType = entry.getKey();
                cfg.dateFormat = json.getString("dateFormat");
                cfg.dateOffset = json.getInteger("dateOffset");
                cfg.dateOffsetUnit = json.getString("dateOffsetUnit");
                cfg.sortOrder = json.getInteger("sortOrder");
                cfg.name = json.getString("name");
                if (cfg.dateFormat == null) cfg.dateFormat = "yyyyMMdd";
                if (cfg.dateOffset == null) cfg.dateOffset = 0;
                if (cfg.dateOffsetUnit == null) cfg.dateOffsetUnit = "DAY";
                if (cfg.sortOrder == null) cfg.sortOrder = 0;
                configs.add(cfg);
            } catch (Exception e) {
                log.warn("【通用预警任务】解析配置失败: code={}", entry.getKey(), e);
            }
        }

        configs.sort(Comparator.comparingInt(c -> c.sortOrder));
        log.info("【通用预警任务】共 {} 个类型待处理", configs.size());

        int successCount = 0;
        int failCount = 0;

        for (JobConfig cfg : configs) {
            try {
                String targetDate = resolveTargetDate(cfg, overrideDate);
                String label = StringUtils.isNotBlank(cfg.name) ? cfg.name : cfg.orderType;
                log.info("【通用预警任务】处理类型: {}, 日期: {}", cfg.orderType, targetDate);
                XxlJobHelper.log("处理类型: " + label + ", 日期: " + targetDate);
                warningStartService.startByType(cfg.orderType, targetDate);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("【通用预警任务】类型 {} 处理异常", cfg.orderType, e);
                XxlJobHelper.log("类型 " + cfg.orderType + " 处理异常: " + e.getMessage());
            }
        }

        long costTime = System.currentTimeMillis() - startTime;
        String resultMsg = String.format("通用预警任务执行完成: 成功=%d, 失败=%d, 耗时=%dms",
                successCount, failCount, costTime);
        log.info("【通用预警任务】{}", resultMsg);
        XxlJobHelper.handleSuccess(resultMsg);
    }

    private String resolveTargetDate(JobConfig cfg, String overrideDate) {
        if (StringUtils.isNotBlank(overrideDate)) {
            return overrideDate.trim();
        }
        int offset = cfg.dateOffset;
        String unit = cfg.dateOffsetUnit;
        String format = cfg.dateFormat;

        Calendar calendar = Calendar.getInstance();
        if ("MONTH".equalsIgnoreCase(unit)) {
            calendar.add(Calendar.MONTH, offset);
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, offset);
        }
        return new SimpleDateFormat(format).format(calendar.getTime());
    }

    private static class JobConfig {
        String orderType;
        String name;
        String dateFormat;
        Integer dateOffset;
        String dateOffsetUnit;
        Integer sortOrder;
    }
}
