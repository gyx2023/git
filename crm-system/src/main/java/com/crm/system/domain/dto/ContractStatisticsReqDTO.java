package com.crm.system.domain.dto;

import lombok.Data;

@Data
public class ContractStatisticsReqDTO {
    /**
     * 客户行业
     */
    private String[] unicomType;
    /**
     * 投标主体
     */
    private String[] bidMainPersonnel;
    /**
     * 竞争情况
     */
    private String[] jzqk;
    /**
     * 项目类型
     */
    private String[] projectType;
    /**
     * 投标结果
     */
    private String bidResult;

    /**
     * 毛利率 最小
     */
    private String minMll;

    /**
     * 毛利率 最大
     */
    private String maxMll;
    /**
     * 部门id
     */
    private String deptId;
    /**
     * 时间范围
     */
    private String[] dateRange;
    /**
     *
     */
    private String kbsj;

    private String minCgAmount;

    private String maxCgAmount;

    private String minYjAmount;

    private String maxYjAmount;

    private String minSjAmount;

    private String maxSjAmount;

    private Long  tempDeptId;

    private  String startTime;

    private  String endTime;



    private String unicomTypeT;
    /**
     * 投标主体
     */
    private String bidMainPersonnelT;
    /**
     * 竞争情况
     */
    private String jzqkT;
    /**
     * 项目类型
     */
    private String projectTypeT;

}
