package com.crm.system.service;

import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 政企集团客户信息Service接口
 *
 * @author crm
 * @date 2025-11-20
 */
public interface IZqGrpGroupInfoService {

    /**
     * 查询政企集团客户信息
     */
    ZqGrpGroupInfoVo queryById(Long grpId);

    /**
     * 查询政企集团客户信息列表
     */
    TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd);

    /**
     * 查询政企集团客户信息列表
     */
    List<ZqGrpGroupInfoVo> queryList(ZqGrpGroupInfoBo bo);

    /**
     * 新增政企集团客户信息
     */
    Boolean insertByBo(ZqGrpGroupInfoBo bo);

    /**
     * 修改政企集团客户信息
     */
    Boolean updateByBo(ZqGrpGroupInfoBo bo);

    /**
     * 校验并批量删除政企集团客户信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
