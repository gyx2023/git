package com.crm.system.service;

import com.crm.system.domain.ZqGrpGroupLog;
import com.crm.system.domain.ZqGrpGroupInfo;
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
 * @date 2025-11-24
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

    /**
     * 记录集团客户信息变更日志
     * @param originalInfo 变更前的集团信息
     * @param updatedInfo 变更后的集团信息
     * @param updateType 变更类型：T1-集团基本信息变更,T2-集团客户经理变更
     * @return 是否记录成功
     */
    Boolean recordChangeLog(ZqGrpGroupInfo originalInfo, ZqGrpGroupInfo updatedInfo, String updateType);
}
