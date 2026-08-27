package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 台账省市区县街道 视图对象（下拉用）
 */
@Data
public class ZqztLedgerAreaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orgCode;
    private String orgName;
    private String parentCode;
    private String orgLevel;
}
