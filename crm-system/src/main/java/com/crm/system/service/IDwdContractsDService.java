package com.crm.system.service;

import com.crm.system.domain.vo.DwdContractsDVo;
import com.crm.system.domain.bo.DwdContractsDBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.List;

/**
 * DWD_CONTRACTS_D 服务层
 *
 * @author 万超凡
 * @date 2026-01-19
 */
public interface IDwdContractsDService {

    /**
     * 根据合同编号查询DWD_CONTRACTS_D
     *
     * @param contractNoLocal 合同编号
     * @return DWD_CONTRACTS_D
     */
    DwdContractsDVo selectDwdContractsDById(String contractNoLocal);

    /**
     * 查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return DWD_CONTRACTS_D集合
     */
    List<DwdContractsDVo> selectDwdContractsDList(DwdContractsDBo dwdContractsDBo);

    /**
     * 分页查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @param pageQuery 分页查询对象
     * @return DWD_CONTRACTS_D集合
     */
    TableDataInfo<DwdContractsDVo> selectDwdContractsDPageList(DwdContractsDBo dwdContractsDBo, PageQuery pageQuery);

    /**
     * 新增DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    Boolean insertByBo(DwdContractsDBo dwdContractsDBo);

    /**
     * 修改DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    Boolean updateByBo(DwdContractsDBo dwdContractsDBo);

    /**
     * 批量删除DWD_CONTRACTS_D
     *
     * @param ids 需要删除的DWD_CONTRACTS_D主键集合
     * @param isValid 是否验证
     * @return 结果
     */
    Boolean deleteWithValidByIds(List<String> ids, Boolean isValid);
}
