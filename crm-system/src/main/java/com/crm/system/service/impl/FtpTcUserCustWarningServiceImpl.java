package com.crm.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.FtpTcUserCustWarningDo;
import com.crm.system.mapper.FtpTcUserCustWarningMapper;
import com.crm.system.service.IFtpTcUserCustWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public String getMainOrderFlowNoById(Long id) {
        if (id == null) {
            return null;
        }
        // 主键查询，最多命中一行，无需 LIMIT
        FtpTcUserCustWarningDo row = lambdaQuery()
                .select(FtpTcUserCustWarningDo::getMainOrderFlowNo)
                .eq(FtpTcUserCustWarningDo::getId, id)
                .one();
        return row == null ? null : row.getMainOrderFlowNo();
    }

    @Override
    public FtpTcUserCustWarningDo getByIdAndWarningType(Long id, String warningType) {
        if (id == null) {
            return null;
        }
        // 主键查询，最多命中一行，无需 LIMIT
        return lambdaQuery()
                .eq(FtpTcUserCustWarningDo::getId, id)
                .eq(StringUtils.isNotBlank(warningType), FtpTcUserCustWarningDo::getWarningType, warningType)
                .one();
    }

    @Override
    public List<FtpTcUserCustWarningDo> listByWarningTypeAndCreateDate(String warningType, String yyyyMMdd) {
        if (StringUtils.isBlank(warningType) || StringUtils.isBlank(yyyyMMdd)) {
            return new ArrayList<>();
        }
        return lambdaQuery()
                .eq(FtpTcUserCustWarningDo::getWarningType, warningType)
                // 与原 Mapper 写法逐字一致：DATE_FORMAT(create_date,'%Y%m%d') = #{targetDate}
                .apply("DATE_FORMAT(create_date, '%Y%m%d') = {0}", yyyyMMdd)
                .orderByAsc(FtpTcUserCustWarningDo::getId)
                .list();
    }
}
