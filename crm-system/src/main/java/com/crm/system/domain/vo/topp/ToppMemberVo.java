package com.crm.system.domain.vo.topp;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 topp_member
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@ExcelIgnoreUnannotated
public class ToppMemberVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 	项目ID
     */
    @ExcelProperty(value = "	项目ID	")
    private Long projectId;

    /**
     * 	团队角色
     */
    @ExcelProperty(value = "	团队角色	")
    private String memberRole;

    /**
     * 	成员id
     */
    @ExcelProperty(value = "	成员id	")
    private Long memberId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long memberArea;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long memberOrg;

    /**
     * 	姓名
     */
    @ExcelProperty(value = "	姓名	")
    private String name;

    /**
     * 	职责说明
     */
    @ExcelProperty(value = "	职责说明	")
    private String roleDesc;

    /**
     * 	创建人员
     */
    @ExcelProperty(value = "	创建人员	")
    private Long creator;

    /**
     * 	备注说明
     */
    @ExcelProperty(value = "	备注说明")
    private String memo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String score;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String scoreExplain;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date scoreTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contri;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String teamLeaderFlag;


}
