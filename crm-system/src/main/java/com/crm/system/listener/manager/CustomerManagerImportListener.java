package com.crm.system.listener.manager;

import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.CustomerManagerImportResultVo;
import com.crm.system.domain.vo.CustomerManagerTemplateVo;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户经理导入监听器
 * 处理客户经理数据导入的具体业务逻辑
 *
 * @author wcf
 * @date 2025-12-01
 */
@Component
@Slf4j
public class CustomerManagerImportListener {

    @Autowired
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    @Autowired
    private IDwdRepSysPersonDService dwdRepSysPersonDService;

    /**
     * 处理导入数据列表
     *
     * @param dataList 数据列表
     * @return 结果列表
     */
    public List<CustomerManagerImportResultVo> processImportData(List<CustomerManagerTemplateVo> dataList) {
        List<CustomerManagerImportResultVo> resultList = new ArrayList<>();

        // 逐条处理数据
        for (CustomerManagerTemplateVo data : dataList) {
            CustomerManagerImportResultVo resultVo = convertToResultVo(data);
            try {
                // 1. 先根据总部集团编码查询集团信息
                ZqGrpGroupInfo groupInfo = zqGrpGroupInfoService.selectByHqGrpCode(data.getHqGrpCode());
                if (groupInfo == null) {
                    log.warn("未找到总部集团编码对应的集团信息: {}", data.getHqGrpCode());
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("未找到对应的集团信息");
                    resultList.add(resultVo);
                    continue;
                }

                // 判断客户拓展类型是否为空
                String expandType = groupInfo.getGrpExpandType();
                if (expandType == null || expandType.isEmpty()) {
                    log.warn("该集团信息的拓展类型为空: {}", data.getHqGrpCode());
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("拓展类型为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 2. 检查是否所有客户经理信息都为空
                if (!isManagerInfoProvided(data.getProvManagerOauser()) &&
                    !isManagerInfoProvided(data.getCityManagerOauser()) &&
                    !isManagerInfoProvided(data.getCountyManagerOauser())) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("所有客户经理信息均为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 3. 处理单条记录
                boolean success = processSingleRecord(data, resultVo, groupInfo);
                if (success) {
                    resultVo.setProcessResult("成功");
                } else {
                    resultVo.setProcessResult("失败");
                    if (resultVo.getErrorMessage() == null || resultVo.getErrorMessage().isEmpty()) {
                        resultVo.setErrorMessage("更新客户经理信息失败");
                    }
                }
            } catch (Exception e) {
                resultVo.setProcessResult("失败");
                resultVo.setErrorMessage(e.getMessage());
                log.error("处理记录失败，总部集团编码: {}", data.getHqGrpCode(), e);
            }
            resultList.add(resultVo);
        }

        return resultList;
    }


    /**
     * 处理单条记录
     *
     * @param data 客户经理数据
     * @param resultVo 结果VO，用于记录错误信息
     * @param groupInfo 集团信息
     * @return 是否处理成功
     */
    public boolean processSingleRecord(CustomerManagerTemplateVo data, CustomerManagerImportResultVo resultVo, ZqGrpGroupInfo groupInfo) {
        try {
            // 获取集团拓展类型
            String expandType = groupInfo.getGrpExpandType();

            // 验证省级客户经理 (只有01类型可以修改省级客户经理)
            if (isManagerInfoProvided(data.getProvManagerOauser())) {
                if (!"01".equals(expandType)) {
                    resultVo.setErrorMessage("当前集团拓展类型(" + expandType + ")不允许修改省级客户经理信息");
                    return false;
                }
                List<Map<String, Object>> provManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                    groupInfo.getCountyId(), "01");
                Map<String, Object> provManager = getManagerByOa(data.getProvManagerOauser(), provManagers);
                if (provManager == null) {
                    resultVo.setErrorMessage("省级客户经理OA账号不存在或不在可选列表中");
                    return false;
                }
            }

            // 验证市级客户经理 (01和02类型可以修改市级客户经理)
            if (isManagerInfoProvided(data.getCityManagerOauser())) {
                if (!"01".equals(expandType) && !"02".equals(expandType)) {
                    resultVo.setErrorMessage("当前集团拓展类型(" + expandType + ")不允许修改市级客户经理信息");
                    return false;
                }
                List<Map<String, Object>> cityManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                    groupInfo.getCountyId(), "02");
                Map<String, Object> cityManager = getManagerByOa(data.getCityManagerOauser(), cityManagers);
                if (cityManager == null) {
                    resultVo.setErrorMessage("市级客户经理OA账号不存在或不在可选列表中");
                    return false;
                }
            }

            // 验证区县级客户经理 (01、02和03类型都可以修改区县级客户经理)
            if (isManagerInfoProvided(data.getCountyManagerOauser())) {
                List<Map<String, Object>> countyManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                    groupInfo.getCountyId(), "03");
                Map<String, Object> countyManager = getManagerByOa(data.getCountyManagerOauser(), countyManagers);
                if (countyManager == null) {
                    resultVo.setErrorMessage("区县级客户经理OA账号不存在或不在可选列表中");
                    return false;
                }
            }

            // 2. 构造更新参数
            ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
            updateBo.setGrpId(groupInfo.getGrpId());

            // 设置省级客户经理
            if (isManagerInfoProvided(data.getProvManagerOauser())) {
                if ("01".equals(expandType)) { // 只有01类型可以修改省级客户经理
                    List<Map<String, Object>> provManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getCountyId(), "01");
                    Map<String, Object> provManager = getManagerByOa(data.getProvManagerOauser(), provManagers);
                    updateBo.setProvManagerName((String) provManager.get("name"));
                    updateBo.setProvManagerOauser(data.getProvManagerOauser());
                }
            }

            // 设置市级客户经理
            if (isManagerInfoProvided(data.getCityManagerOauser())) {
                if ("01".equals(expandType) || "02".equals(expandType)) { // 01和02类型可以修改市级客户经理
                    List<Map<String, Object>> cityManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getCountyId(), "02");
                    Map<String, Object> cityManager = getManagerByOa(data.getCityManagerOauser(), cityManagers);
                    updateBo.setCityManagerName((String) cityManager.get("name"));
                    updateBo.setCityManagerOauser(data.getCityManagerOauser());
                }
            }

