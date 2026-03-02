package com.crm.system.domain.bo.jt;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 订单流程信息（存储流程实例、任务节点、处理人及报文数据）业务对象 zqzt_jt_notice_record
 *
 * @author crm
 * @date 2025-12-31
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ZqztJtNoticeRecordBo extends BaseEntity {

    /**
     * 主键ID（自增，唯一标识每条记录）
     */
    @NotNull(message = "主键ID（自增，唯一标识每条记录）不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 流程实例ID
     */
    @NotBlank(message = "流程实例ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderId;

    /**
     * 订单创建者OA工号
     */
    @NotBlank(message = "订单创建者OA工号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderOperatorId;

    /**
     * 订单创建者OA名称
     */
    @NotBlank(message = "订单创建者OA名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderOperatorName;

    /**
     * 订单创建者省份编码
     */
    @NotBlank(message = "订单创建者省份编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderProvinceCode;

    /**
     * 订单创建者地市编码
     */
    @NotBlank(message = "订单创建者地市编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderCityCode;

    /**
     * 流程状态（3=结束，1=在途）
     */
    @NotBlank(message = "流程状态（3=结束，1=在途）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderStatus;

    /**
     * 任务ID（流程启动/完工等场景使用，与任务无关时为空）
     */
    @NotBlank(message = "任务ID（流程启动/完工等场景使用，与任务无关时为空）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String worksheetId;

    /**
     * 环节编码
     */
    @NotBlank(message = "环节编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskCode;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskName;

    /**
     * 流程编码（流程唯一标识）
     */
    @NotBlank(message = "流程编码（流程唯一标识）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bizCode;

    /**
     * 处理人工号（系统自动任务时可为空）
     */
    @NotBlank(message = "处理人工号（系统自动任务时可为空）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dealUserCode;

    /**
     * 处理人名称
     */
    @NotBlank(message = "处理人名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dealUserName;

    /**
     * 任务处理状态编码
     */
    @NotBlank(message = "任务处理状态编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dealResultCode;

    /**
     * 入参报文体
     */
    @NotBlank(message = "入参报文体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String body;

    /**
     * 表单报文结构（含处理意见和附件）
     */
    @NotBlank(message = "表单报文结构（含处理意见和附件）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formTemplate;

    /**
     * 返回报文体
     */
    @NotBlank(message = "返回报文体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String resBody;


}
