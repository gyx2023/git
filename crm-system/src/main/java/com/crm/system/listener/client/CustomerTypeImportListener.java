package com.crm.system.listener.client;

import com.crm.common.core.domain.entity.SysDictData;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.GrpCustomerTypeImportVo;
import com.crm.system.domain.vo.GrpCustomerTypeTemplateResultVo;
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
 * 集团客户类别导入监听器
 * 处理集团客户类别导入数据导入的具体业务逻辑
 *
 * @author lingma
 * @date 2025-12-02
 */
@Component
@Slf4j
public class CustomerTypeImportListener {

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
    public List<GrpCustomerTypeTemplateResultVo> processImportData(List<GrpCustomerTypeImportVo> dataList) {
        List<GrpCustomerTypeTemplateResultVo> resultList = new ArrayList<>();

        // 获取有效的类别列表和标签到值的映射
        List<String> validCustomerTypeLabels = getValidExpandTypeLabels();
        Map<String, String> labelToValueMap = getLabelToValueMap();

        // 逐条处理数据
        for (GrpCustomerTypeImportVo data : dataList) {
            GrpCustomerTypeTemplateResultVo resultVo = convertToResultVo(data);
            try {
                // 验证总部集团编码是否为空
                if (data.getHqGrpCode() == null || data.getHqGrpCode().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("总部集团编码不能为空");
                    resultList.add(resultVo);
                    continue;
                }

                // 验证集团客户类别是否为空
                if (data.getGrpCustType() == null || data.getGrpCustType().trim().isEmpty()) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("集团客户类别不能为空");
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

                // 验证集团客户类别是否在可选列表中
                if (!validCustomerTypeLabels.contains(data.getGrpCustType())) {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("集团客户类别不在可选列表中");
                    resultList.add(resultVo);
                    continue;
                }

                // 构造更新参数并调用更新接口
                ZqGrpGroupInfoBo updateBo = new ZqGrpGroupInfoBo();
                updateBo.setGrpId(groupInfo.getGrpId());
                // 使用标签对应的值进行更新
                String grpCustType = labelToValueMap.get(data.getGrpCustType());
                updateBo.setGrpCustType(grpCustType);
                boolean success = zqGrpGroupInfoService.updateByBo(updateBo);

                if (success) {
                    resultVo.setProcessResult("成功");
                } else {
                    resultVo.setProcessResult("失败");
                    resultVo.setErrorMessage("更新集团客户类别信息失败");
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
     * 获取有效的集团客户类别标签列表
     *
     * @return 集团客户类别标签列表
     */
    private List<String> getValidExpandTypeLabels() {
        // 调用接口获取集团客户类别数据
        List<SysDictData> dictDataList = sysDictTypeService.selectDictDataByType("sys_cust_type");
        if (CollectionUtils.isEmpty(dictDataList)) {
            return new ArrayList<>();
        }

        // 提取字典标签作为有效的集团客户类别标签
        return dictDataList.stream()
            .map(SysDictData::getDictLabel)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList());
    }

    /**
     * 获取标签到值的映射
     *
     * @return 标签到值的映射
     */
    private Map<String, String> getLabelToValueMap() {
        // 调用接口获取集团客户类别数据
        List<SysDictData> dictDataList = sysDictTypeService.selectDictDataByType("sys_cust_type");
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
    private GrpCustomerTypeTemplateResultVo convertToResultVo(GrpCustomerTypeImportVo data) {
        GrpCustomerTypeTemplateResultVo resultVo = new GrpCustomerTypeTemplateResultVo();
        resultVo.setHqGrpCode(data.getHqGrpCode());
        resultVo.setGrpCustType(data.getGrpCustType());
        return resultVo;
    }
}
