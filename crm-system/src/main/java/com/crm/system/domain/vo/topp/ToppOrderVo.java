package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 【请填写功能名称】视图对象 topp_order
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@ExcelIgnoreUnannotated
public class ToppOrderVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 关联商机，可以为空
     */
    @ExcelProperty(value = "关联商机，可以为空")
    private Long oppoId;

    /**
     * 集团客户，名单制客户
     */
    @ExcelProperty(value = "集团客户，名单制客户")
    private Long custId;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private Long creator;

    /**
     * 关联sys_form表
     */
    @ExcelProperty(value = "关联sys_form表")
    private Long sysFormId;

    /**
     * 地市
     */
    @ExcelProperty(value = "地市")
    private Long areaId;

    /**
     * 部门
     */
    @ExcelProperty(value = "部门")
    private Long orgId;

    /**
     * 订单流水号
     */
    @ExcelProperty(value = "订单流水号")
    private String orderNumber;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String orderName;

    /**
     * 订单类型，流程类型
     */
    @ExcelProperty(value = "订单类型，流程类型")
    private String orderType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String piId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long contractId;

    /**
     * 当前阶段,可以是多个，逗号开始，逗号结束，逗号分隔
     */
    @ExcelProperty(value = "当前阶段,可以是多个，逗号开始，逗号结束，逗号分隔")
    private String phase;

    /**
     * 订单客户经理，默认是创建人，但是如果选择了客户经理那么就是选择的
     */
    @ExcelProperty(value = "订单客户经理，默认是创建人，但是如果选择了客户经理那么就是选择的")
    private Long custManagerId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String actors;

    /**
     * 状态：对应码表order_status
     */
    @ExcelProperty(value = "状态：对应码表order_status")
    private String status;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String actorsInfo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String phaseName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String whiteList;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppPhase;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long clueId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long infoSendId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long taskId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String accNbr;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long infoId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long businessAcceptanceId;

    /**
     * çˆ¶çº§orderId
     */
    @ExcelProperty(value = "çˆ¶çº§orderId")
    private Long phaseOrderId;

    /**
     * 结束原因
     */
    @ExcelProperty(value = "结束原因")
    private String endReason;

    /**
     * 预警等级
     */
    @ExcelProperty(value = "预警等级")
    private String warningLevel;

    /**
     * 在网标识
     */
    @ExcelProperty(value = "在网标识")
    private String curInnetFlag;

    /**
     * 小微云城传过来的订单号
     */
    @ExcelProperty(value = "小微云城传过来的订单号")
    private String xwyOrderNo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer batchFlag;

    /**
     * 小微云传过来的竣工结果
     */
    @ExcelProperty(value = "小微云传过来的竣工结果")
    private String xwyOrderResult;

    /**
     * 调用小微云受理状态接口次数
     */
    @ExcelProperty(value = "调用小微云受理状态接口次数")
    private Long xwyCount;

    /**
     * 客户归属部门
     */
    @ExcelProperty(value = "客户归属部门")
    private String businessDep;

    /**
     * 楼宇编码
     */
    @ExcelProperty(value = "楼宇编码")
    private String buildingNum;

    /**
     * 几日通
     */
    @ExcelProperty(value = "几日通")
    private String adoptDays;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String businessType;

    /**
     * 二级业务类型
     */
    @ExcelProperty(value = "二级业务类型")
    private String businessTwoType;

    /**
     * 订单来源
     */
    @ExcelProperty(value = "订单来源")
    private String orderSource;

    /**
     * 宽带到期预警跟踪情况
     */
    @ExcelProperty(value = "宽带到期预警跟踪情况")
    private String wideResult;

    /**
     * C客户级U用户N不区分
     */
    @ExcelProperty(value = "C客户级U用户N不区分")
    private String orderCategory;

    /**
     * 三方稽核原因
     */
    @ExcelProperty(value = "三方稽核原因")
    private String thirdCheckResult;


}
