package com.crm.system.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 云智眼账户改密 请求参数
 */
@Data
public class ZqztCloudPasswordBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 台账主键ID */
    @NotNull(message = "台账主键不能为空")
    private Long ledgerId;

    /** 新密码 */
    private String newPassword;

    /** 短信验证码 */
    private String smsCode;
}
