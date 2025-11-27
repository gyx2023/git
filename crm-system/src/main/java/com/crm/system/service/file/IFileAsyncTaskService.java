package com.crm.system.service.file;

import com.crm.system.domain.bo.file.FileAsyncTaskBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.vo.file.FileAsyncTaskVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

/**
 * 任务管理Service接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface IFileAsyncTaskService {

    /**
     * 查询任务管理
     */
    FileAsyncTaskVo queryById(Long taskId);

    /**
     * 查询任务管理列表
     */
    TableDataInfo<FileAsyncTaskVo> queryPageList(FileAsyncTaskBo bo, PageQuery pageQuery);

    /**
     * 查询任务管理列表
     */
    List<FileAsyncTaskVo> queryList(FileAsyncTaskBo bo);

    /**
     * 新增任务管理
     */
    Boolean insertByBo(FileAsyncTaskBo bo);

    /**
     * 修改任务管理
     */
    Boolean updateByBo(FileAsyncTaskBo bo);

    /**
     * 校验并批量删除任务管理信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 申请导入任务
     */
    Long applyImport(MultipartFile file, String businessType,String taskName);

    /**
     * 申请导出任务
     * 该方法用于用户申请数据导出任务，根据业务类型、请求参数和任务名称创建一个新的导出任务
     *
     * @param businessType 业务类型，用于标识不同的导出业务场景
     * @param requestParams 请求参数，包含导出所需的具体条件和配置信息，通常为JSON格式字符串
     * @param taskName 任务名称，用于标识和区分不同的导出任务
     * @return 返回新创建的任务ID，Long类型，用于后续查询导出状态和获取导出结果
     */
    Long applyExport(String businessType, String requestParams,String taskName);

    /**
     * 根据任务ID下载文件
     * @param taskId 任务ID
     * @param response HTTP响应
     */
    void downloadFile(Long taskId, HttpServletResponse response) throws Exception;
}
