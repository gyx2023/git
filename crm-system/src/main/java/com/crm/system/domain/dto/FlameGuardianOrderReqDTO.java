package com.crm.system.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 写入受理单信息 请求参数
 * <p>对应火焰卫士网关 POST /gateway/merchant/rpa/order/apply，以覆盖方式写入本机。</p>
 */
@Data
public class FlameGuardianOrderReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 受理单订单号 */
    private String orderId;

    /** 省 */
    private String province;

    /** 地市 */
    private String city;

    /** 区县 */
    private String district;

    /** 街道 */
    private String street;

    /** 云智眼用户名（云账号） */
    private String cloudId;

    /** 机构名称（企业名称） */
    private String merchantName;

    /** 云智眼对应机构ID（企业ID） */
    private String merchantNum;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 经营场所 */
    private String manageAddress;

    /** 行业编码：1 尾矿库 ... 13 其他 */
    private String industry;

    /** 企业注册地址 */
    private String contactAddress;

    /** 企业负责人列表（1~5个） */
    private List<Person> contactPersonList;

    /** 企业安全责任人列表（1~5个） */
    private List<Person> safetyPersonList;

    /** 宽带服务商类型：unicom 联通、other 其他 */
    private String broadbandServiceType;

    /** 带宽账号（最多10个） */
    private List<String> broadbandAccountList;

    /** 设备列表 */
    private List<Device> deviceList;

    /** 资源包列表 */
    private List<Combo> comboList;

    /**
     * 联系人（企业负责人 / 安全责任人）
     */
    @Data
    public static class Person implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 姓名 */
        private String personName;

        /** 联系方式 */
        private String personPhone;
    }

    /**
     * 设备
     */
    @Data
    public static class Device implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 设备名称 */
        private String deviceName;

        /** 设备类型：1 IPC摄像头、4 NVR摄像头 */
        private Integer deviceType;

        /** 通道数，整数 ≥1 */
        private Integer channelCount;

        /** 设备密码 */
        private String password;
    }

    /**
     * 资源包
     */
    @Data
    public static class Combo implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 资源包 ID */
        private String comboId;

        /** 资源包接入数量（路） */
        private Integer channelAllocate;
    }
}
