package com.crm.system.domain.servicehall;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 集团任务前置通知接口记录表对象 zqzt_jt_notice_record
 *
 * @author crm
 * @date 2025-12-31
 */
@Data
@TableName("zqzt_jt_notice_record")
public class ZqztJtNoticeRecordDo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键ID（自增，唯一标识每条记录）
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     * 流程实例ID
     */
    private String orderId;
    /**
     * 订单创建者OA工号
     */
    private String orderOperatorId;
    /**
     * 订单创建者OA名称
     */
    private String orderOperatorName;
    /**
     * 订单创建者省份编码
     */
    private String orderProvinceCode;
    /**
     * 订单创建者地市编码
     */
    private String orderCityCode;
    /**
     * 流程状态（3=结束，1=在途）
     */
    private String orderStatus;
    /**
     * 任务ID（流程启动/完工等场景使用，与任务无关时为空）
     */
    private String worksheetId;
    /**
     * 环节编码
     */
    private String taskCode;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 流程编码（流程唯一标识）
     */
    private String bizCode;
    /**
     * 处理人工号（系统自动任务时可为空）
     */
    private String dealUserCode;
    /**
     * 处理人名称
     */
    private String dealUserName;
    /**
     * 任务处理状态编码
     */
    private String dealResultCode;
    /**
     * 入参报文体
     */
    private String body;
    /**
     * 表单报文结构（含处理意见和附件）
     */
    private String formTemplate;
    /**
     * 返回报文体
     */
    private String resBody;

    private String failReason;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
