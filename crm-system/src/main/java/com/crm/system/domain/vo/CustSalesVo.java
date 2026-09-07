package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 宽带专项营销客户信息视图对象
 * <p>
 * 数据来源 ftp_broadband_market_cust。
 *
 * @author crm
 */
@Data
public class CustSalesVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 集团客户名称 */
    private String grpName;

    /** 营销类型名称 */
    private String marketingName;

    /** 营销类型编码 */
    private String marketingType;

    /** 地市 */
    private String areaName;

    /** 客户归属县分 */
    private String countyName;

    /** 营销用户数 */
    private String userScale;

    /** 账期 */
    private String statCycle;

    /** 集团客户编号 */
    private String custGrpId;

    /** 客户经理（发展人）编号 */
    private String servManagerId;
}
