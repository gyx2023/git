package com.crm.system.service;

import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.vo.ZqztOperRecordVo;
import com.crm.system.domain.bo.ZqztOperRecordBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 系统操作记录Service接口
 *
 * @author crm
 * @date 2026-04-02
 */
public interface IZqztOperRecordService {

    /**
     * 查询系统操作记录
     */
    ZqztOperRecordVo queryById(Long id);

    /**
     * 查询系统操作记录列表
     */
    TableDataInfo<ZqztOperRecordVo> queryPageList(ZqztOperRecordBo bo, PageQuery pageQuery);

    /**
     * 查询系统操作记录列表
     */
    List<ZqztOperRecordVo> queryList(ZqztOperRecordBo bo);

    /**
     * 新增系统操作记录
     */
    Boolean insertByBo(ZqztOperRecordBo bo);

    /**
     * 修改系统操作记录
     */
    Boolean updateByBo(ZqztOperRecordBo bo);

    /**
     * 校验并批量删除系统操作记录信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据集团信息新增系统操作记录
     *
     * @param groupInfo 集团信息对象
     * @param status 操作状态：0-成功，1-失败（可选，默认 0）
     * @param errorMsg 错误信息（可选，默认空字符串）
     * @return 新增结果
     */
    Boolean insertByGroupInfo(ZqGrpGroupInfo groupInfo, Integer status, String errorMsg);

}
