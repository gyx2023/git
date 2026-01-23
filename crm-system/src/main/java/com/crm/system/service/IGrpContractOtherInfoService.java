package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.GrpContractOtherInfoBo;
import com.crm.system.domain.GrpContractOtherInfo;
import com.crm.system.domain.vo.GrpContractOtherInfoVo;

import java.util.Collection;
import java.util.List;

/**
 * Oracle库集团合同扩展信息Service接口
 *
 * @author 万超凡
 * @date 2026-01-23
 */
public interface IGrpContractOtherInfoService {

    /**
     * 查询Oracle库集团合同扩展信息
     */
    GrpContractOtherInfoVo queryById(Long id);

    /**
     * 查询Oracle库集团合同扩展信息列表
     */
    TableDataInfo<GrpContractOtherInfoVo> queryPageList(GrpContractOtherInfoBo bo, PageQuery pageQuery);

    /**
     * 查询Oracle库集团合同扩展信息列表
     */
    List<GrpContractOtherInfoVo> queryList(GrpContractOtherInfoBo bo);

    /**
     * 新增Oracle库集团合同扩展信息
     */
    Boolean insertByBo(GrpContractOtherInfoBo bo);

    /**
     * 修改Oracle库集团合同扩展信息
     */
    Boolean updateByBo(GrpContractOtherInfoBo bo);

    /**
     * 校验并批量删除Oracle库集团合同扩展信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据ID查询单条数据
     *
     * @param id ID
     * @return 扩展信息数据
     */
    GrpContractOtherInfoVo selectById(Long id);

}
