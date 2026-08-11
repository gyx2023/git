package com.crm.system.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 云智眼配置信息 提交/回显参数（模块6）
 */
@Data
public class ZqztLedgerCloudConfigBo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String deviceName;
    private String deviceType;
    private String accessProtocol;
    private String deviceId;
    private String devicePassword;
    private String gbId;
    private String sipServerId;
    private String sipServiceDomain;
    private String sipServerPort;
    private String sipServerIp;
    private String channelCodeId;
    private Integer sortOrder;
}
