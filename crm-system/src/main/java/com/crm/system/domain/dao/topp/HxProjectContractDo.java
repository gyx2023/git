package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 划小项目合同关联对象 hx_project_contract
 */
@Data
@TableName("hx_project_contract")
public class HxProjectContractDo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 划小项目ID */
    private Long hxProjectId;

    /** 合同ID */
    private Long contractId;

    /** 是否有效（1=有效，0=已删除） */
    private String isValid;

    /** 创建人ID */
    private Long createUserId;

    /** 创建时间 */
    private Date createTime;
}
