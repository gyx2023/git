package com.crm.job.service.warning;

import com.crm.system.service.warning.WarningRecord;

import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 预警查询策略轻量抽象基类
 * <p>
 * 统一 query() 骨架（日志 + 空日期默认处理），子类只需实现 doQuery()。
 * <p>
 * 设计说明：旧系统 BaseJob 的模板方法 startTask() 因过度抽象，所有子类都 override 绕过。
 * 此基类吸取教训，只约束查询阶段——子类能 override 的只有 doQuery() 和 getOrderTypeName()，
 * 从而保证各 Strategy 的代码风格一致，同时避免不必要的抽象。
 *
 * @author crm
 */
public abstract class AbstractWarningQueryStrategy implements IWarningQueryStrategy {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public List<WarningRecord> query(String targetDate) {
        if (StringUtils.isBlank(targetDate)) {
            targetDate = DateUtils.dateTimeNow(DateUtils.YYYYMMDD);
        }
        log.info("【{}】开始查询, 日期={}", getOrderTypeName(), targetDate);
        List<WarningRecord> records = doQuery(targetDate);
        log.info("【{}】查询完成, 共{}条", getOrderTypeName(), records.size());
        return records;
    }

    /**
     * 子类实现：查询业务表 → 转 WarningRecord
     *
     * @param targetDate 目标日期（yyyyMMdd，已保证非空）
     * @return 待处理的预警记录列表
     */
    protected abstract List<WarningRecord> doQuery(String targetDate);

    /**
     * 子类提供中文名称，用于日志输出
     *
     * @return 如 "合同到期预警"
     */
    protected abstract String getOrderTypeName();
}
