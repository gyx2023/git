package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 云智眼配置信息子表 zqzt_ledger_cloud_config
 */
@Data
@TableName("zqzt_ledger_cloud_config")
public class ZqztLedgerCloudConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联台账主表ID */
    private Long ledgerId;

    /** 设备名称 */
    private String deviceName;

    /** 摄像头接入方式(摄像头直接接入/硬盘录像机接入) */
    private String cameraAccessMode;

    /** 摄像头通道数 */
    private Integer cameraChannelCount;

    /** 设备类型(IPC设备/NVR设备) */
    private String deviceType;

    /** 接入协议 */
    private String accessProtocol;

    /** 设备ID */
    private String deviceId;

    /** 设备密码 */
    private String devicePassword;

    /** 国标ID */
    private String gbId;

    /** SIP服务器ID */
    private String sipServerId;

    /** SIP服务域 */
    private String sipServiceDomain;

    /** SIP服务器端口 */
    private String sipServerPort;

    /** SIP服务器IP */
    private String sipServerIp;

    /** 通道名称(竖线|分隔多个) */
    private String channelName;

    /** 通道编码ID */
    private String channelCodeId;

    /**
     * 火焰卫士受理结果回调
     */
    private String deviceDesc;

    /** 设备创建时间 */
    private Date deviceCreateTime;

    /** 用户名（国标编码） */
    @TableField("device_gbCode")
    private String deviceGbCode;

    /** 资源包 ID */
    private String comboId;

    /** 资源包名称 */
    private String comboName;

    /** 服务节点 */
    private String serviceNode;

    /** 带宽账号（火焰卫士回调按设备回填）*/
    private String broadbandAccount;

    /** 通道竣工状态，多个用 | 分隔存储 */
    private String channelCodeStatus;

    /** 宽带竣工时间 */
    private Date broadbandFinishDate;

    /** 宽带是否利旧 OLD-利旧/NEW-新装 */
    private String broadbandIsReuse;

    /** 摄像头（组网）账号 */
    private String cameraAccount;

    /** 摄像头账号竣工时间 */
    private Date cameraFinishDate;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 是否有效(0失效/1有效) */
    private String isValid;
}
