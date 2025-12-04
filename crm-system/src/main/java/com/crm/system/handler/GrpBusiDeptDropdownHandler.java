package com.crm.system.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.system.service.ISysDictDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户归属部门下拉框处理器
 * 在导出客户归属部门模板时，为"客户归属部门"列添加下拉选项
 *
 * @author lingma
 * @date 2025-12-04
 */
@Slf4j
@Component
public class GrpBusiDeptDropdownHandler implements SheetWriteHandler {

    @Autowired
    private ISysDictDataService dictDataService;

    // 客户归属部门列索引(从0开始)
    private static final int BUSI_DEPT_COLUMN_INDEX = 1;

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        // 获取数据验证助手
        DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper();

        // 从字典表中获取客户归属部门数据 sys_busi_dept_type 类型
        List<SysDictData> dictDataList = dictDataService.selectDictDataByType("sys_busi_dept_type");
        List<String> deptNames = dictDataList.stream()
                .map(SysDictData::getDictValue)
                .collect(Collectors.toList());

        // 创建下拉约束
        DataValidationConstraint constraint = helper.createExplicitListConstraint(deptNames.toArray(new String[0]));

        // 设置下拉框作用范围（第二列，从第二行开始到最大行数）
        CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, BUSI_DEPT_COLUMN_INDEX, BUSI_DEPT_COLUMN_INDEX);

        // 创建数据验证对象
        DataValidation validation = helper.createValidation(constraint, addressList);

        // 设置提示信息
        validation.setShowPromptBox(true);
        validation.createPromptBox("提示", "请选择客户归属部门");

        // 设置输入无效时的提示信息
        validation.setShowErrorBox(true);
        validation.createErrorBox("错误", "请选择下拉列表中的客户归属部门");

        // 将数据验证添加到工作表
        writeSheetHolder.getSheet().addValidationData(validation);
    }
}