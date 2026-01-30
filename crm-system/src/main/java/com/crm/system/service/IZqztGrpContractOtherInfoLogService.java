package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoLogBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoLogVo;

import java.util.Collection;
import java.util.List;

/**
 * 集团合同扩展信息日志Service接口
 *
 * @author 万超凡
 * @date 2026-01-15
 */
public interface IZqztGrpContractOtherInfoLogService {

    /**
     * 查询集团合同扩展信息日志
     */
    ZqztGrpContractOtherInfoLogVo queryById(String contractId);

    /**
     * 查询集团合同扩展信息日志列表
     */
    TableDataInfo<ZqztGrpContractOtherInfoLogVo> queryPageList(ZqztGrpContractOtherInfoLogBo bo, PageQuery pageQuery);

    /**
     * 查询集团合同扩展信息日志列表
     */
    List<ZqztGrpContractOtherInfoLogVo> queryList(ZqztGrpContractOtherInfoLogBo bo);

    /**
     * 新增集团合同扩展信息日志
     */
    Boolean insertByBo(ZqztGrpContractOtherInfoLogBo bo);

    /**
     * 修改集团合同扩展信息日志
     */
    Boolean updateByBo(ZqztGrpContractOtherInfoLogBo bo);

    /**
     * 校验并批量删除集团合同扩展信息日志信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}
