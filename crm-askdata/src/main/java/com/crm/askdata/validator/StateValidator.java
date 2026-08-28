package com.crm.askdata.validator;

import com.crm.askdata.exception.StateConflictException;
import com.crm.askdata.model.ConversationSession;
import org.springframework.stereotype.Component;

/**
 * 状态校验器
 * <p>
 * 陷阱防护：状态不一致陷阱
 * 通过版本号乐观锁防止并发冲突
 *
 * @author crm
 */
@Component
public class StateValidator {

    /**
     * 校验会话版本
     *
     * @param clientVersion 客户端传入的版本
     * @param session       当前会话
     * @throws StateConflictException 版本不一致时抛出
     */
    public void validateSessionVersion(Long clientVersion, ConversationSession session) {
        if (session == null) {
            throw new StateConflictException("会话不存在");
        }

        if (clientVersion == null) {
            // 首次请求允许
            return;
        }

        if (!session.getVersion().equals(clientVersion)) {
            throw new StateConflictException(
                String.format("会话版本冲突：client=%d, server=%d",
                    clientVersion, session.getVersion())
            );
        }
    }
}
