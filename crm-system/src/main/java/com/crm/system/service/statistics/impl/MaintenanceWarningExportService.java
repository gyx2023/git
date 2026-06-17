package com.crm.system.service.statistics.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 维系预警报表导出服务 — 复杂表头、中文列名、分组颜色
 */
@Slf4j
@Component
public class MaintenanceWarningExportService {

    // ========== 颜色常量 ==========
    private static final byte[] COLOR_HEADER_DEEP = new byte[]{(byte) 0x44, (byte) 0x72, (byte) 0xC4};   // #4472C4 深蓝
    private static final byte[] COLOR_HEADER_LIGHT = new byte[]{(byte) 0xD6, (byte) 0xE4, (byte) 0xF0}; // #D6E4F0 浅蓝
    // Tab2 列分组颜色
    private static final byte[] COLOR_GRAY = new byte[]{(byte) 0xD9, (byte) 0xD9, (byte) 0xD9};   // 浅灰
    private static final byte[] COLOR_BLUE = new byte[]{(byte) 0xBD, (byte) 0xD7, (byte) 0xEE};   // 浅蓝
    private static final byte[] COLOR_GREEN = new byte[]{(byte) 0xC6, (byte) 0xEF, (byte) 0xCE};  // 浅绿
    private static final byte[] COLOR_RED = new byte[]{(byte) 0xFF, (byte) 0xC7, (byte) 0xCE};    // 浅红
    private static final byte[] COLOR_YELLOW = new byte[]{(byte) 0xFF, (byte) 0xEB, (byte) 0x9C}; // 浅黄

    // ========== 入口 ==========

    /**
     * 根据 tabType 导出对应报表
     * @param dataList 已查询好的数据（Controller 负责查）
     * @param orderType 预警类型（Tab3/4/6 需要）
     */
    public void export(Workbook workbook, int tabType, List<Map<String, Object>> dataList, String orderType) {
        switch (tabType) {
            case 1: exportTab1(workbook, dataList); break;
            case 2: exportTab2(workbook, dataList); break;
            case 3: exportTab3(workbook, dataList, orderType); break;
            case 4: exportTab4(workbook, dataList, orderType); break;
            case 5: exportTab5(workbook, dataList); break;
            case 6: exportTab6(workbook, dataList, orderType); break;
            default: exportTab5(workbook, dataList); break;
        }
    }

    // ==================== 公共工具方法 ====================

    private CellStyle deepStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(COLOR_HEADER_DEEP, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setFont(s, wb, true, (short) 11);
        setBorder(s);
        return s;
    }

    private CellStyle lightStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(COLOR_HEADER_LIGHT, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setFont(s, wb, false, (short) 10);
        setBorder(s);
        return s;
    }

