package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.GrpContractInfoBo;
import com.crm.system.domain.GrpContractInfo;
import com.crm.system.domain.vo.GrpContractInfoVo;

import java.util.Collection;
import java.util.List;

/**
 * Oracle库合同信息Service接口
 *
 * @author 万超凡
 * @date 2026-01-23
 */
public interface IGrpContractInfoService {

    /**
     * 查询Oracle库合同信息
     */
    GrpContractInfoVo queryById(String contractId);

    /**
     * 查询Oracle库合同信息列表
     */
    TableDataInfo<GrpContractInfoVo> queryPageList(GrpContractInfoBo bo, PageQuery pageQuery);

    /**
     * 查询Oracle库合同信息列表
     */
    List<GrpContractInfoVo> queryList(GrpContractInfoBo bo);

    /**
     * 新增Oracle库合同信息
     */
    Boolean insertByBo(GrpContractInfoBo bo);

    /**
     * 修改Oracle库合同信息
     */
    Boolean updateByBo(GrpContractInfoBo bo);

    /**
     * 校验并批量删除Oracle库合同信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);

    /**
     * 根据合同ID查询单条数据
     *
     * @param contractId 合同ID
     * @return 合同数据
     */
    GrpContractInfoVo selectByContractId(String contractId);

    /**
     * 查询所有合同数据
     *
     * @return 合同数据列表
     */
    List<GrpContractInfoVo> selectAll();

    /**
     * 根据集团ID查询
     *
     * @param grpId 集团ID
     * @return 合同数据列表
     */
    List<GrpContractInfoVo> selectByGrpId(Long grpId);

    /**
     * 根据合同ERP编码查询
     *
     * @param erpContractId ERP合同编码
     * @return 合同数据
     */
    GrpContractInfoVo selectByErpContractId(String erpContractId);

    /**
     * 根据合同名称查询
     *
     * @param contractName 合同名称
     * @return 合同数据列表
     */
    List<GrpContractInfoVo> selectByContractName(String contractName);

    /**
     * 根据合同类型查询
     *
     * @param contractType 合同类型
     * @return 合同数据列表
     */
    List<GrpContractInfoVo> selectByContractType(String contractType);

    /**
     * 根据合同状态查询
     *
     * @param contractStatus 合同状态
     * @return 合同数据列表
     */
    List<GrpContractInfoVo> selectByContractStatus(String contractStatus);
}