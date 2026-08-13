package com.crm.system.service.topp.impl;

import com.crm.system.domain.dao.topp.ToppOrderUp;
import com.crm.system.mapper.topp.ToppOrderUpMapper;
import com.crm.system.service.topp.IToppOrderUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 升级预警订单关联表 Service 实现类
 *
 * @author crm
 * @date 2026-06-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToppOrderUpServiceImpl implements IToppOrderUpService {

    private final ToppOrderUpMapper toppOrderUpMapper;

    @Override
    public boolean insertToppOrderUp(ToppOrderUp orderUp) {
        try {
            int result = toppOrderUpMapper.insert(orderUp);
            log.info("【升级预警关联表】保存成功, orderId={}, bssCode={}, mdmOrgCode={}, period={}",
                    orderUp.getOrderId(), orderUp.getBssCode(), orderUp.getMdmOrgCode(), orderUp.getPeriod());
            return result > 0;
        } catch (Exception e) {
            log.error("【升级预警关联表】保存失败, orderId={}", orderUp.getOrderId(), e);
            return false;
        }
    }
}