            // 设置区县级客户经理
            if (isManagerInfoProvided(data.getCountyManagerOauser())) {
                // 01、02和03类型都可以修改区县级客户经理
                List<Map<String, Object>> countyManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                    groupInfo.getCountyId(), "03");
                Map<String, Object> countyManager = getManagerByOa(data.getCountyManagerOauser(), countyManagers);
                updateBo.setCountyManagerName((String) countyManager.get("name"));
                updateBo.setCountyManagerOauser(data.getCountyManagerOauser());
            }

            // 3. 调用更新接口
            // 直接调用服务方法更新客户经理信息
            return zqGrpGroupInfoService.updateByBo1(updateBo);
        } catch (Exception e) {
            log.error("处理单条记录失败: {}", data.getHqGrpCode(), e);
            resultVo.setErrorMessage("处理记录时发生异常: " + e.getMessage());
            return false;
        }
    }


    /**
     * 判断客户经理信息是否已提供（OA账号非空且非空白）
     *
     * @param managerOa 客户经理OA账号
     * @return 是否提供了客户经理信息
     */
    private boolean isManagerInfoProvided(String managerOa) {
        return (managerOa != null && !managerOa.trim().isEmpty());
    }

    /**
     * 根据OA账号获取客户经理信息
     *
     * @param managerOa 客户经理OA账号
     * @param managers 可选客户经理列表
     * @return 客户经理信息
     */
    private Map<String, Object> getManagerByOa(String managerOa, List<Map<String, Object>> managers) {
        if ((managerOa == null || managerOa.isEmpty()) ||
            managers == null || managers.isEmpty()) {
            return null;
        }

        for (Map<String, Object> category : managers) {
            List<Map<String, Object>> managerList = (List<Map<String, Object>>) category.get("managers");
            if (managerList != null) {
                for (Map<String, Object> manager : managerList) {
                    String oa = (String) manager.get("oaAccount"); // 注意：此处字段名为oaAccount，不是oa
                    if (managerOa.equals(oa)) {
                        return manager;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 转换为结果VO
     *
     * @param data 模板数据
     * @return 结果VO
     */
    public CustomerManagerImportResultVo convertToResultVo(CustomerManagerTemplateVo data) {
        CustomerManagerImportResultVo resultVo = new CustomerManagerImportResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setProvManagerOauser(data.getProvManagerOauser());
        resultVo.setCityManagerOauser(data.getCityManagerOauser());
        resultVo.setCountyManagerOauser(data.getCountyManagerOauser());
        return resultVo;
    }
}