package com.crm.common.core.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 老项目商机导出DTO
 * 对应老项目ExportOpportunityService中的37个导出字段
 *
 * @author wcf
 * @date 2026-02-09
 */
@Data
public class OldOpportunityDTO {

    /** 1. 商机编号 */
    private String oppCode;

    /** 2. 商机名称 */
    private String oppName;

    /** 3. 业务类型（主） */
    private String oppType;

    /** 4. 业务类型（次） */
    private String oppIncludeType;

    /** 5. 所属一级行业 */
    private String unicomType;

    /** 6. 商机来源 */
    private String oppSource;

    /** 7. 预计合同金额 */
    private BigDecimal expectContractMoney;

    /** 8. 月收入贡献 */
    private BigDecimal monthIncomeContribution;

    /** 9. 预估合同周期 */
    private Long contractMonth;

    /** 10. 是否招投标 */
    private String isBidding;

    /** 11. 集团客户编号 */
    private String custNo;

    /** 12. 集团客户名称 */
    private String custName;

    /** 13. 创建时间 */
    private Date createTime;

    /** 14. 商机状态 */
    private String oppoState;

    /** 15. 商机创建人姓名 */
    private String creatorName;

    /** 16. 商机创建人电话 */
    private String creatorPhone;

    /** 17. 商机创建人归属地市 */
    private String creatorCity;

    /** 18. 商机创建人归属区县 */
    private String creatorCounty;

    /** 19. 商机创建人归属部门 */
    private String creatorOrg;

    /** 20. 商机客户经理姓名 */
    private String custManagerName;

    /** 21. 商机客户经理电话 */
    private String custManagerPhone;

    /** 22. 商机客户经理归属地市 */
    private String custManagerCity;

    /** 23. 商机客户经理归属区县 */
    private String custManagerCounty;

    /** 24. 商机客户经理归属部门 */
    private String custManagerOrg;

    /** 25. 商机支撑经理姓名 */
    private String designerName;

    /** 26. 商机支撑经理电话 */
    private String designerPhone;

    /** 27. 商机支撑经理归属地市 */
    private String designerCity;

    /** 28. 商机支撑经理归属区县 */
    private String designerCounty;

    /** 29. 商机支撑经理归属部门 */
    private String designerOrg;

    /** 30. 是否有效 */
    private String isValid;

    /** 31. 主要产品/解决方案 */
    private String mainProduct;

    /** 32. 项目来源分类 */
    private String oppDataReform;

    /** 33. 商机签约概率 */
    private String oppWinProb;

    /** 34. 预计签约时间 */
    private Date signTime;

    /** 35. 商机增长类型 */
    private String oppGrowthType;

    /** 36. 绑定合同 */
    private String contractNames;

    /** 37. 电子表单 */
    private String eformNames;
}
