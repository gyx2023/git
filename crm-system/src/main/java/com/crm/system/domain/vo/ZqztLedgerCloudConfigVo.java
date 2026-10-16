package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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

    /** 通道竣工状态，多个用 | 分隔 */
    private String channelCodeStatus;

    /** 宽带账号 */
    private String broadbandAccount;

    /** 宽带竣工时间 */
    private Date broadbandFinishDate;

    /** 宽带是否利旧 OLD-利旧/NEW-新装 */
    private String broadbandIsReuse;

    /** 摄像头（组网）账号 */
    private String cameraAccount;

    /** 摄像头账号竣工时间 */
    private Date cameraFinishDate;
}
