package com.crm.system.listener.client;

import com.crm.common.core.domain.entity.SysDictData;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.GrpExpandTypeTemplateVo;
import com.crm.system.domain.vo.GrpExpandTypeTemplateResultVo;
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
 * 集团客户拓展类型导入监听器
 * 处理集团客户拓展类型数据导入的具体业务逻辑
 *
 * @author lingma
 * @date 2025-12-02
 */
@Component
@Slf4j
public class ExtendTypeImportListener {

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
    public List<GrpExpandTypeTemplateResultVo> processImportData(List<GrpExpandTypeTemplateVo> dataList) {
        List<GrpExpandTypeTemplateResultVo> resultList = new ArrayList<>();

        // 获取有效的拓展类型列表
        List<String> validExpandTypeNames = getValidExpandTypeNames();

        // 逐条处理数据
        for (GrpExpandTypeTemplateVo data : dataList) {
            GrpExpandTypeTemplateResultVo resultVo = convertToResultVo(data);
            try {
                // 验证总部集团编码是否为空
                if (data.getHqGrpCode() == null || data.getHqGrpCode().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("总部集团编码不能为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 验证集团客户拓展类型是否为空
                if (data.getExpandTypeName() == null || data.getExpandTypeName().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("集团客户拓展类型不能为空");
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

                // 验证集团客户拓展类型是否在可选列表中
                if (!validExpandTypeNames.contains(data.getExpandTypeName())) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("集团客户拓展类型不在可选列表中");
                    resultList.add(resultVo);
                    continue;
                }

                // 构造更新参数并调用更新接口
                ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
                updateBo.setGrpId(groupInfo.getGrpId());
                updateBo.setGrpExpandType(data.getExpandTypeName());
                boolean success = zqGrpGroupInfoService.updateByBo(updateBo);

                if (success) {
                    resultVo.setProcessResult("成功");
                } else {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("更新集团客户拓展类型信息失败");
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
     * 获取有效的集团客户拓展类型名称列表
     *
     * @return 集团客户拓展类型名称列表
     */
    private List<String> getValidExpandTypeNames() {
        // 调用接口获取集团客户拓展类型数据
        List<SysDictData> dictDataList = sysDictTypeService.selectDictDataByType("sys_expand_type");
        if (CollectionUtils.isEmpty(dictDataList)) {
            return new ArrayList<>();
        }

        // 提取字典标签作为有效的集团客户拓展类型名称
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
    private GrpExpandTypeTemplateResultVo convertToResultVo(GrpExpandTypeTemplateVo data) {
        GrpExpandTypeTemplateResultVo resultVo = new GrpExpandTypeTemplateResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setExpandTypeName(data.getExpandTypeName());
        return resultVo;
    }
}
