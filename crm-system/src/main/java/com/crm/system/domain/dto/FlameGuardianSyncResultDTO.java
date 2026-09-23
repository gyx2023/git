package com.crm.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 火焰卫士同步结果统计
 */
@Data
public class FlameGuardianSyncResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 本轮扫描到的待处理条数 */
    private Integer total = 0;

    /** 同步成功条数 */
    private Integer success = 0;

    /** 同步失败条数 */
    private Integer failed = 0;

    /** 本轮回收的僵死「同步中」条数 */
    private Integer reclaimed = 0;
}
