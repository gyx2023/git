package com.crm.system.domain.dao.zbzq;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * VIEW对象 salesup_coll_oppo_sign
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@TableName("salesup_coll_oppo_sign")
public class SalesupCollOppoSignDo {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    private Long id;
    /**
     *
     */
    private String ifSign;
    /**
     *
     */
    private Integer signType;
    /**
     *
     */
    private String signName;
    /**
     *
     */
    private String clientoId;
    /**
     *
     */
    private String clientoName;
    /**
     *
     */
    private String contractId;
    /**
     *
     */
    private String signDate;
    /**
     *
     */
    private String contractType;
    /**
     *
     */
    private String signSub;
    /**
     *
     */
    private BigDecimal contractIncome;
    /**
     *
     */
    private String effectDate;
    /**
     *
     */
    private String endDate;
    /**
     *
     */
    private Date signEndTime;
    /**
     *
     */
    private BigDecimal annualIncome;
    /**
     *
     */
    private Long oppoId;
    /**
     *
     */
    private String createDate;
    /**
     *
     */
    private Long orderId;
    /**
     *
     */
    private Long mainIncom;
    /**
     *
     */
    private String jsDate;
    /**
     *
     */
    private String payPattern;
    /**
     *
     */
    private String staDimension;
    /**
     *
     */
    private Long executorContract;
    /**
     *
     */
    private String executorContractLogin;
    /**
     *
     */
    private String executorContractName;
    /**
     *
     */
    private BigDecimal signExpense;
    /**
     *
     */
    private String drafterContract;
    /**
     *
     */
    private String localsn;

}
