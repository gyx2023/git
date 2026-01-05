package com.crm.system.service.topp;

import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface IToppContractService {

    /**
     * 查询【请填写功能名称】
     */
    ToppContractVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppContractVo> queryPageList(ToppContractBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppContractVo> queryList(ToppContractBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppContractBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppContractBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<ToppContractVo> queryContractList(List<Long> ids);

    List<ToppContractVo>  findContractDetailById(Long oppoId);
}
