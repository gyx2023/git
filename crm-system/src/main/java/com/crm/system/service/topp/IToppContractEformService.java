package com.crm.system.service.topp;

import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
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
public interface IToppContractEformService {

    /**
     * 查询【请填写功能名称】
     */
    ToppContractEformVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppContractEformVo> queryPageList(ToppContractEformBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppContractEformVo> queryList(ToppContractEformBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppContractEformBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppContractEformBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
