package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 系统操作记录对象 zqzt_oper_record
 *
 * @author crm
 * @date 2026-04-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_oper_record")
public class ZqztOperRecord extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID，自增生成
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 模块编码：商机/集团等大模块
     */
    private String moduleCode;
    /**
     * 操作点编码：模块内具体操作
     */
    private String operCode;
    /**
     * 关联业务ID
     */
    private String businessId;
    /**
     * 请求参数(JSON格式)
     */
    private String requestParams;
    /**
     * 扩展信息(JSON格式，非入参非返回值)
     */
    private String extraInfo;
    /**
     * 操作状态：0-成功，1-失败
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 操作人ID
     */
    private Long operUserId;
    /**
     * 操作人姓名
     */
    private String operUserName;
    /**
     * 操作时间
     */
    private Date operTime;
    /**
     * 操作人IP
     */
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
