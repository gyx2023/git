package com.crm.system.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 火焰卫士受理订单执行结果通知 入参
 */
@Data
public class FlameGuardianOrderResultBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作类型：T1-RPA执行结果回填 / T2-设备信息修改 */
    private String operType;

    /** 受理单订单号，与写入接口的 orderId 一致 */
    private String orderId;

    /** 总执行是否成功 */
    private Boolean allSuccess;

    /** 总结果描述；失败时返回原因 */
    private String allDesc;

    /** 总执行时长（秒） */
    private Long allTime;

    /**
     * 各步骤执行结果列表
     * <p>整体以 json 存入 step_list，不做字段回读，用 Map 承接以保留报文的原始结构。</p>
     */
    private List<Map<String, Object>> stepList;

    /** 云智眼对应机构ID（企业ID） */
    private String merchantNum;

    /** 设备列表 */
    private List<Device> deviceList;

    /** 应用id */
    private String appid;

    /** 加密串 */
    private String sign;

    /** yyyy-MM-dd HH:mm:ss，与当前时间相差不超过 5 分钟 */
    private String timestamp;

    /**
     * 设备结果（只声明需要沉淀的字段）
     */
    @Data
    public static class Device implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 设备名称，用于与台账云智眼配置子表按名称匹配 */
        private String deviceName;

        /** 设备描述 */
        private String desc;

        /** 带宽账号 */
        private String broadbandAccount;

        /** 设备创建时间（yyyy-MM-dd HH:mm:ss） */
        private String deviceCreateTime;

        /** 用户名（国标编码） */
        private String deviceGbCode;

        /** 设备国标 ID */
        private String deviceGbId;

        /** 资源包 ID */
        private String comboId;

        /** 资源包名称 */
        private String comboName;

        /** 服务节点 */
        private String serviceNode;

        /** 设备 ID（T2 匹配键，T2 时必填） */
        private String deviceId;

        /** 设备类型：1 IPC / 4 NVR */
        private Integer deviceType;

        /** 通道数 */
        private Integer channelCount;

        /** 设备密码 */
        private String password;

        /** 接入协议；默认 GB28181 */
        private String accessProtocol;

        /** SIP IP */
        private String sipIp;

        /** SIP 端口 */
        private String sipPort;

        /** SIP ID */
        private String sipId;

        /** SIP 服务域 */
        private String sipDomain;

        /** 通道列表 */
        private List<ChannelCode> channelCodeList;
    }

    /** 通道信息 */
    @Data
    public static class ChannelCode implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 通道编码 ID */
        private String channelCodeId;

        /** 通道名称 */
        private String channelCodeName;

        /** 竣工状态 */
        private String channelCodeStatus;
    }
}
