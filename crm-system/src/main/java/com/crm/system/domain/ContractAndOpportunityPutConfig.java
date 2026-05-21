package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("contract_and_opportunity_put_config")
public class ContractAndOpportunityPutConfig {
    @TableId(value = "id_")
    private Integer id;
    @TableField(value = "area_name")
    private String areaName;
    @TableField(value = "area_eng")
    private String areaEng;
    @TableField(value = "area_id")
    private Integer areaId;
    @TableField(value = "ftp_directory")
    private String ftpDirectory;
}
