package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 云智眼配置信息 视图对象
 */
@Data
public class ZqztLedgerCloudConfigVo implements Serializable {

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
