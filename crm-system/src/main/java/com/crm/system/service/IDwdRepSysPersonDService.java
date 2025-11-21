package com.crm.system.service;

import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface IDwdRepSysPersonDService {

    /**
     * 查询【请填写功能名称】
     */
    DwdRepSysPersonDVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<DwdRepSysPersonDVo> queryPageList(DwdRepSysPersonDBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<DwdRepSysPersonDVo> queryList(DwdRepSysPersonDBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(DwdRepSysPersonDBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(DwdRepSysPersonDBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
