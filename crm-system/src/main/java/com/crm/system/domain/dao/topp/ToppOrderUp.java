package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 升级预警订单关联表对象 topp_order_up
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
@TableName("topp_order_up")
public class ToppOrderUp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单ID，关联 topp_order.id_
     */
    private Integer orderId;

    /**
     * BSS编码（区县用）
     */
    private String bssCode;

    /**
     * MDM组织编码（市本级用）
     */
    private String mdmOrgCode;

    /**
     * 账期，格式：yyyy-MM
     */
    private String period;
}
