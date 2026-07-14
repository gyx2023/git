package com.crm.system.service.sys;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dao.sys.SysOperLog;

import java.util.List;
import java.util.concurrent.Callable;

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

    /**
     * 执行外部API调用并记录操作日志（模板方法）
     * <p>
     * 自动处理：startTime/requestTime 计时、status/jsonResult/errorMsg 设置、finally 写入
     * 业务逻辑成功时 status=0 并记录 jsonResult，抛出异常时 status=1 并记录 errorMsg
     *
     * @param title     操作标题
     * @param url       请求URL
     * @param operParam 请求参数
     * @param apiCall   实际API调用逻辑，成功返回响应字符串，失败抛异常
     * @return apiCall 返回的响应字符串
     */
    String logApiCall(String title, String url, String operParam, Callable<String> apiCall);

    /**
     * 执行外部API调用并记录操作日志（静默版模板方法）
     * <p>
     * 与 logApiCall 行为一致，但异常时不 rethrow，返回 false 表示失败
     * 适用于调用方不需要抛异常的场景（如 XXL-Job 定时任务）
     *
     * @param title     操作标题
     * @param url       请求URL
     * @param operParam 请求参数
     * @param apiCall   实际API调用逻辑，成功返回响应字符串，失败抛异常
     * @return true=成功，false=失败
     */
    boolean tryApiCall(String title, String url, String operParam, Callable<String> apiCall);
}
