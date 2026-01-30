package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.ZqztGrpContractInfoLogBo;
import com.crm.system.domain.vo.ZqztGrpContractInfoLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 集团合同信息日志Service接口
 *
 * @author 万超凡
 * @date 2026-01-16
 */
public interface IZqztGrpContractInfoLogService {

    /**
     * 查询集团合同信息日志
     */
    ZqztGrpContractInfoLogVo queryById(String contractId);

    /**
     * 查询集团合同信息日志列表
     */
    TableDataInfo<ZqztGrpContractInfoLogVo> queryPageList(ZqztGrpContractInfoLogBo bo, PageQuery pageQuery);

    /**
     * 查询集团合同信息日志列表
     */
    List<ZqztGrpContractInfoLogVo> queryList(ZqztGrpContractInfoLogBo bo);

    /**
     * 新增集团合同信息日志
     */
    Boolean insertByBo(ZqztGrpContractInfoLogBo bo);

    /**
     * 修改集团合同信息日志
     */
    Boolean updateByBo(ZqztGrpContractInfoLogBo bo);

    /**
     * 校验并批量删除集团合同信息日志信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}
