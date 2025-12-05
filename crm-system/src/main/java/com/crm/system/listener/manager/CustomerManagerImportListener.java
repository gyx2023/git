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

                // 2. 检查是否所有客户经理信息都为空
                if (!isManagerInfoProvided(data.getProvManagerName(), data.getProvManagerOauser()) &&
                    !isManagerInfoProvided(data.getCityManagerName(), data.getCityManagerOauser()) &&
                    !isManagerInfoProvided(data.getCountyManagerName(), data.getCountyManagerOauser())) {
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
            // 1. 获取客户经理可选列表并验证Excel中填写的客户经理是否在可选列表中
            // 验证省级客户经理
            if (isManagerInfoProvided(data.getProvManagerName(), data.getProvManagerOauser())) {
                List<Map<String, Object>> provManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getCountyId(), "01");
                if (!isManagerInList(data.getProvManagerName(), data.getProvManagerOauser(), provManagers)) {
                    resultVo.setErrorMessage("省级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 验证市级客户经理
            if (isManagerInfoProvided(data.getCityManagerName(), data.getCityManagerOauser())) {
                List<Map<String, Object>> cityManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getCountyId(), "02");
                if (!isManagerInList(data.getCityManagerName(), data.getCityManagerOauser(), cityManagers)) {
                    resultVo.setErrorMessage("市级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 验证区县级客户经理
            if (isManagerInfoProvided(data.getCountyManagerName(), data.getCountyManagerOauser())) {
                List<Map<String, Object>> countyManagers = dwdRepSysPersonDService.queryManagersByCityCodeAndType(
                        groupInfo.getCountyId(), "03");
                if (!isManagerInList(data.getCountyManagerName(), data.getCountyManagerOauser(), countyManagers)) {
                    resultVo.setErrorMessage("区县级客户经理信息不匹配或不在可选列表中");
                    return false;
                }
            }

            // 2. 构造更新参数
            ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
            updateBo.setGrpId(groupInfo.getGrpId());

            // 设置省级客户经理
            if (isManagerInfoProvided(data.getProvManagerName(), data.getProvManagerOauser())) {
                updateBo.setProvManagerName(data.getProvManagerName());
                updateBo.setProvManagerOauser(data.getProvManagerOauser());
            }

            // 设置市级客户经理
            if (isManagerInfoProvided(data.getCityManagerName(), data.getCityManagerOauser())) {
                updateBo.setCityManagerName(data.getCityManagerName());
                updateBo.setCityManagerOauser(data.getCityManagerOauser());
            }

            // 设置区县级客户经理
            if (isManagerInfoProvided(data.getCountyManagerName(), data.getCountyManagerOauser())) {
                updateBo.setCountyManagerName(data.getCountyManagerName());
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
     * 判断客户经理信息是否已提供（姓名和OA账号都非空且非空白）
     *
     * @param managerName 客户经理姓名
     * @param managerOa 客户经理OA账号
     * @return 是否提供了客户经理信息
     */
    private boolean isManagerInfoProvided(String managerName, String managerOa) {
        return (managerName != null && !managerName.trim().isEmpty()) &&
               (managerOa != null && !managerOa.trim().isEmpty());
    }

    /**
     * 检查指定的客户经理姓名和OA账号是否在可选列表中且匹配
     *
     * @param managerName 客户经理姓名
     * @param managerOa 客户经理OA账号
     * @param managers 可选客户经理列表
     * @return 是否匹配且在列表中
     */
    private boolean isManagerInList(String managerName, String managerOa, List<Map<String, Object>> managers) {
        if ((managerName == null || managerName.isEmpty()) ||
            (managerOa == null || managerOa.isEmpty()) ||
            managers == null || managers.isEmpty()) {
            return false;
        }

        for (Map<String, Object> category : managers) {
            List<Map<String, Object>> managerList = (List<Map<String, Object>>) category.get("managers");
            if (managerList != null) {
                for (Map<String, Object> manager : managerList) {
                    String name = (String) manager.get("name");
                    String oa = (String) manager.get("oaAccount"); // 注意：此处字段名为oaAccount，不是oa
                    // 姓名和OA账号都需要匹配
                    if (managerName.equals(name) && managerOa.equals(oa)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        resultVo.setProvManagerName(data.getProvManagerName());
        resultVo.setProvManagerOauser(data.getProvManagerOauser());
        resultVo.setCityManagerName(data.getCityManagerName());
        resultVo.setCityManagerOauser(data.getCityManagerOauser());
        resultVo.setCountyManagerName(data.getCountyManagerName());
        resultVo.setCountyManagerOauser(data.getCountyManagerOauser());
        return resultVo;
    }
}
