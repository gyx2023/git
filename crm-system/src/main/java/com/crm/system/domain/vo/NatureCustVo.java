package com.crm.system.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 自然客户下拉选项 VO
 *
 * @author crm
 * @date 2026-06-17
 */
@Data
public class NatureCustVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自然客户编码 */
    private String natureCustId;

    /** 自然客户名称 */
    private String natureCustName;
}
