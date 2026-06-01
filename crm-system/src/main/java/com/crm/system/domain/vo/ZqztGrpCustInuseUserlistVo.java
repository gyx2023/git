package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.utils.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 政企实际使用客户类型用户清单视图对象 zqzt_grp_cust_inuse_userlist
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@ExcelIgnoreUnannotated
public class ZqztGrpCustInuseUserlistVo {

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
     * 地市（转为政企的编码）
     */
    @ExcelProperty(value = "地市", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "转=为政企的编码")
    private String regionId;

    /**
     * 所属业务市县（转为政企的编码）
     */
    @ExcelProperty(value = "所属业务市县", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "转=为政企的编码")
    private String countyId;

    /**
     * 受理操作员（存政企的用）
     */
    @ExcelProperty(value = "受理操作员", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "存=政企的用")
    private String operatorId;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String REMARK;

    /**
     * 是否有效：1有效，0无效
     */
    @ExcelProperty(value = "是否有效：1有效，0无效")
    private String isValid;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;


}
