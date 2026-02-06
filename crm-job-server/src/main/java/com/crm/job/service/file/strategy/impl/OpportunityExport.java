package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.service.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 商机信息导出策略实现类
 * 处理商机信息导出的具体业务逻辑
 *
 * @author crm
 * @date 2026-02-04
 */
@Slf4j
@Component
public class OpportunityExport extends AbstractExportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Override
    public String getBusinessType() {
        return "opportunity_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            log.info("开始执行商机导出任务，任务ID: {}", task.getTaskId());

            // 解析请求参数
            Map<String, Object> requestParams = null;
            if (task.getRequestParams() != null) {
                requestParams = JSONUtil.toBean(task.getRequestParams(), Map.class);
            }

            String oppCode = getParamValue(requestParams, "oppCode");
            String oppName = getParamValue(requestParams, "oppName");
            String custName = getParamValue(requestParams, "custName");
            String oppoStates = getParamValue(requestParams, "oppoStates");
            String oppSource = getParamValue(requestParams, "oppSource");
            String custManagerName = getParamValue(requestParams, "custManagerName");
            String areaId = getParamValue(requestParams, "areaId");
            String developFlag = getParamValue(requestParams, "developFlag");
            String auditStatus = getParamValue(requestParams, "auditStatus");
            String createTimeLeft = getParamValue(requestParams, "createTimeLeft");
            String createTimeRight = getParamValue(requestParams, "createTimeRight");
            String auditTimeLeft = getParamValue(requestParams, "auditTimeLeft");
            String auditTimeRight = getParamValue(requestParams, "auditTimeRight");
            String type1 = getParamValue(requestParams, "type1");
            String oppoTypes = getParamValue(requestParams, "oppoTypes");
            String oppoTypesNew = getParamValue(requestParams, "oppoTypesNew");
            String bgType = getParamValue(requestParams, "bgType");
            String expectContractMoneyMin = getParamValue(requestParams, "expectContractMoneyMin");
            String expectContractMoneyMax = getParamValue(requestParams, "expectContractMoneyMax");
            String isValid = getParamValue(requestParams, "isValid");
            String isBidding = getParamValue(requestParams, "isBidding");
            String bidState = getParamValue(requestParams, "bidState");
            String moneyMin = getParamValue(requestParams, "moneyMin");
            String moneyMax = getParamValue(requestParams, "moneyMax");
            String signTimeStart = getParamValue(requestParams, "signTimeStart");
            String signTimeEnd = getParamValue(requestParams, "signTimeEnd");
            String oppWinProbMin = getParamValue(requestParams, "oppWinProbMin");
            String oppWinProbMax = getParamValue(requestParams, "oppWinProbMax");
            String oppGrowthType = getParamValue(requestParams, "oppGrowthType");
            String estimatTotalContratAmountMin = getParamValue(requestParams, "estimatTotalContratAmountMin");
            String estimatTotalContratAmountMax = getParamValue(requestParams, "estimatTotalContratAmountMax");

            StringBuffer sql = new StringBuffer();
            sql.append("select * from topp_opportunity op " +
                "join sys_user su on su.id_ = op.cust_manager_id where" +
                " (1=2 or (1=1 and (1=2 or op.opp_area_id like '33%%' ) " +
                "and op.unicom_type in('22','01','23','02','03','25','unicom_jr',");
            return true;

        } catch (Exception e) {
            log.error("商机信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从参数Map中获取指定参数的值
     * @param params 参数Map
     * @param paramName 参数名
     * @return 参数值
     */
    private String getParamValue(Map<String, Object> params, String paramName) {
        if (params != null && params.containsKey(paramName)) {
            Object valueObj = params.get(paramName);
            if (valueObj != null) {
                if (valueObj instanceof String[]) {
                    String[] values = (String[]) valueObj;
                    if (values.length > 0) {
                        return values[0];
                    }
                } else if (valueObj instanceof String) {
                    return (String) valueObj;
                } else if (valueObj instanceof List) {
                    List<?> list = (List<?>) valueObj;
                    if (!list.isEmpty()) {
                        return String.valueOf(list.get(0));
                    }
                }
            }
        }
        return null;
    }
}
