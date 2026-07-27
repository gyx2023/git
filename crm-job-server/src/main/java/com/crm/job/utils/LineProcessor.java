package com.crm.job.utils;

/**
 * 大文件行处理器接口
 * 用于处理按行读取的文件内容
 */
@FunctionalInterface
interface LineProcessor {
    /**
     * 处理一行数据
     * @param line 行数据
     * @param lineNumber 行号
     * @return 是否继续处理下一行
     */
    boolean process(String line, long lineNumber);
}
