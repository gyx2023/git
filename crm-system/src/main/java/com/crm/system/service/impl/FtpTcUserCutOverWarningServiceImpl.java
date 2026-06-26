package com.crm.system.service.impl;

import com.crm.system.domain.dao.FtpTcUserCutOverWarningDo;
import com.crm.system.mapper.FtpTcUserCutOverWarningMapper;
import com.crm.system.service.IFtpTcUserCutOverWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户级割接预警 Service业务层处理
 *
 * @author crm
 */
@Slf4j
@Service
public class FtpTcUserCutOverWarningServiceImpl implements IFtpTcUserCutOverWarningService {

    @Resource
    private FtpTcUserCutOverWarningMapper ftpTcUserCutOverWarningMapper;

    @Override
    public void insertBatch(List<FtpTcUserCutOverWarningDo> list) {
        ftpTcUserCutOverWarningMapper.insertBatch(list);
    }
}
