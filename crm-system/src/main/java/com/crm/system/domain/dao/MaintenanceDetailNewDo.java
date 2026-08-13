package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 新维系预警明细对象 maintenance_detail_new
 * <p>
 * 迁移自旧系统 com.hollycrm.entity.business.MaintenanceDetailNew，
 * 字段对齐旧系统 TcUserCustWarningNewJob 中 maintenanceDetailNewService.save 的赋值。
 * 与 {@link MaintenanceDetailDo} 的区别：area_id/county_id 走客户经理组织维度，
 * 且无 business_dep/cust_grp_id/grp_name/svc_num 列。
 *
 * @author crm
 */
@Data
@TableName("maintenance_detail_new")
public class MaintenanceDetailNewDo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /** 地市ID（sys_area.id_） */
    @TableField("area_id")
    private Long areaId;

    /** 区县ID（sys_area.id_） */
    @TableField("county_id")
    private Long countyId;

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

    /** 客户15位编码 */
    @TableField("grp_code")
    private String grpCode;

    /** 客户经理编码（sys_user.customer_no） */
    @TableField("manager_id")
    private String managerId;

    /** 客户经理名称（sys_user.name_ch） */
    @TableField("manager_name")
    private String managerName;

    /** 客户经理id（sys_user.id_） */
    @TableField("user_id")
    private Long userId;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 是否重保客户 */
    @TableField("is_imp_prt")
    private String isImpPrt;

    /** 集团类别 */
    @TableField("grp_type")
    private String grpType;

    /** C客户级U用户N不区分 */
    @TableField("order_category")
    private String orderCategory;
}
