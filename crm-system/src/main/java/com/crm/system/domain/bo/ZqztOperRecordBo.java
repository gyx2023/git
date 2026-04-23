package com.crm.system.domain.bo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 系统操作记录业务对象 zqzt_oper_record
 *
 * @author crm
 * @date 2026-04-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztOperRecordBo extends BaseEntity {

    /**
     * 主键ID，自增生成
     */
    @NotNull(message = "主键ID，自增生成不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 模块编码：商机/集团等大模块
     */
    @NotBlank(message = "模块编码：商机/集团等大模块不能为空", groups = { AddGroup.class, EditGroup.class })
    private String moduleCode;

    /**
     * 操作点编码：模块内具体操作
     */
    @NotBlank(message = "操作点编码：模块内具体操作不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operCode;

    /**
     * 关联业务ID
     */
    @NotBlank(message = "关联业务ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessId;

    /**
     * 请求参数(JSON格式)
     */
    @NotBlank(message = "请求参数(JSON格式)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String requestParams;

    /**
     * 扩展信息(JSON格式，非入参非返回值)
     */
    @NotBlank(message = "扩展信息(JSON格式，非入参非返回值)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String extraInfo;

    /**
     * 操作状态：0-成功，1-失败
     */
    @NotNull(message = "操作状态：0-成功，1-失败不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 错误信息
     */
    @NotBlank(message = "错误信息不能为空", groups = { AddGroup.class, EditGroup.class })
    private String errorMsg;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long operUserId;

    /**
     * 操作人姓名
     */
    @NotBlank(message = "操作人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operUserName;

    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date operTime;

    /**
     * 操作人IP
     */
    @NotBlank(message = "操作人IP不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operIp;

    @TableField(exist = false)
    private String createBy;
    @TableField(exist = false)
    private Date createTime;
    @TableField(exist = false)
    private String updateBy;
    @TableField(exist = false)
    private Date updateTime;
}
