package com.crm.system.service.impl;

import com.crm.common.constant.HttpStatus;
import com.crm.common.exception.ServiceException;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;
import com.crm.system.mapper.ZqztOrderInfoObMapper;
import com.crm.system.service.IZqztOrderInfoObService;
import com.crm.system.utils.ZqztLedgerObMergeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 总部OB工单自动带出 服务实现
 * 同一工单可能存在于 sl08md 或 04049f，各查一张表后在 Java 层合并
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztOrderInfoObServiceImpl implements IZqztOrderInfoObService {

    private final ZqztOrderInfoObMapper orderInfoObMapper;

    @Override
    public ZqztLedgerInfoVo getOrderInfo(String orderNo) {
        try {
            ZqztOrderInfoObVo t1 = first(orderInfoObMapper.selectFromSl08md(orderNo));
            ZqztOrderInfoObVo t2 = first(orderInfoObMapper.selectFrom04049f(orderNo));
            if (t1 == null && t2 == null) {
                throw new ServiceException("未查询到该工单对应的企业信息", HttpStatus.WARN);
            }
            return ZqztLedgerObMergeUtil.merge(t1, t2);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("工单自动填充失败, orderNo={}", orderNo, e);
            throw new ServiceException("自动填充失败，请稍后再试或手动填充", HttpStatus.WARN);
        }
    }

    private ZqztOrderInfoObVo first(List<ZqztOrderInfoObVo> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}
