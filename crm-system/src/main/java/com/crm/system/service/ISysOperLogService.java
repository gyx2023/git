package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.SysOperLog;
import com.crm.system.domain.vo.SysUsersVo;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @author Lion Li
 */
public interface ISysOperLogService {

    TableDataInfo<SysOperLog> selectPageOperLogList(SysOperLog operLog, PageQuery pageQuery);

    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    void insertOperlog(SysOperLog operLog);

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    List<SysOperLog> selectOperLogList(SysOperLog operLog);

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    SysOperLog selectOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    void cleanOperLog();

    void recordOperLog(String title, String method, String requestMethod, String operUrl,
                       String operParam, Long startTime, Integer status,
                       String jsonResult, String errorMsg, Integer operatorType, SysUser sysUser );
}
