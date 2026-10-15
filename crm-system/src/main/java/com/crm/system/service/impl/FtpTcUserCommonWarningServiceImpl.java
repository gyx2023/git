package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.FtpTcUserCommonWarningDo;
import com.crm.system.mapper.FtpTcUserCommonWarningMapper;
import com.crm.system.service.IFtpTcUserCommonWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户级通用预警 Service业务层处理
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpTcUserCommonWarningServiceImpl
        extends ServiceImpl<FtpTcUserCommonWarningMapper, FtpTcUserCommonWarningDo>
        implements IFtpTcUserCommonWarningService {

    @Override
    public void insertBatch(List<FtpTcUserCommonWarningDo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        baseMapper.insertBatch(list);
    }

    @Override
    public List<FtpTcUserCommonWarningDo> listByMainOrderFlowNo(String mainOrderFlowNo) {
        if (mainOrderFlowNo == null || mainOrderFlowNo.isEmpty()) {
            return new ArrayList<>();
        }
        return lambdaQuery()
                .eq(FtpTcUserCommonWarningDo::getMainOrderFlowNo, mainOrderFlowNo)
                .orderByAsc(FtpTcUserCommonWarningDo::getId)
                .list();
    }

    @Override
    public FtpTcUserCommonWarningDo getByOrderFlowNo(String orderFlowNo) {
        if (orderFlowNo == null || orderFlowNo.isEmpty()) {
            return null;
        }
        return lambdaQuery()
                .eq(FtpTcUserCommonWarningDo::getOrderFlowNo, orderFlowNo)
                .last("LIMIT 1")
                .one();
    }
}
