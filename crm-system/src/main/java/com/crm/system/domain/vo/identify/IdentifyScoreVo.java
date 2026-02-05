package com.crm.system.domain.vo.identify;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 identify_score
 *
 * @author crm
 * @date 2026-02-02
 */
@Data
@ExcelIgnoreUnannotated
public class IdentifyScoreVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 对应 identify_score_customer表的id
     */
    @ExcelProperty(value = "对应 identify_score_customer表的id")
    private Long customerId;

    /**
     * 总分
     */
    @ExcelProperty(value = "总分")
    private String totalScore;

    /**
     * 智能体接口返回详细内容
     */
    @ExcelProperty(value = "智能体接口返回详细内容")
    private String content;

    /**
     * 附件原始名称
     */
    @ExcelProperty(value = "附件原始名称")
    private String name;

    /**
     * 调用接口状态,0待调用，1成功,2失败
     */
    @ExcelProperty(value = "调用接口状态,0待调用，1成功,2失败")
    private String scoreStatus;

    /**
     * 上传时间
     */
    @ExcelProperty(value = "上传时间")
    private Date uploadTime;

    /**
     * 上传人员
     */
    @ExcelProperty(value = "上传人员")
    private Long uploader;

    private Long taskId;


}
