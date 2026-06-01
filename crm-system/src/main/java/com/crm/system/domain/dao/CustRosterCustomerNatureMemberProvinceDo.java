package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class CustRosterCustomerNatureMemberProvinceDo {
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    private Long rosterCustomerId;
    private Long natureCustId;
    private String natureCustName;
    private String orgCode;
    private String tydm;
    private Date createDate;
    private String state;
    private String operationMode;
    private String operationCode;
    private Date stateDate;
    private String remark;
    private Long taskId;
    private String provinceCode;
    private String eparchyCode;
    private String cityCode;
    private String virtualFlag;
    private String md5UniqueColumns;
    private Long insertBatch;
}
