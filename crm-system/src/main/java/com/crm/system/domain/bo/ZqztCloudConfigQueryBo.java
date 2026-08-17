package com.crm.system.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 云智眼设备配置信息查询 请求参数
 */
@Data
public class ZqztCloudConfigQueryBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 摄像头账号，对应 zqzt_ledger_info.camera_account */
    private String cameraAccount;

    /** 应用id */
    private String appid;

    /** 加密串 */
    private String sign;

    /** 时间戳 yyyy-MM-dd HH:mm:ss */
    private String timestamp;
}
