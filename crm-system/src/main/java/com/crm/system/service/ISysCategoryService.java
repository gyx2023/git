package com.crm.system.service;

import com.crm.system.domain.SysCategory;
import com.crm.system.domain.vo.SysCategoryVo;
import com.crm.system.domain.bo.SysCategoryBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 行业大类-亚类-细类分类Service接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface ISysCategoryService {

    /**
     * 查询行业大类-亚类-细类分类
     */
    SysCategoryVo queryById(String id);

    /**
     * 查询行业大类-亚类-细类分类列表
     */
    TableDataInfo<SysCategoryVo> queryPageList(SysCategoryBo bo, PageQuery pageQuery);

    /**
     * 查询行业大类-亚类-细类分类列表
     */
    List<SysCategoryVo> queryList(SysCategoryBo bo);

    /**
     * 查询所有行业大类列表
     */
    List<SysCategoryVo> selectMajorCategories();

    /**
     * 新增行业大类-亚类-细类分类
     */
    Boolean insertByBo(SysCategoryBo bo);

    /**
     * 修改行业大类-亚类-细类分类
     */
    Boolean updateByBo(SysCategoryBo bo);

    /**
     * 校验并批量删除行业大类-亚类-细类分类信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}