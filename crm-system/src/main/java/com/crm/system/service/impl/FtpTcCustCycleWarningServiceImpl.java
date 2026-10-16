package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.system.domain.dao.FtpTcCustCycleWarningDo;
import com.crm.system.mapper.FtpTcCustCycleWarningMapper;
import com.crm.system.service.IFtpTcCustCycleWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户级闭环预警 Service业务层处理
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpTcCustCycleWarningServiceImpl
        extends ServiceImpl<FtpTcCustCycleWarningMapper, FtpTcCustCycleWarningDo>
        implements IFtpTcCustCycleWarningService {

    @Override
    public void insertBatch(List<FtpTcCustCycleWarningDo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        baseMapper.insertBatch(list);
    }

    @Override
    public FtpTcCustCycleWarningDo getLatestByMainOrderFlowNo(String mainOrderFlowNo) {
        if (mainOrderFlowNo == null || mainOrderFlowNo.isEmpty()) {
            return null;
        }
        return lambdaQuery()
                .eq(FtpTcCustCycleWarningDo::getMainOrderFlowNo, mainOrderFlowNo)
                .orderByDesc(FtpTcCustCycleWarningDo::getId)
                .last("LIMIT 1")
                .one();
    }
}
