package com.crm.system.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 云智眼设备配置信息查询 响应数据体
 */
@Data
public class ZqztCloudConfigRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cameraAccount;
    private String orderNo;
    private List<ZqztCloudConfigItemDTO> cloudConfigList;
}
