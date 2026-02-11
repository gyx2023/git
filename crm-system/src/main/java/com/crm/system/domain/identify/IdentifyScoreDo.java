package com.crm.system.domain.identify;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;
import org.springframework.data.annotation.Id;

/**
 * 【请填写功能名称】对象 identify_score
 *
 * @author crm
 * @date 2026-02-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("identify_score")
public class IdentifyScoreDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     * 对应 identify_score_customer表的id
     */
    private Long customerId;
    /**
     * 总分
     */
    private String totalScore;
    /**
     * 智能体接口返回详细内容
     */
    private String content;
    /**
     * 附件原始名称
     */
    @TableField(value = "name_")
    private String name;
    /**
     * 调用接口状态,0待调用，1成功,2失败
     */
    private String scoreStatus;
    /**
     * 上传时间
     */
    private Date uploadTime;
    /**
     * 上传人员
     */
    @TableField(value = "uploader_")
    private Long uploader;
    /**
     * 更新者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;


    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;


    private String isAgain;

    private String fileText;
}
