package com.crm.system.listener.client;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.excel.ExcelResult;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.GrpBusiDeptTemplateVo;
import com.crm.system.domain.vo.GrpBusiDeptTemplateResultVo;
import com.crm.system.service.ISysDictDataService;
import com.crm.system.service.ISysDictTypeService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户归属部门导入监听器
 * 处理客户归属部门数据导入的具体业务逻辑
 *
 * @author lingma
 * @date 2025-12-01
 */
@Component
@Slf4j
public class GrpBusiDeptImportListener implements ReadListener<GrpBusiDeptTemplateVo> {
    
    @Autowired
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    @Autowired
    private ISysDictDataService sysDictTypeService;
    
    private List<GrpBusiDeptTemplateVo> cachedDataList = new ArrayList<>();
    
    @Override
    public void invoke(GrpBusiDeptTemplateVo data, AnalysisContext context) {
        cachedDataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // Excel解析完成后不需要特殊处理
    }
    
    public ExcelResult<GrpBusiDeptTemplateResultVo> getExcelResult() {
        return new ExcelResult<GrpBusiDeptTemplateResultVo>() {
            @Override
            public String getAnalysis() {
                return "解析完成";
            }

            @Override
            public List<GrpBusiDeptTemplateResultVo> getList() {
                return processImportData(cachedDataList);
            }

            @Override
            public List<String> getErrorList() {
                return new ArrayList<>();
            }
        };
    }

    /**
     * 处理导入数据列表
     *
     * @param dataList 数据列表
     * @return 结果列表
     */
    public List<GrpBusiDeptTemplateResultVo> processImportData(List<GrpBusiDeptTemplateVo> dataList) {
        List<GrpBusiDeptTemplateResultVo> resultList = new ArrayList<>();

        // 获取有效的客户归属部门列表
        List<String> validDeptNames = getValidBusiDeptNames();

        // 逐条处理数据
        for (GrpBusiDeptTemplateVo data : dataList) {
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
                updateBo.setGrpBusiDeptType(data.getBusiDeptName());
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

        // 提取字典标签作为有效的客户归属部门名称
        return dictDataList.stream()
                .map(SysDictData::getDictValue)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 转换为结果VO
     *
     * @param data 模板数据
     * @return 结果VO
     */
    private GrpBusiDeptTemplateResultVo convertToResultVo(GrpBusiDeptTemplateVo data) {
        GrpBusiDeptTemplateResultVo resultVo = new GrpBusiDeptTemplateResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setBusiDeptName(data.getBusiDeptName());
        return resultVo;
    }
}