package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 维系预警明细对象 maintenance_detail
 * <p>
 * 迁移自旧系统 com.hollycrm.entity.business.MaintenanceDetail，
 * 供新框架预警流程（如 kd_addr_warn）写入，字段对齐旧系统
 * TcUserCustWarningNewJob.startTask() 中 maintenanceDetailService.save 的赋值。
 *
 * @author crm
 */
@Data
@TableName("maintenance_detail")
public class MaintenanceDetailDo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /** 地市ID（sys_area.id_） */
    @TableField("area_id")
    private Long areaId;

    /** 区县ID（sys_area.id_） */
    @TableField("county_id")
    private Long countyId;

    /** 客户归属部门 */
    @TableField("business_dep")
    private String businessDep;

    /** 订单ID，关联 topp_order.id_ */
    @TableField("order_id")
    private Long orderId;

    /** 预警类型，如 kd_addr_warn */
    @TableField("order_type")
    private String orderType;

    /** 维系预警相关表ID（ftp_tc_user_cust_warning.id_） */
    @TableField("detail_id")
    private Long detailId;

    /** 启用状态 0:失败 1:成功 */
    @TableField("status")
    private Integer status;

    /** 客户编号 */
    @TableField("cust_grp_id")
    private String custGrpId;

    /** 客户15位编码 */
    @TableField("grp_code")
    private String grpCode;

    /** 客户名称 */
    @TableField("grp_name")
    private String grpName;

    /** 客户经理编码（sys_user.customer_no；失败时存 oa_id_dev） */
    @TableField("manager_id")
    private String managerId;

    /** 客户经理名称（sys_user.name_ch；失败时存 oa_name_dev） */
    @TableField("manager_name")
    private String managerName;

    /** 客户经理id（sys_user.id_） */
    @TableField("user_id")
    private Long userId;

    /** 用户号码 */
    @TableField("svc_num")
    private String svcNum;

    /** 是否重保客户 */
    @TableField("is_imp_prt")
    private String isImpPrt;

    /** 集团类别 */
    @TableField("grp_type")
    private String grpType;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** C客户级U用户N不区分 */
    @TableField("order_category")
    private String orderCategory;
}
