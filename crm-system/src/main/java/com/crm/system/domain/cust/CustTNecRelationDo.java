package com.crm.system.domain.cust;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 实体客户id 自然客户id对应关系表对象 cust_t_nec_relation
 *
 * @author crm
 * @date 2026-05-12
 */
@Data
@TableName("cust_t_nec_relation")
public class CustTNecRelationDo {
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "NATURE_CUST_ID")
    private String natureCustId;

    @TableField(value = "PROVINCE_CUST_ID")
    private String provinceCustId;
}
