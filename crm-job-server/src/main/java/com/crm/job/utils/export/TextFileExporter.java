package com.crm.job.utils.export;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文本文件导出模板
 * <p>
 * 定义导出文件的标准化生命周期：
 * <ol>
 *   <li>createFile — 创建文件 + 初始化上下文</li>
 *   <li>writeHeader — 写表头（可选）</li>
 *   <li>writeBody — 写数据体（核心逻辑）</li>
 *   <li>writeEnd — 写汇总尾行（可选）</li>
 *   <li>finishWriteFileInternal — 收尾：上传/重命名等</li>
 *   <li>cleanup — 清理临时文件（在 finally 中执行）</li>
 * </ol>
 * </p>
 *
 * @param <T> 文件上下文类型，继承 {@link TextFileExportContext}
 */
@Slf4j
public class TextFileExporter<T extends TextFileExportContext> {

    /**
     * 模板方法：执行文件导出的完整生命周期
     *
     * @param context 文件上下文
     * @return 执行完成后的上下文
     */
    public final T execute(T context) {
        long startTime = System.currentTimeMillis();
        context.setSuccess(false);

        try {
            context = createFile(context);
            log.info("文件导出开始: {}", context.getFileName());

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(Files.newOutputStream(context.getLocalFile().toPath()), StandardCharsets.UTF_8))) {
                writeHeader(context, writer);
                writeBody(context, writer);
                writeEnd(context, writer);
            }

            SummaryHolder summary = new SummaryHolder();
            summary.setTotalRows(context.getTotalRows());
            summary.setSuccessRows(context.getTotalRows());

            finishWriteFileInternal(context, summary);
            context.setSuccess(true);

            long cost = System.currentTimeMillis() - startTime;
            log.info("文件导出完成: {}, {} 行, 耗时 {}ms", context.getFileName(), context.getTotalRows(), cost);
        } catch (Exception e) {
            context.setSuccess(false);
            log.error("文件导出失败: {}", context.getFileName(), e);
            throw new RuntimeException("文件导出失败: " + context.getFileName(), e);
        } finally {
            cleanup(context);
        }

        return context;
    }

    /**
     * 创建文件并初始化上下文
     */
    protected T createFile(T context) {
        File dir = new File(context.getFilePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        context.setLocalFile(new File(dir, context.getFileName()));
        return context;
    }

    /**
     * 写表头行（可选）
     */
    protected void writeHeader(T context, BufferedWriter writer) throws IOException {
    }

    /**
     * 写数据体（可选，子类按需覆盖）
     */
    protected void writeBody(T context, BufferedWriter writer) throws IOException {
    }

    /**
     * 写汇总尾行（可选）
     */
    protected void writeEnd(T context, BufferedWriter writer) throws IOException {
    }

    /**
     * 收尾钩子：上传 / 重命名等
     */
    protected void finishWriteFileInternal(T context, SummaryHolder summary) {
    }

    /**
     * 清理钩子：在 finally 中执行，默认删除本地临时文件
     */
    protected void cleanup(T context) {
        if (context.getLocalFile() != null && context.getLocalFile().exists()) {
            if (context.getLocalFile().delete()) {
                log.debug("临时文件已删除: {}", context.getLocalFile().getAbsolutePath());
            } else {
                log.warn("临时文件删除失败: {}", context.getLocalFile().getAbsolutePath());
            }
        }
    }
}
