package com.crm.system.service.rpa;

import com.crm.system.domain.dao.rpa.RpaErrorSaveDo;

/**
 * RPA 错误信息服务接口。
 */
public interface IRpaErrorSaveService {

    /**
     * 保存 RPA 上报的错误信息。
     *
     * @param rpaErrorSave 错误信息
     * @return 受影响行数
     */
    int insertRpaErrorSave(RpaErrorSaveDo rpaErrorSave);
}
