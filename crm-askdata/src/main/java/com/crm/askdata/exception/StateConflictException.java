package com.crm.askdata.exception;

/**
 * 状态冲突异常
 * <p>
 * 陷阱防护：状态不一致陷阱
 * 通过版本号乐观锁防止并发冲突，版本不一致时抛出此异常
 *
 * @author crm
 */
public class StateConflictException extends RuntimeException {

    public StateConflictException(String message) {
        super(message);
    }

    public StateConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
