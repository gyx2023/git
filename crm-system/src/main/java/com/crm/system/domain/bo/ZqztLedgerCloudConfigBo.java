package com.crm.system.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 云智眼配置信息 提交/回显参数
 */
@Data
public class ZqztLedgerCloudConfigBo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceName;
    private String cameraAccessMode;
    private Integer cameraChannelCount;
    private String deviceType;
    private String accessProtocol;
    private String deviceId;
    private String devicePassword;
    private String gbId;
    private String sipServerId;
    private String sipServiceDomain;
    private String sipServerPort;
    private String sipServerIp;
    private String channelName;
    private String channelCodeId;
    private Integer sortOrder;
}
