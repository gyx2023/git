package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * RPA业务重试配置 zqzt_rpa_business_retry_config
 *
 * @author crm
 */
@Data
@TableName("zqzt_rpa_business_retry_config")
public class ZqztRpaBusinessRetryConfigDo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型编码
     */
    private String bizCode;

    /**
     * 重试间隔(秒)
     */
    private Integer retryIntervalSeconds;

    /**
     * 最大重试次数, 999表示无限重试
     */
    private Integer maxRetryCount;

    /**
     * 是否启用: 0-否, 1-是
     */
    private Integer isEnabled;

    /**
     * 备注
     */
    private String remark;

    private Date createTime;

    private Date updateTime;
}
