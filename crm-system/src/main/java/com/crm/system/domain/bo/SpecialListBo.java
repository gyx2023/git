package com.crm.system.domain.bo;

import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 特殊名单业务对象 zqzt_sys_speciallist
 *
 * @author crm
 * @date 2026-06-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialListBo extends BaseEntity {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 目标值
     */
    private String targetValue;

    /**
     * 状态
     */
    private String status;

    /**
     * 生效时间
     */
    private Date effectiveTime;

    /**
     * 失效时间
     */
    private Date expireTime;

    /**
     * 原因
     */
    private String reason;

    /**
     * 生效时间开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTimeStart;

    /**
     * 生效时间结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTimeEnd;

    /**
     * 创建时间开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeStart;

    /**
     * 创建时间结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTimeEnd;

    /**
     * ID集合（用于批量操作）
     */
    private List<Integer> ids;

}
