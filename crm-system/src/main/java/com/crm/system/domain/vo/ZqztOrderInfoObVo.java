package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 总部OB工单源表查询结果（slave_1, zq_user_36）
 */
@Data
public class ZqztOrderInfoObVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 企业名称 */
    private String companyName;
    /** 社会统一信用代码 */
    private String creditCode;
    /** 注册地址 */
    private String regAddress;
    /** 实际地址 */
    private String actualAddress;
    /** 企业负责人 */
    private String contactName;
    /** 企业负责人联系方式 */
    private String contactPhone;
    /** 联通客户经理 */
    private String developName;
    /** 联通客户经理联系方式 */
    private String developPhone;

    /** t1(sl08md) 签约路数 textField_vLXLQGlK */
    private String signRoutes1;
    /** t1(sl08md) 宽带账号 textField_90QNngmC */
    private String broadbandAccount1;
    /** t1(sl08md) 云智眼账号 textField_GKvzHJez */
    private String cloudAccount1;
    /** t1(sl08md) 摄像头施工账号 textField_0MTXxPrM */
    private String cameraAccount1;

    /** t2(04049f) 签约路数 textField_vLXLQGlK */
    private String signRoutes2;
    /** t2(04049f) 宽带账号 textField_GYjOc1Vy */
    private String broadbandAccount2;
    /** t2(04049f) 云智眼账号 textField_GC5b4aBM */
    private String cloudAccount2;
    /** t2(04049f) 摄像头施工账号 textField_sZFFJVLc */
    private String cameraAccount2;
}
