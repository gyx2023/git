package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.FtpTcUserCustWarningDo;
import com.crm.system.mapper.FtpTcUserCustWarningMapper;
import com.crm.system.service.IFtpTcUserCustWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户级通用预警 Service业务层处理
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpTcUserCustWarningServiceImpl
        extends ServiceImpl<FtpTcUserCustWarningMapper, FtpTcUserCustWarningDo>
        implements IFtpTcUserCustWarningService {

    @Override
    public void insertBatch(List<FtpTcUserCustWarningDo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        baseMapper.insertBatch(list);
    }

    @Override
    public FtpTcUserCustWarningDo getByMainOrderFlowNo(String mainOrderFlowNo) {
        if (mainOrderFlowNo == null || mainOrderFlowNo.isEmpty()) {
            return null;
        }
        return lambdaQuery()
                .eq(FtpTcUserCustWarningDo::getMainOrderFlowNo, mainOrderFlowNo)
                .last("LIMIT 1")
                .one();
    }
}
