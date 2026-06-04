package com.crm.system.domain.vo.zbzq;

import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * VIEW视图对象 salesup_coll_oppo_sign
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@ExcelIgnoreUnannotated
public class SalesupCollOppoSignVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String ifSign;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer signType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String signName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String clientoId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String clientoName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String signDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String signSub;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal contractIncome;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String effectDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String endDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date signEndTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal annualIncome;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long oppoId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String createDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long orderId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long mainIncom;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String jsDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String payPattern;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String staDimension;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long executorContract;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String executorContractLogin;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String executorContractName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal signExpense;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String drafterContract;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String localsn;

    private String oppoNumber;
}
