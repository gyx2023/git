package com.crm.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 云智眼设备配置信息（子表单条，对应 zqzt_ledger_cloud_config）
 */
@Data
public class ZqztCloudConfigItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceName;
    private String deviceType;
    private String accessProtocol;

    /** 通道数（IPC设备默认1，NVR是摄像头数量） */
    private String channelNum;

    private String deviceId;
    private String devicePassword;
    private String gbId;
    private String sipServerId;
    private String sipServiceDomain;
    private String sipServerPort;
    private String sipServerIp;
    private String channelCodeId;

    /** 宽带账号（火焰卫士按设备回填，对应 zqzt_ledger_cloud_config.broadband_account） */
    private String broadbandAccount;
}
