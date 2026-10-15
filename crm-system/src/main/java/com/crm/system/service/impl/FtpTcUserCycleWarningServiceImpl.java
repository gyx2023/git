package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.FtpTcUserCycleWarningDo;
import com.crm.system.mapper.FtpTcUserCycleWarningMapper;
import com.crm.system.service.IFtpTcUserCycleWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户级闭环预警 Service业务层处理
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpTcUserCycleWarningServiceImpl
        extends ServiceImpl<FtpTcUserCycleWarningMapper, FtpTcUserCycleWarningDo>
        implements IFtpTcUserCycleWarningService {

    @Override
    public void insertBatch(List<FtpTcUserCycleWarningDo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        baseMapper.insertBatch(list);
    }

    @Override
    public FtpTcUserCycleWarningDo getLatestByOrderFlowNo(String orderFlowNo) {
        if (orderFlowNo == null || orderFlowNo.isEmpty()) {
            return null;
        }
        return lambdaQuery()
                .eq(FtpTcUserCycleWarningDo::getOrderFlowNo, orderFlowNo)
                .orderByDesc(FtpTcUserCycleWarningDo::getId)
                .last("LIMIT 1")
                .one();
    }
}
