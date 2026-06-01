package com.crm.system.domain.bo.cust;

import lombok.Data;

import java.io.Serializable;

/**
 * 集团客户标签同步请求DTO
 * 用于RPA调用admin接口时的入参
 *
 * @author crm
 */
@Data
public class CustomerTagSyncDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总部集团编码（唯一标识）
     */
    private String hqGrpCode;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 行业分类（中文标签，用于查字典表获取dict_value）
     */
    private String industryClassification;

    /**
     * 直拓类型（中文标签：省直拓/市直拓/县直拓）
     */
    private String directExpansionType;

    /**
     * 发展人OA号
     */
    private String developerOa;

    /**
     * 集团客户编码（即zqzt_grp_group_info的hqGrpCode）
     */
    private String groupCustomerCode;

    /**
     * 业务类型编码(由RPA端传入)
     */
    private String bizCode;
}
