package com.crm.system.listener.client;

import com.crm.common.core.domain.entity.SysDictData;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.GrpBusiDeptImportVo;
import com.crm.system.domain.vo.GrpBusiDeptTemplateResultVo;
import com.crm.system.domain.vo.GrpBusiDeptTemplateVo;
import com.crm.system.service.ISysDictTypeService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户归属部门导入监听器
 * 处理客户归属部门数据导入的具体业务逻辑
 *
 * @author wcf
 * @date 2025-12-01
 */
@Component
@Slf4j
public class GrpBusiDeptImportListener {

    @Autowired
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    @Autowired
    private ISysDictTypeService sysDictTypeService;

    /**
     * 处理导入数据列表
     *
     * @param dataList 数据列表
     * @return 结果列表
     */
    public List<GrpBusiDeptTemplateResultVo> processImportData(List<GrpBusiDeptImportVo> dataList) {
        List<GrpBusiDeptTemplateResultVo> resultList = new ArrayList<>();

        // 获取有效的客户归属部门列表和标签到值的映射
        List<String> validDeptNames = getValidBusiDeptNames();
        Map<String, String> labelToValueMap = getLabelToValueMap();

        // 逐条处理数据
        for (GrpBusiDeptImportVo data : dataList) {
            GrpBusiDeptTemplateResultVo resultVo = convertToResultVo(data);
            try {
                // 验证总部集团编码是否为空
                if (data.getHqGrpCode() == null || data.getHqGrpCode().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("总部集团编码不能为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 验证客户归属部门是否为空
                if (data.getBusiDeptName() == null || data.getBusiDeptName().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("客户归属部门不能为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 根据总部集团编码查询集团信息
                ZqGrpGroupInfo groupInfo = zqGrpGroupInfoService.selectByHqGrpCode(data.getHqGrpCode());
                if (groupInfo == null) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("未找到对应的集团信息");
                    resultList.add(resultVo);
                    continue;
                }

                // 验证客户归属部门是否在可选列表中
                if (!validDeptNames.contains(data.getBusiDeptName())) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("客户归属部门不在可选列表中");
                    resultList.add(resultVo);
                    continue;
                }

                // 构造更新参数并调用更新接口
                ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
                updateBo.setGrpId(groupInfo.getGrpId());
                // 使用标签对应的值进行更新（仿照集团客户拓展类型处理方式）
                String busiDeptValue = labelToValueMap.get(data.getBusiDeptName());
                updateBo.setGrpBusiDeptType(busiDeptValue);
                boolean success = zqGrpGroupInfoService.updateByBo(updateBo);

                if (success) {
                    resultVo.setProcessResult("成功");
                } else {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("更新客户归属部门信息失败");
                }
            } catch (Exception e) {
                resultVo.setProcessResult("失败");
                resultVo.setErrorMessage("处理记录时发生异常: " + e.getMessage());
                log.error("处理记录失败，总部集团编码: {}", data.getHqGrpCode(), e);
            }
            resultList.add(resultVo);
        }

        return resultList;
    }

    /**
     * 处理导入数据列表 - 重载方法，处理原始的TemplateVo类型
     *
     * @param dataList 数据列表
     * @return 结果列表
     */
    public List<GrpBusiDeptTemplateResultVo> processImportDataTemplate(List<GrpBusiDeptTemplateVo> dataList) {
        // 将TemplateVo转换为ImportVo
        List<GrpBusiDeptImportVo> importDataList = new ArrayList<>();
        for (GrpBusiDeptTemplateVo templateVo : dataList) {
            GrpBusiDeptImportVo importVo = new GrpBusiDeptImportVo();
            importVo.setHqGrpCode(templateVo.getHqGrpCode());
            importVo.setBusiDeptName(templateVo.getBusiDeptName());
            importDataList.add(importVo);
        }
        return processImportData(importDataList);
    }

    /**
     * 获取有效的客户归属部门名称列表
     *
     * @return 客户归属部门名称列表
     */
    private List<String> getValidBusiDeptNames() {
        // 调用之前创建的接口获取集团客户业务部门分类数据
        List<SysDictData> dictDataList = sysDictTypeService.selectDictDataByType("sys_busi_dept_type");
        if (CollectionUtils.isEmpty(dictDataList)) {
            return new ArrayList<>();
        }

        // 提取字典标签作为有效的客户归属部门名称（Excel中存储的是标签）
        return dictDataList.stream()
                .map(SysDictData::getDictLabel)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 获取标签到值的映射（仿照集团客户拓展类型处理方式）
     *
     * @return 标签到值的映射
     */
    private Map<String, String> getLabelToValueMap() {
        // 调用接口获取集团客户业务部门分类数据
        List<SysDictData> dictDataList = sysDictTypeService.selectDictDataByType("sys_busi_dept_type");
        if (CollectionUtils.isEmpty(dictDataList)) {
            return Collections.emptyMap();
        }

        // 构建标签到值的映射
        return dictDataList.stream()
                .collect(Collectors.toMap(SysDictData::getDictLabel, SysDictData::getDictValue));
    }

    /**
     * 转换为结果VO
     *
     * @param data 模板数据
     * @return 结果VO
     */
    private GrpBusiDeptTemplateResultVo convertToResultVo(GrpBusiDeptImportVo data) {
        GrpBusiDeptTemplateResultVo resultVo = new GrpBusiDeptTemplateResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setBusiDeptName(data.getBusiDeptName());
        return resultVo;
    }
}
