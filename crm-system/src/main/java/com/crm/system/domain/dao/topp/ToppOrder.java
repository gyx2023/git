package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【请填写功能名称】对象 topp_order
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_order")
public class ToppOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     * 关联商机，可以为空
     */
    private Long oppoId;
    /**
     * 集团客户，名单制客户
     */
    private Long custId;
    /**
     * 创建人
     */
    @TableField("creator_")
    private Long creator;
    /**
     * 关联sys_form表
     */
    private Long sysFormId;
    /**
     * 地市
     */
    private Long areaId;
    /**
     * 部门
     */
    private Long orgId;
    /**
     * 订单流水号
     */
    private String orderNumber;
    /**
     *
     */
    private String orderName;
    /**
     * 订单类型，流程类型
     */
    private String orderType;
    /**
     *
     */
    private String piId;
    /**
     *
     */
    private Long contractId;
    /**
     * 当前阶段,可以是多个，逗号开始，逗号结束，逗号分隔
     */
    @TableField("phase_")
    private String phase;
    /**
     * 订单客户经理，默认是创建人，但是如果选择了客户经理那么就是选择的
     */
    private Long custManagerId;
    /**
     *
     */
    @TableField("actors_")
    private String actors;
    /**
     * 状态：对应码表order_status
     */
    @TableField("status_")
    private String status;
    /**
     *
     */
    private String actorsInfo;
    /**
     *
     */
    private String phaseName;
    /**
     *
     */
    private String whiteList;
    /**
     *
     */
    private String oppPhase;
    /**
     *
     */
    private Long clueId;
    /**
     *
     */
    private Long infoSendId;
    /**
     *
     */
    private Long taskId;
    /**
     *
     */
    private String custName;
    /**
     *
     */
    private String accNbr;
    /**
     *
     */
    private Long infoId;
    /**
     *
     */
    private Long businessAcceptanceId;
    /**
     * çˆ¶çº§orderId
     */
    private Long phaseOrderId;
    /**
     * 结束原因
     */
    private String endReason;
    /**
     * 预警等级
     */
    private String warningLevel;
    /**
     * 在网标识
     */
    private String curInnetFlag;
    /**
     * 小微云城传过来的订单号
     */
    private String xwyOrderNo;
    /**
     *
     */
    private Integer batchFlag;
    /**
     * 小微云传过来的竣工结果
     */
    private String xwyOrderResult;
    /**
     * 调用小微云受理状态接口次数
     */
    private Long xwyCount;
    /**
     * 客户归属部门
     */
    private String businessDep;
    /**
     * 楼宇编码
     */
    private String buildingNum;
    /**
     * 几日通
     */
    private String adoptDays;
    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 二级业务类型
     */
    private String businessTwoType;
    /**
     * 订单来源
     */
    private String orderSource;
    /**
     * 宽带到期预警跟踪情况
     */
    private String wideResult;
    /**
     * C客户级U用户N不区分
     */
    private String orderCategory;
    /**
     * 三方稽核原因
     */
    private String thirdCheckResult;

    @TableField(exist = false)
    private String createBy;


    @TableField(exist = false)
    private String updateBy;
}
