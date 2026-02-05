package com.crm.common.utils;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author FBODC
 */
public class WordTextExtractor {

    /**
     * 统一入口：读取 doc 或 docx 文档的文本内容
     *
     * @param inputStream 文档路径
     * @return 提取的文本
     */
    public static StringBuilder readWordText(FileInputStream inputStream, String fileName) {
        if (inputStream == null || !fileName.matches(".*\\.(doc|docx)$")) {
            throw new IllegalArgumentException("仅支持 .doc 或 .docx 格式文档");
        }
        if (fileName.endsWith(".docx")) {
            return analysisDocxFile(inputStream);
        } else {
            return analysisDocFile(inputStream);
        }
    }

    /**
     * 处理 .docx 格式（原逻辑优化）
     */
    private static StringBuilder analysisDocxFile(InputStream fis) {
        try {
            XWPFDocument document = new XWPFDocument(fis);
            StringBuilder text = new StringBuilder();
            // 提取正文段落
            getTextFromDocxParagraphs(document, text);
            // 提取表格内容
            getTextFromDocxTables(document, text);
            // 提取页眉页脚
            getTextFromDocxHeadersFooters(document, text);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder();
    }

    /**
     * 处理 .doc 格式（HWPF 实现）
     */
    private static StringBuilder analysisDocFile(InputStream fis) {
        try {
            HWPFDocument document = new HWPFDocument(fis);
            StringBuilder text = new StringBuilder();
            // 提取正文段落
            getTextFromDocParagraphs(document, text);
            // 提取表格内容
            getTextFromDocTables(document, text);
            // 简易提取页眉页脚（HWPF 对页眉页脚支持有限）
            getTextFromDocHeadersFooters(document, text);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder();
    }

    // ====================== .docx 文本提取 ======================
    private static void getTextFromDocxParagraphs(XWPFDocument doc, StringBuilder text) {
        List<XWPFParagraph> paragraphs = doc.getParagraphs();
        for (XWPFParagraph para : paragraphs) {
            String paraText = para.getText();
            if (paraText != null && !paraText.trim().isEmpty()) {
                text.append(paraText.trim()).append("\n");
            }
        }
    }

    private static void getTextFromDocxTables(XWPFDocument doc, StringBuilder text) {
        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        String cellText = p.getText();
                        if (cellText != null && !cellText.trim().isEmpty()) {
                            text.append(cellText.trim()).append("\t"); // 表格单元格用制表符分隔
                        }
                    }
                }
                text.append("\n"); // 行结束
            }
        }
    }

    private static void getTextFromDocxHeadersFooters(XWPFDocument doc, StringBuilder text) {
        try {
            // 读取页眉
            if (doc.getHeaderList() != null) {
                for (XWPFHeader header : doc.getHeaderList()) {
                    if (header != null) {
                        for (XWPFParagraph p : header.getParagraphs()) {
                            String headerText = p.getText();
                            if (headerText != null && !headerText.trim().isEmpty()) {
                                text.append("[页眉]").append(headerText.trim()).append("\n");
                            }
                        }
                    }
                }
            }

            // 读取页脚
            if (doc.getFooterList() != null) {
                for (XWPFFooter footer : doc.getFooterList()) {
                    if (footer != null) {
                        for (XWPFParagraph p : footer.getParagraphs()) {
                            String footerText = p.getText();
                            if (footerText != null && !footerText.trim().isEmpty()) {
                                text.append("[页脚]").append(footerText.trim()).append("\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== .doc 文本提取 ======================
    private static void getTextFromDocParagraphs(HWPFDocument doc, StringBuilder text) {
        // 方式1：逐段落读取（更精准）
        org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph para = range.getParagraph(i);
            String paraText = para.text();
            if (paraText != null && !paraText.trim().isEmpty()) {
                text.append(paraText.trim()).append("\n");
            }
        }

        // 方式2：简易提取（备用，适合纯文本）
        // WordExtractor extractor = new WordExtractor(doc);
        // text.append(extractor.getText()).append("\n");
    }


    private static void getTextFromDocTables(HWPFDocument doc, StringBuilder text) {
        org.apache.poi.hwpf.usermodel.Range range = doc.getRange();
        for (int i = 0; i < range.numParagraphs(); i++) {
            Paragraph paragraph = range.getParagraph(i);
            // 判断段落是否包含表格
            if (paragraph.isInTable()) {
                try {
                    // 获取段落所在的表格
                    Table table = range.getTable(paragraph);
                    for (int j = 0; j < table.numRows(); j++) {
                        TableRow row = table.getRow(j);
                        for (int k = 0; k < row.numCells(); k++) {
                            TableCell cell = row.getCell(k);
                            String cellText = cell.text();
                            if (cellText != null && !cellText.trim().isEmpty()) {
                                text.append(cellText.trim()).append("\t");
                            }
                        }
                        text.append("\n");
                    }
                    // 跳过该段落（因为它属于表格）
                    continue;
                } catch (Exception e) {
                    // 如果获取表格出错，则按普通段落处理
                    String paraText = paragraph.text();
                    if (paraText != null && !paraText.trim().isEmpty()) {
                        text.append(paraText.trim()).append("\n");
                    }
                }
            } else {
                // 普通段落
                String paraText = paragraph.text();
                if (paraText != null && !paraText.trim().isEmpty()) {
                    text.append(paraText.trim()).append("\n");
                }
            }
        }
    }

    private static void getTextFromDocHeadersFooters(HWPFDocument doc, StringBuilder text) {
        // HWPF 对页眉页脚的支持较弱，需通过 Range 定位
        try {
            // 获取页眉 Range（0 代表页眉区域）
            org.apache.poi.hwpf.usermodel.Range headerRange = doc.getHeaderStoryRange();
            if (headerRange != null) {
                String headerText = headerRange.text();
                if (headerText != null && !headerText.trim().isEmpty()) {
                    text.append("[页眉]").append(headerText.trim()).append("\n");
                }
            }
            // 获取页脚 Range
            org.apache.poi.hwpf.usermodel.Range footerRange = doc.getFootnoteRange();
            if (footerRange != null) {
                String footerText = footerRange.text();
                if (footerText != null && !footerText.trim().isEmpty()) {
                    text.append("[页脚]").append(footerText.trim()).append("\n");
                }
            }
        } catch (Exception e) {
            // 部分老版本 doc 可能无页眉页脚，忽略异常
            e.printStackTrace();
        }
    }

    // 测试示例
//    public static void main(String[] args) {
//        WordTextExtractor extractor = new WordTextExtractor();
//        // 测试 doc
//        StringBuilder docText = extractor.readWordText("C:\\RPA\\湖州南浔区教育局三清单-南浔（12.7）.doc");
//        System.out.println("=== .doc 文本内容 ===");
//        System.out.println(docText);
//
//        // 测试 docx
//        StringBuilder docxText = extractor.readWordText("test.docx");
//        System.out.println("=== .docx 文本内容 ===");
//        System.out.println(docxText);
//    }
}
