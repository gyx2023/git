package com.crm.system.service;

import com.crm.system.domain.ZqGrpGroupLog;
import com.crm.system.domain.vo.ZqGrpGroupLogVo;
import com.crm.system.domain.bo.ZqGrpGroupLogBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 政企集团客户信息Service接口
 *
 * @author crm
 * @date 2025-11-21
 */
public interface IZqGrpGroupLogService {

    /**
     * 查询政企集团客户信息
     */
    ZqGrpGroupLogVo queryById(Long grpId);

    /**
     * 查询政企集团客户信息列表
     */
    TableDataInfo<ZqGrpGroupLogVo> queryPageList(ZqGrpGroupLogBo bo, PageQuery pageQuery);

    /**
     * 查询政企集团客户信息列表
     */
    List<ZqGrpGroupLogVo> queryList(ZqGrpGroupLogBo bo);

    /**
     * 新增政企集团客户信息
     */
    Boolean insertByBo(ZqGrpGroupLogBo bo);

    /**
     * 修改政企集团客户信息
     */
    Boolean updateByBo(ZqGrpGroupLogBo bo);

    /**
     * 校验并批量删除政企集团客户信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
