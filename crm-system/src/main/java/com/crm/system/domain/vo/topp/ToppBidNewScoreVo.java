package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 topp_bid_new_score
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@ExcelIgnoreUnannotated
public class ToppBidNewScoreVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 新招投标id
     */
    @ExcelProperty(value = "新招投标id")
    private Long toppBidNewId;

    /**
     * 商机编码
     */
    @ExcelProperty(value = "商机编码")
    private String opportunityCode;

    /**
     * 支撑经理姓名
     */
    @ExcelProperty(value = "支撑经理姓名")
    private String name;

    /**
     * 角色
     */
    @ExcelProperty(value = "角色")
    private String role;

    /**
     * 是否录入支撑工单
     */
    @ExcelProperty(value = "是否录入支撑工单")
    private String isNotSupport;

    /**
     * 贡献度
     */
    @ExcelProperty(value = "贡献度")
    private String contribution;

    /**
     * 创建人员
     */
    @ExcelProperty(value = "创建人员")
    private String creator;

    /**
     * 状态0:保存,1:审核中，2:结束
     */
    @ExcelProperty(value = "状态0:保存,1:审核中，2:结束")
    private String status;


}
