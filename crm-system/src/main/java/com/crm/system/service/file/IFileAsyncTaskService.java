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
     */
    Long applyExport(String businessType, String requestParams,String taskName);

    /**
     * 根据任务ID下载文件
     * @param taskId 任务ID
     * @param response HTTP响应
     */
    void downloadFile(Long taskId,String fileType, HttpServletResponse response) throws Exception;
}
