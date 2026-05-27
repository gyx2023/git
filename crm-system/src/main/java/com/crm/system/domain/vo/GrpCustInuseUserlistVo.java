package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.util.Date;



/**
 * 政企实际使用客户类型用户清单视图对象 grp_cust_inuse_userlist
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@ExcelIgnoreUnannotated
public class GrpCustInuseUserlistVo {

    private static final long serialVersionUID = 1L;

    /**
     * 关系编号
     */
    @ExcelProperty(value = "关系编号")
    private String inuseId;

    /**
     * 用户编号
     */
    @ExcelProperty(value = "用户编号")
    private Long SUBSCRBID;

    /**
     * 业务号码
     */
    @ExcelProperty(value = "业务号码")
    private String svcNum;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 地市
     */
    @ExcelProperty(value = "地市")
    private String regionId;

    /**
     * 所属业务市县
     */
    @ExcelProperty(value = "所属业务市县")
    private String countyId;

    /**
     * 受理营业厅
     */
    @ExcelProperty(value = "受理营业厅")
    private String officeId;

    /**
     * 受理操作员
     */
    @ExcelProperty(value = "受理操作员")
    private String operatorId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String REMARK;


}
