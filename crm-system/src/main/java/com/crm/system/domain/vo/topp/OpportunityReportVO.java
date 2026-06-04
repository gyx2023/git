package com.crm.system.domain.vo.topp;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 千项万亿工程报表 VO
 * 标准英文命名，杜绝中文属性！
 */
@Data
public class OpportunityReportVO {

    /**
     * 地市
     */
    private String city;

    /**
     * 商机编号
     */
    private String oppCode;

    /**
     * 商机名称
     */
    private String oppName;

    /**
     * 客户行业
     */
    private String customerIndustry;

    /**
     * 商机类型
     */
    private String oppType;

    /**
     * 商机金额万
     */
    private BigDecimal oppMoney;

    /**
     * 子单数量
     */
    private Integer cnt;

    /**
     * 投标ID
     */
    private String id;

    /**
     * 合同总金额
     */
    private BigDecimal total;
}
