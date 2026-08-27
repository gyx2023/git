package com.crm.system.domain.dao.rpa;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * rpa错误信息对象 rpa_error_save
 *
 * @author ruoyi
 * @date 2025-09-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rpa_error_save")
public class RpaErrorSaveDo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** ts */
    @TableField(exist = false)
    private Long ts;

    /** 场景名称 */
    private String sceneName;

    /** 场景提出人 */
    private String sceneUser;

    /** 报错信息 */
    private String errorInfo;

    /** 异常单号(可能为空) */
    private String errorId;

    /** type */
    private String type;

    /** message */
    private String message;

    /** extraInfo */
    private String extraInfo;

    /** runningStack */
    private String runningStack;

    /** methodName */
    private String methodName;

    /** bak1 */
    private String bak1;

    /** bak2 */
    private String bak2;

    /** bak3 */
    private String bak3;

    /** bak4 */
    private String bak4;

    /** bak5 */
    private String bak5;

    /** bak6 */
    private String bak6;

    /** bak7 */
    private String bak7;

    /** bak8 */
    private String bak8;

    /** 0-有效，1无效 */
    private String status;


    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("sceneName", getSceneName())
            .append("sceneUser", getSceneUser())
            .append("errorInfo", getErrorInfo())
            .append("errorId", getErrorId())
            .append("type", getType())
            .append("message", getMessage())
            .append("extraInfo", getExtraInfo())
            .append("runningStack", getRunningStack())
            .append("methodName", getMethodName())
            .append("bak1", getBak1())
            .append("bak2", getBak2())
            .append("bak3", getBak3())
            .append("bak4", getBak4())
            .append("bak5", getBak5())
            .append("bak6", getBak6())
            .append("bak7", getBak7())
            .append("bak8", getBak8())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
