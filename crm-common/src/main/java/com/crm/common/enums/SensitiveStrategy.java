package com.crm.common.enums;

import cn.hutool.core.util.DesensitizedUtil;
import com.crm.common.utils.Desensitization;
import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * 脱敏策略
 *
 * @author Yjoioooo
 * @version 3.6.0
 */
@AllArgsConstructor
public enum SensitiveStrategy {

    /**
     * 身份证脱敏
     */
    ID_CARD(s -> DesensitizedUtil.idCardNum(s, 3, 4)),

    /**
     * 手机号脱敏
     */
    PHONE(DesensitizedUtil::mobilePhone),

    /**
     * 地址脱敏
     */
    ADDRESS(s -> DesensitizedUtil.address(s, 8)),

    /**
     * 邮箱脱敏
     */
    EMAIL(DesensitizedUtil::email),

    /**
     * 银行卡
     */
    BANK_CARD(DesensitizedUtil::bankCard),

    /**
     * 公司或集团名称脱敏
     */
    COMPANY_NAME(Desensitization::desensitizeCompanyName),

    /**
     * 个人姓名脱敏
     */
    NAME(Desensitization::desensitizeName),

    /**
     * 合同名称脱敏
     */
    CONTRACT_NAME(Desensitization::desensitizeContractName),

    /**
     * 手机号码脱敏（自定义规则）
     */
    CUSTOM_PHONE(Desensitization::desensitizePhoneNumber);

    //可自行添加其他脱敏策略

    private final Function<String, String> desensitizer;

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
