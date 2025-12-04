package com.crm.system.utils;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.system.service.ISysDictTypeService;
import com.crm.common.utils.spring.SpringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel下拉框处理类
 * 用于在导出的Excel模板中添加下拉框选项
 */
public class ExcelDropdownHandler implements SheetWriteHandler {
    
    private final int dropdownColumnIndex;
    private final String dictType;
    
    public ExcelDropdownHandler(int dropdownColumnIndex, String dictType) {
        this.dropdownColumnIndex = dropdownColumnIndex;
        this.dictType = dictType;
    }
    
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        try {
            // 获取字典服务
            ISysDictTypeService dictService = SpringUtils.getBean(ISysDictTypeService.class);
            
            // 获取字典数据
            List<SysDictData> dictDataList = dictService.selectDictDataByType(dictType);
            
            if (dictDataList != null && !dictDataList.isEmpty()) {
                // 提取字典标签作为下拉选项（只显示标签，不显示值）
                List<String> dictLabels = dictDataList.stream()
                        .map(SysDictData::getDictLabel)
                        .collect(Collectors.toList());
                
                Workbook workbook = writeWorkbookHolder.getWorkbook();
                DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper();
                
                // 创建隐藏sheet存储下拉选项
                String hiddenSheetName = "hidden_" + dictType;
                Sheet hiddenSheet = workbook.createSheet(hiddenSheetName);
                
                // 在隐藏sheet中写入下拉选项（只存储标签）
                for (int i = 0; i < dictLabels.size(); i++) {
                    Row row = hiddenSheet.createRow(i);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(dictLabels.get(i));
                }
                
                // 创建名称管理器引用
                Name name = workbook.createName();
                String range = hiddenSheetName + "!$A$1:$A$" + dictLabels.size();
                name.setNameName(dictType);
                name.setRefersToFormula(range);
                
                // 设置下拉框约束
                DataValidationConstraint constraint = helper.createFormulaListConstraint(dictType);
                CellRangeAddressList addressList = new CellRangeAddressList(1, 10000, dropdownColumnIndex, dropdownColumnIndex);
                DataValidation validation = helper.createValidation(constraint, addressList);
                
                // 设置错误提示
                validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                validation.setShowErrorBox(true);
                validation.setSuppressDropDownArrow(true);
                validation.createErrorBox("提示", "请选择下拉框中的选项");
                
                // 添加数据验证到sheet
                writeSheetHolder.getSheet().addValidationData(validation);
                
                // 隐藏sheet
                int hiddenSheetIndex = workbook.getSheetIndex(hiddenSheet);
                workbook.setSheetHidden(hiddenSheetIndex, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}