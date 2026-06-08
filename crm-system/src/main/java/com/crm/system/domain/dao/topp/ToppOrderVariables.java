package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单流程变量对象 topp_order_variables
 *
 * @author crm
 */
@Data
@TableName("topp_order_variables")
public class ToppOrderVariables implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("order_id")
    private Long orderId;

    @TableField("name_")
    private String name;

    @TableField("text_")
    private String text;

    @TableField("create_time")
    private Date createTime;
}
