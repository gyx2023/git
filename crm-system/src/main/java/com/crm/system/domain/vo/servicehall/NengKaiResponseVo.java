package com.crm.system.domain.vo.servicehall;

import java.io.Serializable;

/**
 * 能开接口返回参数
 */
public class NengKaiResponseVo implements Serializable {
    private static final long serialVersionUID = -3743827844997912752L;

    private static final String ERROR_STATUS = "1";

    private static final String SUCCESS_STATUS = "0";

    /**
     * 处理结果 0成功；否则失败
     */
    private String status;

    /**
     * 返回信息 失败时返回错误信息
     */
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static NengKaiResponseVo error(String message) {
        NengKaiResponseVo vo = new NengKaiResponseVo();
        vo.setStatus(ERROR_STATUS);
        vo.setMessage(message);
        return vo;
    }

    public static NengKaiResponseVo success() {
        NengKaiResponseVo vo = new NengKaiResponseVo();
        vo.setStatus(SUCCESS_STATUS);
        vo.setMessage("处理成功");
        return vo;
    }
}