    private CellStyle whiteStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setFont(s, wb, false, (short) 10);
        setBorder(s);
        return s;
    }

    private CellStyle coloredStyle(Workbook wb, byte[] rgb, boolean bold) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(rgb, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private CellStyle dataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s);
        return s;
    }

    private void setFont(CellStyle s, Workbook wb, boolean bold, short size) {
        Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints(size);
        if (bold) f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
    }

    private void setBorder(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void writeCell(Sheet sheet, int row, int col, String val, CellStyle style) {
        Row r = sheet.getRow(row);
        if (r == null) r = sheet.createRow(row);
        Cell c = r.createCell(col);
        c.setCellValue(val);
        if (style != null) c.setCellStyle(style);
    }

    private void writeDataRow(Sheet sheet, int rowIdx, int startCol, List<String> keys, Map<String, Object> data, CellStyle style) {
        Row r = sheet.createRow(rowIdx);
        for (int i = 0; i < keys.size(); i++) {
            Cell c = r.createCell(startCol + i);
            Object val = data.get(keys.get(i));
            c.setCellValue(val != null ? val.toString() : "");
            if (style != null) c.setCellStyle(style);
        }
    }

    /**
     * 写 Tab1/Tab2 的共用复杂表头（3 层：其中正常关单5列 + 其中异常关单4列）
     * @param leftCols     左侧最固定列数（Tab1=1:地市, Tab2=3:地市/县分/客户经理名称）
     * @param extraLeftCols 左侧额外列数（在 rowspan 列与正常关单之间的列,Tab1=3:已派发量/已结束量/已结束占比,Tab2=3:预警工单量/已结束量/已结束占比）
     * @param rightCols    右侧固定列数（3:跟踪量/未处理量/未处理量占比）
     */
    private void writeCommon3RowHeader(Sheet sheet, int leftCols, int extraLeftCols, int rightCols,
                                        String[] leftTitles, String[] rightTitles,
                                        CellStyle deep, CellStyle light, CellStyle white) {
        int normalStart = leftCols + extraLeftCols;              // 正常关单开始列
        int normalEnd = normalStart + 4;                          // 正常关单结束列（5列）
        int abnormalStart = normalStart + 5;                     // 异常关单开始列
        int abnormalEnd = abnormalStart + 3;                     // 异常关单结束列（4列）
        int rightStart = abnormalStart + 4;                      // 右侧固定列开始

        // Row 0: 含合并的组头
        Row r0 = sheet.createRow(0);
        for (int i = 0; i < leftCols; i++)      writeCell(sheet, 0, i, leftTitles[i], deep);
        writeCell(sheet, 0, normalStart, "其中正常关单", deep);
        writeCell(sheet, 0, abnormalStart, "其中异常关单", deep);
        for (int i = 0; i < rightCols; i++)     writeCell(sheet, 0, rightStart + i, rightTitles[i], deep);
        // 合并组头单元格
        sheet.addMergedRegion(new CellRangeAddress(0, 0, normalStart, normalEnd));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, abnormalStart, abnormalEnd));
        // rowspan=3 的左侧固定列 + 额外左列 + 右列
        for (int i = 0; i < leftCols + extraLeftCols; i++)
            sheet.addMergedRegion(new CellRangeAddress(0, 2, i, i));
        for (int i = 0; i < rightCols; i++)
            sheet.addMergedRegion(new CellRangeAddress(0, 2, rightStart + i, rightStart + i));

        // Row 1
        Row r1 = sheet.createRow(1);
        writeCell(sheet, 1, normalStart, "客户经理处理", light);
        writeCell(sheet, 1, normalStart + 2, "正常智能关单", light);
        writeCell(sheet, 1, normalStart + 3, "合计", light);
        writeCell(sheet, 1, normalStart + 4, "占比", light);
        writeCell(sheet, 1, abnormalStart, "用户离网", light);
        writeCell(sheet, 1, abnormalStart + 1, "工单超时", light);
        writeCell(sheet, 1, abnormalStart + 2, "合计", light);
        writeCell(sheet, 1, abnormalStart + 3, "占比", light);
        // Row 2 行内合并：客户经理处理 colspan=2（只占 Row2，不跨 Row3）
        sheet.addMergedRegion(new CellRangeAddress(1, 1, normalStart, normalStart + 1));       // 客户经理处理(仅Row2)
        // Row 2-3 rowspan=2 的列：正常智能关单/合计/占比/用户离网/工单超时/合计/占比
        sheet.addMergedRegion(new CellRangeAddress(1, 2, normalStart + 2, normalStart + 2));   // 正常智能关单
        sheet.addMergedRegion(new CellRangeAddress(1, 2, normalStart + 3, normalStart + 3));   // 合计
        sheet.addMergedRegion(new CellRangeAddress(1, 2, normalStart + 4, normalStart + 4));   // 占比
        sheet.addMergedRegion(new CellRangeAddress(1, 2, abnormalStart, abnormalStart));       // 用户离网
        sheet.addMergedRegion(new CellRangeAddress(1, 2, abnormalStart + 1, abnormalStart + 1)); // 工单超时
        sheet.addMergedRegion(new CellRangeAddress(1, 2, abnormalStart + 2, abnormalStart + 2)); // 合计
        sheet.addMergedRegion(new CellRangeAddress(1, 2, abnormalStart + 3, abnormalStart + 3)); // 占比

        // Row 2: 叶子列
        writeCell(sheet, 2, normalStart, "手动处理", white);
        writeCell(sheet, 2, normalStart + 1, "批量处理", white);
    }

    /**
     * 给表头区域的空单元格补上边框（已有内容的单元格已有边框，不重复处理）
     */
    private void fillEmptyCells(Sheet sheet, int headerRows, int totalCols) {
        CellStyle bs = sheet.getWorkbook().createCellStyle();
        setBorderOnly(bs);
        bs.setAlignment(HorizontalAlignment.CENTER);
        bs.setVerticalAlignment(VerticalAlignment.CENTER);
        for (int r = 0; r < headerRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            for (int c = 0; c < totalCols; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) {
                    cell = row.createCell(c);
                    cell.setCellStyle(bs);
                }
            }
        }
    }

    private void setBorderOnly(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void setColWidths(Sheet sheet, int cols, int width) {
        for (int i = 0; i < cols; i++) sheet.setColumnWidth(i, width);
    }

    // ==================== Tab1: 已发工单整体情况 (16列, 列分组颜色) ====================
    private void exportTab1(Workbook wb, List<Map<String, Object>> list) {

        Sheet sheet = wb.createSheet("已发工单整体情况");
        CellStyle sGray = coloredStyle(wb, COLOR_GRAY, true);    // 浅灰：地市/已派发量
        CellStyle sBlue = coloredStyle(wb, COLOR_BLUE, true);    // 浅蓝：已结束量/已结束占比
        CellStyle sGreen = coloredStyle(wb, COLOR_GREEN, false); // 浅绿：正常关单组
        CellStyle sRed = coloredStyle(wb, COLOR_RED, false);     // 浅红：异常关单组
        CellStyle sYellow = coloredStyle(wb, COLOR_YELLOW, true);// 浅黄：未处理量/跟踪量/占比
        CellStyle dStyle = dataStyle(wb);

        // Row 0: group headers
        writeCell(sheet, 0, 0, "地市", sGray);
        writeCell(sheet, 0, 1, "已派发量", sGray);
        writeCell(sheet, 0, 2, "已结束量", sBlue);
        writeCell(sheet, 0, 3, "已结束占比", sBlue);
        writeCell(sheet, 0, 4, "其中正常关单", sGreen);
        writeCell(sheet, 0, 9, "其中异常关单", sRed);
        writeCell(sheet, 0, 13, "跟踪量", sYellow);
        writeCell(sheet, 0, 14, "未处理量", sYellow);
        writeCell(sheet, 0, 15, "未处理量占比", sYellow);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 8));   // 正常关单 E1:I1
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 12));  // 异常关单 J1:M1
        for (int c = 0; c <= 3; c++)  sheet.addMergedRegion(new CellRangeAddress(0, 2, c, c));
        for (int c = 13; c <= 15; c++) sheet.addMergedRegion(new CellRangeAddress(0, 2, c, c));

        // Row 1: sub-group headers
        writeCell(sheet, 1, 4, "客户经理处理", sGreen);
        writeCell(sheet, 1, 6, "正常智能关单", sGreen);
        writeCell(sheet, 1, 7, "合计", sGreen);
        writeCell(sheet, 1, 8, "占比", sGreen);
        writeCell(sheet, 1, 9, "用户离网", sRed);
        writeCell(sheet, 1, 10, "工单超时", sRed);
        writeCell(sheet, 1, 11, "合计", sRed);
        writeCell(sheet, 1, 12, "占比", sRed);

        sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5));   // 客户经理处理 (Row1 only)
        for (int c = 6; c <= 12; c++)
            sheet.addMergedRegion(new CellRangeAddress(1, 2, c, c));

        // Row 2: leaf headers
        writeCell(sheet, 2, 4, "手动处理", sGreen);
        writeCell(sheet, 2, 5, "批量处理", sGreen);

        List<String> keys = Arrays.asList(
            "areaName", "ypfl", "yjsl", "yjszb",
            "sdcl", "plcl", "zczngd", "zcgdhj", "zcgdzb",
            "yhlw", "gdcs", "ycgdhj", "ycgdzb",
            "gzl", "wjsl", "wjszb"
        );

        int headerRows = 3;
        fillEmptyCells(sheet, headerRows, 16);
        for (int i = 0; i < list.size(); i++)
            writeDataRow(sheet, headerRows + i, 0, keys, list.get(i), dStyle);

        setColWidths(sheet, 16, 4200);
    }

    // ==================== Tab2: 客户经理处理情况 (18列, 列分组颜色) ====================
    private void exportTab2(Workbook wb, List<Map<String, Object>> list) {

        Sheet sheet = wb.createSheet("客户经理处理情况");
        // 列分组颜色
        CellStyle sGray = coloredStyle(wb, COLOR_GRAY, true);    // 浅灰：地市县分客户经理预警工单量
        CellStyle sBlue = coloredStyle(wb, COLOR_BLUE, true);    // 浅蓝：已结束量 已结束占比
        CellStyle sGreen = coloredStyle(wb, COLOR_GREEN, false); // 浅绿：正常关单组
        CellStyle sRed = coloredStyle(wb, COLOR_RED, false);     // 浅红：异常关单组
        CellStyle sYellow = coloredStyle(wb, COLOR_YELLOW, true);// 浅黄：未处理量 跟踪量 占比
        CellStyle dStyle = dataStyle(wb);

        // ========== Row 0: group headers ==========
        writeCell(sheet, 0, 0, "地市", sGray);
        writeCell(sheet, 0, 1, "县分", sGray);
        writeCell(sheet, 0, 2, "客户经理名称", sGray);
        writeCell(sheet, 0, 3, "预警工单量", sGray);
        writeCell(sheet, 0, 4, "已结束量", sBlue);
        writeCell(sheet, 0, 5, "已结束占比", sBlue);
        writeCell(sheet, 0, 6, "其中正常关单", sGreen);
        writeCell(sheet, 0, 11, "其中异常关单", sRed);
        writeCell(sheet, 0, 15, "跟踪量", sYellow);
        writeCell(sheet, 0, 16, "未处理量", sYellow);
        writeCell(sheet, 0, 17, "未处理量占比", sYellow);

        // Merged Row 0 group headers
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 10));   // 正常关单 G1:K1
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 11, 14));  // 异常关单 L1:O1

        // Rowspan=3: cols 0-5 (地市..已结束占比) + cols 15-17
        for (int c = 0; c <= 5; c++)  sheet.addMergedRegion(new CellRangeAddress(0, 2, c, c));
        for (int c = 15; c <= 17; c++) sheet.addMergedRegion(new CellRangeAddress(0, 2, c, c));

        // ========== Row 1: sub-group headers ==========
        writeCell(sheet, 1, 6, "客户经理处理", sGreen);
        writeCell(sheet, 1, 8, "正常智能关单", sGreen);
        writeCell(sheet, 1, 9, "合计", sGreen);
        writeCell(sheet, 1, 10, "占比", sGreen);
        writeCell(sheet, 1, 11, "用户离网", sRed);
        writeCell(sheet, 1, 12, "工单超时", sRed);
        writeCell(sheet, 1, 13, "合计", sRed);
        writeCell(sheet, 1, 14, "占比", sRed);

        // Row 2 merge: 客户经理处理 (colspan=2, row 1 only)
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 6, 7));
        // Rowspan=2: 正常智能关单/合计/占比/用户离网/工单超时/合计/占比
        for (int c = 8; c <= 14; c++)
            sheet.addMergedRegion(new CellRangeAddress(1, 2, c, c));

        // ========== Row 2: leaf headers ==========
        writeCell(sheet, 2, 6, "手动处理", sGreen);
        writeCell(sheet, 2, 7, "批量处理", sGreen);

        List<String> keys = Arrays.asList(
            "areaName", "countyName", "managerName", "ypfl", "yjsl", "yjszb",
            "sdcl", "plcl", "zczngd", "zcgdhj", "zcgdzb",
            "yhlw", "gdcs", "ycgdhj", "ycgdzb",
            "gzl", "wjsl", "wjszb"
        );

        int headerRows = 3;
        fillEmptyCells(sheet, headerRows,18);
        for (int i = 0; i < list.size(); i++)
            writeDataRow(sheet, headerRows + i, 0, keys, list.get(i), dStyle);

        setColWidths(sheet, 18, 4000);
    }

    // ==================== Tab5: 未派发工单情况 (8列, 2层表头) ====================
    private void exportTab5(Workbook wb, List<Map<String, Object>> list) {

        Sheet sheet = wb.createSheet("未派发工单情况");
        CellStyle sGray = coloredStyle(wb, COLOR_GRAY, true);     // 浅灰：地市/预警工单量/涉及客户经理量
        CellStyle sYellow = coloredStyle(wb, COLOR_YELLOW, true); // 浅黄：已派单至客户经理组
        CellStyle sBlue = coloredStyle(wb, COLOR_BLUE, true);     // 浅蓝：未成功派单至客户经理组
        CellStyle dStyle = dataStyle(wb);

        // Row 0
        writeCell(sheet, 0, 0, "地市", sGray);
        writeCell(sheet, 0, 1, "预警工单量", sGray);
        writeCell(sheet, 0, 2, "涉及客户经理量", sGray);
        writeCell(sheet, 0, 3, "已派单至客户经理", sYellow);
        writeCell(sheet, 0, 5, "未成功派单至客户经理", sBlue);
        // rowspan=2: 左侧固定列
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
        // colspan=2: 已派单组头(Row0 only)
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));
        // colspan=3: 未派单组头(Row0 only)
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 7));

        // Row 1
        writeCell(sheet, 1, 3, "已派发量", sYellow);
        writeCell(sheet, 1, 4, "已派发占比", sYellow);
        writeCell(sheet, 1, 5, "未派发量", sBlue);
        writeCell(sheet, 1, 6, "未派发占比", sBlue);
        writeCell(sheet, 1, 7, "涉及客户经理量", sBlue);

        List<String> keys = Arrays.asList(
            "areaName", "gdl", "khjll", "ypfl", "ypfzb", "wpfl", "wpfzb", "wpfkhjll"
        );

        int headerRows = 2;
        fillEmptyCells(sheet, headerRows, 8);
        for (int i = 0; i < list.size(); i++)
            writeDataRow(sheet, headerRows + i, 0, keys, list.get(i), dStyle);

        setColWidths(sheet, 8, 4600);
    }

    // ==================== Tab3: 维系效果-预警维度 (动态列) ====================
    private void exportTab3(Workbook wb, List<Map<String, Object>> list, String orderType) {
        if (orderType == null || orderType.isEmpty()) {
            wb.createSheet("无数据-请先选择预警类型");
            return;
        }
        Sheet sheet = wb.createSheet("维系效果报表-预警维度");
        CellStyle deep = deepStyle(wb), dStyle = dataStyle(wb);
        writeSimpleExport(sheet, deep, dStyle, getTab3Titles(orderType), getTab3Keys(orderType), list);
    }

    // ==================== Tab4: 维系效果-业务维度 (动态列) ====================
    private void exportTab4(Workbook wb, List<Map<String, Object>> list, String orderType) {
        if (orderType == null || orderType.isEmpty()) {
            wb.createSheet("无数据-请先选择预警类型");
            return;
        }
        Sheet sheet = wb.createSheet("维系效果报表-业务维度");
        CellStyle deep = deepStyle(wb), dStyle = dataStyle(wb);
        writeSimpleExport(sheet, deep, dStyle, getTab4Titles(orderType), getTab4Keys(orderType), list);
    }

    // ==================== Tab6: 客户经理维系效果 (动态列, 2层表头) ====================
    private void exportTab6(Workbook wb, List<Map<String, Object>> list, String orderType) {
        if (orderType == null || orderType.isEmpty()) {
            wb.createSheet("无数据-请先选择预警类型");
            return;
        }
        Sheet sheet = wb.createSheet("客户经理维系效果");
        CellStyle deep = deepStyle(wb), light = lightStyle(wb), dStyle = dataStyle(wb);

        boolean hasCity = "Wide".equals(orderType) || "user_cust_owe_warn".equals(orderType) || "mnp".equals(orderType);
        boolean hasUserCount = "user_cust_owe_warn".equals(orderType);
        int fixCols = (hasCity ? 2 : 0) + 3 + (hasUserCount ? 1 : 0); // city,county?,manager,tel,warn[,userCount]
        int processStart = fixCols;  // 预警处理情况4列
        int effectStart = processStart + 4;
        List<String> effectTitles = getTab6EffectTitles(orderType);
        List<String> effectKeys = getTab6EffectKeys(orderType);

        // Row 0
        int col = 0;
        if (hasCity) { writeCell(sheet, 0, col, "地市", deep); col++; writeCell(sheet, 0, col, "县分", deep); col++; }
        writeCell(sheet, 0, col++, "客户经理", deep);
        writeCell(sheet, 0, col++, "联系电话", deep);
        writeCell(sheet, 0, col++, "预警量", deep);
        if (hasUserCount) writeCell(sheet, 0, col++, "涉及用户量", deep);
        writeCell(sheet, 0, processStart, "预警处理情况", deep);
        writeCell(sheet, 0, effectStart, "维系效果", deep);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, processStart, processStart + 3));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, effectStart, effectStart + effectTitles.size() - 1));
        for (int i = 0; i < fixCols; i++) sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));

        // Row 1
        Row r1 = sheet.createRow(1);
        writeCell(sheet, 1, processStart, "正常关单量", light);
        writeCell(sheet, 1, processStart + 1, "异常关单量", light);
        writeCell(sheet, 1, processStart + 2, "跟踪量", light);
        writeCell(sheet, 1, processStart + 3, "未处理量", light);
        for (int i = 0; i < effectTitles.size(); i++)
            writeCell(sheet, 1, effectStart + i, effectTitles.get(i), light);

        // Build key list
        List<String> allKeys = new ArrayList<>();
        if (hasCity) { allKeys.add("areaName"); allKeys.add("countyName"); }
        allKeys.add("managerName");
        allKeys.add("relationTel");
        allKeys.add("warnCount");
        if (hasUserCount) allKeys.add("userCount");
        allKeys.add("normalClose");
        allKeys.add("abnormalClose");
        allKeys.add("tracking");
        allKeys.add("untreated");
        allKeys.addAll(effectKeys);

        int headerRows = 2;
        fillEmptyCells(sheet, headerRows,allKeys.size());
        for (int i = 0; i < list.size(); i++)
            writeDataRow(sheet, headerRows + i, 0, allKeys, list.get(i), dStyle);

        setColWidths(sheet, allKeys.size(), 4000);
    }

    // ==================== Tab3/4 简单表头导出 helper ====================
    private void writeSimpleExport(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle,
                                    String[] titles, String[] keys, List<Map<String, Object>> list) {
        Row hr = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            Cell c = hr.createCell(i);
            c.setCellValue(titles[i]);
            if (headerStyle != null) c.setCellStyle(headerStyle);
        }
        for (int r = 0; r < list.size(); r++) {
            Row dr = sheet.createRow(r + 1);
            for (int c = 0; c < keys.length; c++) {
                Object val = list.get(r).get(keys[c]);
                Cell cell = dr.createCell(c);
                cell.setCellValue(val != null ? val.toString() : "");
                if (dataStyle != null) cell.setCellStyle(dataStyle);
            }
        }
        setColWidths(sheet, titles.length, 4600);
    }

    // ==================== Tab3/Tab4/Tab6 动态列标题和 key ====================

    private String[] getTab3Titles(String orderType) {
        switch (orderType) {
            case "mnp": return new String[]{"地市", "预警量", "在网量", "在网率"};
            case "hmd_warn": case "special_activity_warn": return new String[]{"地市", "预警量", "续约量", "续约率"};
            case "group_buy_warn": case "terminal_contract_warn": return new String[]{"地市", "预警用户数", "续约量", "续约率"};
            case "Wide": case "double_wide": return new String[]{"地市", "预警量", "续约量", "续约率", "续约年费（万元）"};
            case "third_check_warn": return new String[]{"地市", "预警量", "整改量", "整改率"};
            case "cloud_expire_alarm": return new String[]{"地市", "预警量", "续约量", "续约率", "预计年收入（万元）"};
            case "qf_warn": case "user_cust_owe_warn": return new String[]{"地市", "预警量", "逾期欠费（万元）", "欠费回收（万元）", "回款率"};
            case "cust_receipt_warn": return new String[]{"地市", "预警量", "发票金额（万元）", "转正量", "转正金额（万元）（含发票未转正但已部分销账）", "转正率", "转正金额占比"};
            case "htdq_warn": return new String[]{"地市", "预警量", "续约量", "续约率", "续约合同金额（万元）"};
            default: return new String[]{"地市", "预警量", "数量", "占比"};
        }
    }

    private String[] getTab3Keys(String orderType) {
        switch (orderType) {
            case "mnp": return new String[]{"areaName", "yjl", "number", "zb"};
            case "hmd_warn": case "special_activity_warn": case "group_buy_warn": case "terminal_contract_warn":
                return new String[]{"areaName", "yjl", "number", "zb"};
            case "Wide": case "double_wide": return new String[]{"areaName", "yjl", "number", "zb", "xynf"};
            case "third_check_warn": return new String[]{"areaName", "yjl", "number", "zb"};
            case "cloud_expire_alarm": return new String[]{"areaName", "yjl", "number", "zb", "yjnsr"};
            case "qf_warn": case "user_cust_owe_warn": return new String[]{"areaName", "yjl", "number", "qfhs", "hkl"};
            case "cust_receipt_warn": return new String[]{"areaName", "yjl", "number", "zzl", "zzje", "zzlv", "zzjezb"};
            case "htdq_warn": return new String[]{"areaName", "yjl", "number", "xyl", "htje"};
            default: return new String[]{"areaName", "yjl", "number", "zb"};
        }
    }

    private String[] getTab4Titles(String orderType) {
        if ("double_zero_warn".equals(orderType) || "pcdn_warn".equals(orderType))
            return new String[]{"地市", "预警量", "异常用户量", "占比"};
        if ("cut_over_warn".equals(orderType))
            return new String[]{"地市", "预警量", "处理量", "处理率"};
        if ("leaseline_relet".equals(orderType))
            return new String[]{"地市", "预警用户数", "起租量", "起租率"};
        if ("cust_receipt_warn".equals(orderType))
            return new String[]{"地市", "发票打印量", "发票金额（万元）", "转正量", "转正金额（万元）（含发票未转正但已部分销账）", "转正率", "转正金额占比"};
        // Others same as Tab3
        return getTab3Titles(orderType);
    }

    private String[] getTab4Keys(String orderType) {
        if ("double_zero_warn".equals(orderType) || "pcdn_warn".equals(orderType))
            return new String[]{"areaName", "yjl", "number", "zb"};
        if ("cut_over_warn".equals(orderType) || "leaseline_relet".equals(orderType))
            return new String[]{"areaName", "yjl", "number", "zb"};
        return getTab3Keys(orderType);
    }

    private List<String> getTab6EffectTitles(String orderType) {
        switch (orderType) {
            case "Wide": return Arrays.asList("续约量", "续约率", "续约年费（万元）", "排名");
            case "mnp": return Arrays.asList("在网量", "在网率", "排名");
            case "qf_warn": case "user_cust_owe_warn": return Arrays.asList("逾期欠费（万元）", "欠费回收（万元）", "回款率", "排名");
            default: return Arrays.asList("排名");
        }
    }

    private List<String> getTab6EffectKeys(String orderType) {
        switch (orderType) {
            case "Wide": return Arrays.asList("xyCount", "xyRate", "xyFee", "rank");
            case "mnp": return Arrays.asList("onlineCount", "onlineRate", "rank");
            case "qf_warn": case "user_cust_owe_warn": return Arrays.asList("overdueFee", "recoveryFee", "recoveryRate", "rank");
            default: return Arrays.asList("rank");
        }
    }
}
