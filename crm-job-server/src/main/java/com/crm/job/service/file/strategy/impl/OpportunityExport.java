package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
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

            LoginUser loginUser = LoginHelper.getLoginUser();

            StringBuffer sql = new StringBuffer();

            sql.append("select * from topp_opportunity op " +
                "join sys_user su on su.id_ = op.cust_manager_id where" +
                " (1=2 or (1=1 and (1=2 or op.opp_area_id like '33%%' ) " +
                "op.unicom_type in('22','01','23','02','03','25','unicom_jr'," +
                "'04','26','unicom_ygs','05','06','28','07','29','08','unicom_zy'," +
                "'unicom_xy','30','10','13','15','16','17','18','19','unicom_dqy'," +
                "'unicom_sq','unicom_zlkh','21')) or (op.cust_manager_id = " + loginUser.getUserId() + "or " +
                "op.designer = " + loginUser.getUserId() + "or op.white_list like '%,"+ loginUser.getUserId() +",%' or" +
                " exists (select 1 from topp_member m where m.project_id = op.id_ and m.member_id = " + loginUser.getUserId()
                + "))) "
            );

            // 判断并决定是否查询条件
            if (StringUtils.isNotEmpty(oppCode)) {
                sql.append("and op.opp_code like '%" + oppCode + "%'");
            }

            if (StringUtils.isNotEmpty(oppName)) {
                sql.append("and op.opp_name like '%" + oppName + "%'");
            }

            if (StringUtils.isNotEmpty(custName)) {
                sql.append("and op.cust_name like '%" + custName + "%'");
            }

            // 商机状态处理，支持多选
            if (StringUtils.isNotEmpty(oppoStates)) {
                if (oppoStates.contains(",")) {
                    String[] states = oppoStates.split(",");
                    sql.append(" and ( 1=2");
                    for (String state : states) {
                        sql.append(" or op.opp_states = '" + state.trim() + "' ");
                    }
                    sql.append(" ) ");
                } else {
                    sql.append(" and op.opp_states = '" + oppoStates.trim() + "' ");
                }
            }

            if (StringUtils.isNotEmpty(oppSource)) {
                sql.append("and op.opp_source = '" + oppSource + "'" );
            }

            if (StringUtils.isNotEmpty(custManagerName)){
                sql.append("and op.name like '%" + custManagerName + "%'");
            }

            if (StringUtils.isNotEmpty(areaId)) {
                sql.append("and op.opp_area_id = '" + areaId + "'");
            }

            if (StringUtils.isNotEmpty(developFlag)) {
                sql.append("and op.develop_flag = '" + developFlag + "'" );
            }

            if (auditStatus != null) {
            }

            if (StringUtils.isNotEmpty(createTimeLeft)) {
                sql.append("and op.create_time >= '" + createTimeLeft + "'");
            }

            if (StringUtils.isNotEmpty(createTimeRight)) {
                sql.append("and op.create_time <= '" + createTimeRight + "'");
            }

            if (StringUtils.isNotEmpty(auditTimeLeft)) {
                sql.append("and op.audit_time >= '" + auditTimeLeft + "'");
            }

            if (StringUtils.isNotEmpty(auditTimeRight)) {
                sql.append("and op.audit_time <= '" + auditTimeRight + "'");
            }

            if (type1 != null) {
            }

            // 商机类型处理，支持多选
            if (StringUtils.isNotEmpty(oppoTypesNew) && StringUtils.isNotEmpty(oppoTypes)) {
                oppoTypes += "," + oppoTypesNew;
            } else if (StringUtils.isEmpty(oppoTypes) && StringUtils.isNotEmpty(oppoTypesNew)) {
                oppoTypes = oppoTypesNew;
            }

            // 构建商机类型查询条件
            if (StringUtils.isNotEmpty(oppoTypes)) {
                if (oppoTypes.contains(",")) {
                    String[] types = oppoTypes.split(",");
                    sql.append(" and ( 1=2");
                    for (String type : types) {
                        sql.append(" or op.opp_type = '" + type + "' or op.opp_include_type like '%," + type + ",%' ");
                    }
                    sql.append(" ) ");
                } else {
                    sql.append(" and (op.opp_type = '" + oppoTypes + "' or op.opp_include_type like '%," + oppoTypes + ",%')");
                }
            }

            if (StringUtils.isNotEmpty(bgType)) {
                sql.append("and op.bg_type = '" + bgType + "'" );
            }

            if (isValid != null) {
            }

            if (StringUtils.isNotEmpty(isBidding)) {
                sql.append("and op.is_bidding = '" + isBidding + "'" );
            }

            if (bidState != null) {
            }

            if (StringUtils.isNotEmpty(moneyMin)) {
                sql.append("and op.expectContractMoney >= '" + moneyMin + "'");
            }

            if (StringUtils.isNotEmpty(moneyMax)) {
                sql.append("and op.expectContractMoney <= '" + moneyMax + "'");
            }

            if (StringUtils.isNotEmpty(signTimeStart)) {
                sql.append("and op.sign_time >= '" + signTimeStart + "'");
            }

            if (StringUtils.isNotEmpty(signTimeEnd)) {
                sql.append("and op.sign_time <= '" + signTimeEnd + "'");
            }

            if (StringUtils.isNotEmpty(oppWinProbMin) && StringUtils.isNotEmpty(oppWinProbMax)) {
                String oppWinProb = oppWinProbMin + "%-" + oppWinProbMax+"%";
                sql.append("and op.opp_win_prob = '" + oppWinProb + "'");
            }

            if (StringUtils.isNotEmpty(oppGrowthType)) {
                sql.append("and op.opp_growth_type = '" + oppGrowthType + "'" );
            }

            if (StringUtils.isNotEmpty(estimatTotalContratAmountMin)) {
                sql.append("and op.estimat_total_contrat_amount >= '" + estimatTotalContratAmountMin + "'");
            }

            if (StringUtils.isNotEmpty(estimatTotalContratAmountMax)) {
                sql.append("and op.estimat_total_contrat_amount <= '" + estimatTotalContratAmountMax + "'");
            }



            sql.append(" order by op.id_ desc ok");
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
