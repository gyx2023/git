package com.crm.system.domain.cust;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 zqzt_cust_maint_info
 *
 * @author crm
 * @date 2026-02-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_cust_maint_info")
public class ZqztCustMaintInfo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    private String statCycle;
    /**
     *
     */
    private Date etlTime;
    /**
     *
     */
    private String operType;
    /**
     *
     */
    private String userId;
    /**
     *
     */
    private String svcNum;
    /**
     *
     */
    private String typeLvl2;
    /**
     *
     */
    private String typeLvl2Name;
    /**
     *
     */
    private String custGrpId;
    /**
     *
     */
    private String custGrpName;
    /**
     *
     */
    private String custGrpType;
    /**
     *
     */
    private String custId;
    /**
     *
     */
    private String custName;
    /**
     *
     */
    private String STATUS;

}
