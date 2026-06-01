package com.crm.system.domain.vo.topp;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 商机标讯信息视图对象 topp_bid_suboppo
 *
 * @author crm
 * @date 2026-01-09
 */
@Data
@ExcelIgnoreUnannotated
public class ToppBidSuboppoVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @ExcelProperty(value = "主键ID，自增")
    private Long id;

    /**
     * 关联商机ID
     */
    @ExcelProperty(value = "关联商机ID")
    private Long projectId;

    /**
     * 标讯ID
     */
    @ExcelProperty(value = "标讯ID")
    private String infoId;

    /**
     * 标讯任务ID
     */
    @ExcelProperty(value = "标讯任务ID")
    private String bidTaskManagerId;

    /**
     * 商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌
     */
    @ExcelProperty(value = "商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌")
    private String colorBrand;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;


}
