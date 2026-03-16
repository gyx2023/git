package com.crm.system.domain.vo.cust;

import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.Sensitive;
import com.crm.common.enums.SensitiveStrategy;
import lombok.Data;



/**
 * 【请填写功能名称】视图对象 zqzt_cust_maint_info
 *
 * @author crm
 * @date 2026-02-12
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztCustMaintInfoVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String statCycle;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date etlTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String operType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String userId;

    /**
     *
     */
    @ExcelProperty(value = "")
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String svcNum;

    private String encryptedSvcNum;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String typeLvl2;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String typeLvl2Name;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custGrpId;

    /**
     *
     */
    @ExcelProperty(value = "")
    @Sensitive(strategy = SensitiveStrategy.COMPANY_NAME)
    private String custGrpName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custGrpType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custId;

    /**
     *
     */
    @ExcelProperty(value = "")
    @Sensitive(strategy = SensitiveStrategy.COMPANY_NAME)
    private String custName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String status;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date createTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date updateTime;


}
