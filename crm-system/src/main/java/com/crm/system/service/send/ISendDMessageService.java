package com.crm.system.service.send;

import java.util.List;

public interface ISendDMessageService {
    void initNoticeMessage(List<Integer> userIds, String message);
}
