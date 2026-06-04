package com.crm.common.core.domain.dto;

import lombok.Data;
import java.util.Date;

@Data
public class WorkOrderDTO {

    /** 工单编码 */
    private String workOrderCode;

    /** 工单状态 2=结束 4=处理中 */
    private String workOrderStatus;

    /** 工单名称 */
    private String workOrderName;

    /** 工单时间 */
    private String workOrderTime;

    /** 机构ID */
    private String orgName;

    /** 员工OA */
    private String managerNo;

    /** 员工姓名 */
    private String managerName;

    /** 员工手机 */
    private String managerMobile;

    /** 实际处理人OA */
    private String actualManagerNo;

    /** 实际处理人姓名 */
    private String actualManagerName;

    /** 实际处理人手机 */
    private String actualManagerMobile;

    /** 客户ID */
    private String customId;

    /** 客户名称 */
    private String customName;

    /** 创建时间 */
    private String gmtCreate;

    /** 完成时间 */
    private String backTime;

    /** 区域ID */
    private String areaId;

    /** 楼宇ID */
    private String buildId;

    /** 楼宇名称 */
    private String buildName;

    /** 业务类型 */
    private String category;

    /** 业务到期时间 */
    private String businessEndDate;

    /** 合同名称 */
    private String contractName;

    /** 合同编码 */
    private String contractCode;

    /** 客户编号 */
    private String custNo;

    /** 业务号码 */
    private String svcNum;

}
