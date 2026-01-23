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

}
