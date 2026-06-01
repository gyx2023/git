package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 策略中心产品对象 tbl_policy_center_product
 */
@Data
@TableName("tbl_policy_center_product")
public class PolicyCenterProduct implements Serializable {
    private static final long serialVersionUID = 2243994516507064619L;

    /**
     * 主键ID
     */
    @TableId(value = "id_")
    private Integer id;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 产品详情
     */
    @TableField("contact")
    private String contact;

    /**
     * 任务关联方案等
     */
    @TableField("competency")
    private String competency;

    /**
     * 产品说明
     */
    @TableField("subfield_content")
    private String subfieldContent;

    /**
     * 产品信息
     */
    @TableField("base_content")
    private String baseContent;

    /**
     * 附件等
     */
    @TableField("attachment")
    private String attachment;

    /**
     * 拜访任务ID
     */
    @TableField("visit_task_id")
    private Integer visitTaskId;
}
