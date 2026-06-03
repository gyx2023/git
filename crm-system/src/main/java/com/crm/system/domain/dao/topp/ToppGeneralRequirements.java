package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用需求订单对象 topp_general_requirements
 *
 * @author crm
 */
@Data
@TableName("topp_general_requirements")
public class ToppGeneralRequirements implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("order_id")
    private Long orderId;

    @TableField("operation_type")
    private String operationType;
}
