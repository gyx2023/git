package com.crm.askdata.exception;

/**
 * 元数据异常
 * <p>
 * 陷阱防护：工具参数幻觉
 * 元数据校验失败、指标/维度不存在时抛出此异常
 *
 * @author crm
 */
public class MetadataException extends RuntimeException {

    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
