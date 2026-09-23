package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 客户级闭环预警 ftp_tc_cust_cycle_warning
 * <p>
 * 按 MAIN_ORDER_FLOW_NO 关联客户级预警 ftp_tc_user_cust_warning，
 * 提供闭环标识/闭环订单数/正常闭环数等，用于闭环自动关单。
 * </p>
 *
 * @author crm
 */
@Data
@TableName("ftp_tc_cust_cycle_warning")
public class FtpTcCustCycleWarningDo implements Serializable {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /** 地市 */
    @TableField(value = "AREA_ID")
    private String areaId;

    /** 预警类型 000031:团购折扣到期 000033:终端合约到期 等 */
    @TableField(value = "WARNING_TYPE")
    private String warningType;

    /** 主订单编号 */
    @TableField(value = "MAIN_ORDER_FLOW_NO")
    private String mainOrderFlowNo;

    /** 预警账期 */
    @TableField(value = "WARNING_DATE")
    private String warningDate;

    /** 闭环账期 */
    @TableField(value = "CYCLE_DATE")
    private String cycleDate;

    /** 预警优先级 */
    @TableField(value = "WARNING_LEVEL")
    private String warningLevel;

    /** 闭环标识 0:未闭环 1:已闭环(已续约) 2:离网 3:超时 */
    @TableField(value = "CYCLE_FLAG")
    private Integer cycleFlag;

    /** 集团客户编号（系统内部编号） */
    @TableField(value = "CUST_GRP_ID")
    private Integer custGrpId;

    /** 订单数 */
    @TableField(value = "ORDER_NUM")
    private String orderNum;

    /** 订单数-其中高优先 */
    @TableField(value = "HIGH_LEVEL_NUM")
    private String highLevelNum;

    /** 闭环订单数 */
    @TableField(value = "CLOSE_NUM")
    private String closeNum;

    /** 其中正常闭环 */
    @TableField(value = "NORMAL_CLOSE_NUM")
    private String normalCloseNum;

    /** 其中非正常闭环 */
    @TableField(value = "ABNOMAL_CLOSE_NUM")
    private String abnomalCloseNum;

    @TableField(value = "create_date")
    private Date createDate;
}
