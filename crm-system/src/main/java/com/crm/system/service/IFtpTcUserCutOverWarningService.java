package com.crm.system.service;

import com.crm.system.domain.dao.FtpTcUserCutOverWarningDo;

import java.util.List;

/**
 * 用户级割接预警 Service接口
 *
 * @author crm
 */
public interface IFtpTcUserCutOverWarningService {

    /**
     * 批量插入用户级割接预警数据
     *
     * @param list 数据列表
     */
    void insertBatch(List<FtpTcUserCutOverWarningDo> list);
}
