package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 topp_member
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_member")
public class ToppMemberDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 	项目ID
     */
    private Long projectId;
    /**
     * 	团队角色
     */
    private String memberRole;
    /**
     * 	成员id
     */
    private Long memberId;
    /**
     *
     */
    private Long memberArea;
    /**
     *
     */
    private Long memberOrg;
    /**
     * 	姓名
     */
    private String name;
    /**
     * 	职责说明
     */
    private String roleDesc;
    /**
     * 	创建人员
     */
    private Long creator;
    /**
     * 	备注说明
     */
    private String memo;
    /**
     *
     */
    private String score;
    /**
     *
     */
    private String scoreExplain;
    /**
     *
     */
    private Date scoreTime;
    /**
     *
     */
    private String contri;
    /**
     *
     */
    private String teamLeaderFlag;

}
