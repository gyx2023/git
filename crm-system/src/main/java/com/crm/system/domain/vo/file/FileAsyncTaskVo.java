package com.crm.system.domain.vo.file;

import java.util.Date;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;



/**
 * 任务管理视图对象 zqzt_file_async_task
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@ExcelIgnoreUnannotated
public class FileAsyncTaskVo {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID 格式：yyyymmdd+0001（4位序列号)，例如：202511120001
     */
    private Long taskId;

    /**
     * 任务名称，可读性标识，如“用户数据导出”、“订单批量导入”
     */
    private String taskName;

    /**
     * 任务大类，用于区分核心业务逻辑： import-导入，export-导出
     */
    private String taskType;

    /**
     * 用户上传的原始文件名（导入）或系统生成的目标文件名（导出）
     */
    private String fileName;

    /**
     * 文件在对象存储（如S3/OSS/MinIO）或服务器上的完整URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 业务类型： 集团客户信息导出grp_group_export、 客户归属部门维护grp_busi_dept_import、 维护拓展类型grp_expand_type_import、 批量分配客户经理grp_manager_import
     */
    private String businessType;

    /**
     * 总条数
     */
    private Long totalRows;

    /**
     * 成功条数
     */
    private Long successRows;

    /**
     * 失效条数
     */
    private Long failRows;

    /**
     * 任务状态： 0-待处理，1-处理中，2-处理成功，3-处理失败，4-取消
     */
    private Long status;

    /**
     * 任务开始处理时间
     */
    private Date startTime;

    /**
     * 任务完成（成功/失败）时间
     */
    private Date finishTime;

    /**
     * 说明
     */
    private String remark;

    /**
     * 请求参数。导出时的查询条件，导入时的配置项等。 导出： { "filters": { "startDate": "2023-10-01", "endDate": "2023-10-31", "status": [1, 2], "userId": 12345 }, "columns": ["id", "orderNo", "amount", "createTime"], "format": "xlsx" }
     */
    private String requestParams;

    /**
     * 导入结果文件名称
     */
    private String resultFileName;

    /**
     * 导入结果文件名称在对象存储（如S3/OSS/MinIO）或服务器上的完整URL
     */
    private String resultFileUrl;

    /**
     * 任务创建者用户ID
     */
    private Long createUserId;

    /**
     * 任务创建者姓名（冗余设计，避免联表查询）
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

}
