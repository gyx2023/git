package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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

    /** 工单创建时间（sl08md.createTime / 04049f.createTime） */
    private Date orderCreateTime;

    /** t1(sl08md) 签约路数 textField_vLXLQGlK */
    private String signRoutes1;
    /** t1(sl08md) 利旧宽带账号 textField_ahdM4O0A */
    private String broadbandAccountOld1;
    /** t1(sl08md) 新装宽带账号 textField_90QNngmC（为空取 textField_YkrgCq0G） */
    private String broadbandAccountNew1;
    /** t1(sl08md) 云智眼账号 textField_GKvzHJez */
    private String cloudAccount1;
    /** t1(sl08md) 摄像头施工账号 textField_0MTXxPrM */
    private String cameraAccount1;
    /** t1(sl08md) 视频资源包ID textField_lasPZdW7 */
    private String cloudVisionPackageId1;

    /** t2(04049f) 签约路数 textField_vLXLQGlK */
    private String signRoutes2;
    /** t2(04049f) 利旧宽带账号 textField_ahdM4O0A */
    private String broadbandAccountOld2;
    /** t2(04049f) 新装宽带账号 textField_GYjOc1Vy（为空取 textField_IIPTA5sx） */
    private String broadbandAccountNew2;
    /** t2(04049f) 云智眼账号 textField_GC5b4aBM */
    private String cloudAccount2;
    /** t2(04049f) 摄像头施工账号 textField_sZFFJVLc */
    private String cameraAccount2;
    /** t2(04049f) 视频资源包ID textField_kjpWKhE9 */
    private String cloudVisionPackageId2;
    /** t1(sl08md) 安全负责人1 textField_DXBr43jw */
    private String safetyOfficerName1;
    /** t1(sl08md) 安全负责人1联系方式 textField_8jIxQJdp */
    private String safetyOfficerPhone1;
    /** t2(04049f) 安全负责人1 textField_BHw221Qs */
    private String safetyOfficerName2;
    /** t2(04049f) 安全负责人1联系方式 textField_IrBYU7dG */
    private String safetyOfficerPhone2;
}
