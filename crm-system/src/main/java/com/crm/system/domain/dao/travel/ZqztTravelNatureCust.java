package com.crm.system.domain.dao.travel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 浙江省自然客户缓存表实体
 *
 * @author crm
 * @date 2026-06-17
 */
@Data
@TableName("zqzt_travel_nature_cust")
public class ZqztTravelNatureCust implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String natureCustId;

    private String natureCustName;

    private Date createTime;
}
