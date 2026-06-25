package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 topp_order
 *
 * @author crm
 * @date 2025-12-18
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppOrderBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商机，可以为空
     */
    @NotNull(message = "关联商机，可以为空不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long oppoId;

    /**
     * 集团客户，名单制客户
     */
    @NotNull(message = "集团客户，名单制客户不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long custId;

    /**
     * 创建人
     */
    @NotNull(message = "创建人不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 关联sys_form表
     */
    @NotNull(message = "关联sys_form表不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sysFormId;

    /**
     * 地市
     */
    @NotNull(message = "地市不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long areaId;

    /**
     * 部门
     */
    @NotNull(message = "部门不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orgId;

    /**
     * 订单流水号
     */
    @NotBlank(message = "订单流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNumber;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderName;

    /**
     * 订单类型，流程类型
     */
    @NotBlank(message = "订单类型，流程类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String piId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long contractId;

    /**
     * 当前阶段,可以是多个，逗号开始，逗号结束，逗号分隔
     */
    @NotBlank(message = "当前阶段,可以是多个，逗号开始，逗号结束，逗号分隔不能为空", groups = { AddGroup.class, EditGroup.class })
    private String phase;

    /**
     * 订单客户经理，默认是创建人，但是如果选择了客户经理那么就是选择的
     */
    @NotNull(message = "订单客户经理，默认是创建人，但是如果选择了客户经理那么就是选择的不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long custManagerId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String actors;

    /**
     * 状态：对应码表order_status
     */
    @NotBlank(message = "状态：对应码表order_status不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String actorsInfo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String phaseName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String whiteList;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppPhase;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long clueId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long infoSendId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long taskId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String accNbr;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long infoId;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long businessAcceptanceId;

    /**
     * çˆ¶çº§orderId
     */
    @NotNull(message = "çˆ¶çº§orderId不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long phaseOrderId;

    /**
     * 结束原因
     */
    @NotBlank(message = "结束原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String endReason;

    /**
     * 预警等级
     */
    @NotBlank(message = "预警等级不能为空", groups = { AddGroup.class, EditGroup.class })
    private String warningLevel;

    /**
     * 在网标识
     */
    @NotBlank(message = "在网标识不能为空", groups = { AddGroup.class, EditGroup.class })
    private String curInnetFlag;

    /**
     * 小微云城传过来的订单号
     */
    @NotBlank(message = "小微云城传过来的订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String xwyOrderNo;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer batchFlag;

    /**
     * 小微云传过来的竣工结果
     */
    @NotBlank(message = "小微云传过来的竣工结果不能为空", groups = { AddGroup.class, EditGroup.class })
    private String xwyOrderResult;

    /**
     * 调用小微云受理状态接口次数
     */
    @NotNull(message = "调用小微云受理状态接口次数不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long xwyCount;

    /**
     * 客户归属部门
     */
    @NotBlank(message = "客户归属部门不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessDep;

    /**
     * 楼宇编码
     */
    @NotBlank(message = "楼宇编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String buildingNum;

    /**
     * 几日通
     */
    @NotBlank(message = "几日通不能为空", groups = { AddGroup.class, EditGroup.class })
    private String adoptDays;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessType;

    /**
     * 二级业务类型
     */
    @NotBlank(message = "二级业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String businessTwoType;

    /**
     * 订单来源
     */
    @NotBlank(message = "订单来源不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderSource;

    /**
     * 宽带到期预警跟踪情况
     */
    @NotBlank(message = "宽带到期预警跟踪情况不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wideResult;

    /**
     * C客户级U用户N不区分
     */
    @NotBlank(message = "C客户级U用户N不区分不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderCategory;

    /**
     * 三方稽核原因
     */
    @NotBlank(message = "三方稽核原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdCheckResult;

    /**
     * 流程引擎来源：ACTIVITI/FLOWABLE
     */
    private String processEngine;


}
