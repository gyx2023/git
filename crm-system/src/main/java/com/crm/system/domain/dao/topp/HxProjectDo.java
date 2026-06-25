package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 划小项目对象 hx_project
 */
@Data
@TableName("hx_project")
public class HxProjectDo {

    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /** 商机ID */
    private Long oppoId;

    /** 商机编号 */
    private String oppoCode;

    /** 需求编号 */
    private String remandCode;

    /** 需求名称 */
    private String remandName;

    /** 地市编码 */
    private String city;

    /** 地市名称 */
    private String cityName;

    /** 区县编码 */
    private String county;

    /** 区县名称 */
    private String countyName;

    /** 客户名称 */
    private String custName;

    /** 客户编号 */
    private String custCode;

    /** 登录账号 */
    private String loginName;

    /** 创建时间 */
    private Date createdDate;
}
