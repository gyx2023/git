package com.crm.system.service;

import com.crm.system.domain.TcommTask;
import com.crm.system.domain.vo.TcommTaskVo;
import com.crm.system.domain.bo.TcommTaskBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 待办任务Service接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface ITcommTaskService {

    /**
     * 查询待办任务
     */
    TcommTaskVo queryById(Integer id);

    /**
     * 查询待办任务列表
     */
    TableDataInfo<TcommTaskVo> queryPageList(TcommTaskBo bo, PageQuery pageQuery);

    /**
     * 查询待办任务列表
     */
    List<TcommTaskVo> queryList(TcommTaskBo bo);

    /**
     * 新增待办任务
     */
    Boolean insertByBo(TcommTaskBo bo);

    /**
     * 修改待办任务
     */
    Boolean updateByBo(TcommTaskBo bo);

    /**
     * 校验并批量删除待办任务信息
     */
    Boolean deleteWithValidByIds(Collection<Integer> ids, Boolean isValid);
}
