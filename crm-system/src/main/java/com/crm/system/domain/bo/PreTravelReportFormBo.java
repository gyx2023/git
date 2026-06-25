package com.crm.system.domain.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 行前报告表单提交业务对象
 *
 * @author crm
 * @date 2026-06-17
 */
@Data
public class PreTravelReportFormBo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自然客户编码 */
    @NotBlank(message = "请选择客户")
    private String natureCustId;

    /** 自然客户名称 */
    @NotBlank(message = "客户名称不能为空")
    private String natureCustName;

    /** 拜访人姓名 */
    private String visitorName;

    /** 拜访人职位 */
    private String visitorTitle;

    /** 拜访时间（格式 yyyy-MM-dd HH:mm） */
    @NotBlank(message = "拜访时间不能为空")
    private String visitTime;

    /** 拜访目的（最大100字） */
    @NotBlank(message = "拜访目的不能为空")
    @Size(max = 100, message = "拜访目的最多100字")
    private String visitPurpose;
}
