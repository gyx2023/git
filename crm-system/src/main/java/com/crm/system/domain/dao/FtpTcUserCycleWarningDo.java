package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户级闭环预警 ftp_tc_user_cycle_warning
 * <p>
 * 按 ORDER_FLOW_NO 关联用户级预警 ftp_tc_user_common_warning，
 * 提供闭环标识/闭环账期/续约信息等。
 * </p>
 *
 * @author crm
 */
@Data
@TableName("ftp_tc_user_cycle_warning")
public class FtpTcUserCycleWarningDo implements Serializable {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /** 地市 */
    @TableField(value = "AREA_ID")
    private String areaId;

    /** 预警类型 C00002:宽带到期预警 等 */
    @TableField(value = "WARNING_TYPE")
    private String warningType;

    /** 主订单编号 */
    @TableField(value = "MAIN_ORDER_FLOW_NO")
    private String mainOrderFlowNo;

    /** 子订单编号 */
    @TableField(value = "ORDER_FLOW_NO")
    private String orderFlowNo;

    /** 预警账期 */
    @TableField(value = "WARNING_DATE")
    private String warningDate;

    /** 闭环账期 */
    @TableField(value = "CYCLE_DATE")
    private String cycleDate;

    /** 用户编号 */
    @TableField(value = "USER_ID")
    private String userId;

    /** 预警优先级 */
    @TableField(value = "WARNING_LEVEL")
    private String warningLevel;

    /** 闭环标识 0:未闭环 1:已闭环(已续约) 2:离网 3:超时 */
    @TableField(value = "CYCLE_FLAG")
    private Integer cycleFlag;

    /** 复合字段，多字段用 || 拼接 */
    @TableField(value = "WARNING_INFO")
    private String warningInfo;

    /** 用户状态 */
    @TableField(value = "USER_STATUS")
    private String userStatus;

    @TableField(value = "create_date")
    private Date createDate;
}
