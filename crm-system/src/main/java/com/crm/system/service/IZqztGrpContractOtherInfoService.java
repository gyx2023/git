package com.crm.system.service;

import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 集团合同扩展信息Service接口
 *
 * @author crm
 * @date 2026-01-13
 */
public interface IZqztGrpContractOtherInfoService {

    /**
     * 查询集团合同扩展信息
     */
    ZqztGrpContractOtherInfoVo queryById(String contractId);

    /**
     * 查询集团合同扩展信息列表
     */
    TableDataInfo<ZqztGrpContractOtherInfoVo> queryPageList(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery);

    /**
     * 查询集团合同扩展信息列表
     */
    List<ZqztGrpContractOtherInfoVo> queryList(ZqztGrpContractOtherInfoBo bo);

    /**
     * 新增集团合同扩展信息
     */
    Boolean insertByBo(ZqztGrpContractOtherInfoBo bo);

    /**
     * 修改集团合同扩展信息
     */
    Boolean updateByBo(ZqztGrpContractOtherInfoBo bo);

    /**
     * 校验并批量删除集团合同扩展信息信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}
