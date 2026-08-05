package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 商机标讯信息业务对象 topp_bid_suboppo
 *
 * @author crm
 * @date 2026-01-09
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppBidSuboppoBo extends BaseEntity {

    /**
     * 主键ID，自增
     */
    @NotNull(message = "主键ID，自增不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商机ID
     */
    @NotNull(message = "关联商机ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long projectId;

    /**
     * 标讯ID
     */
    @NotBlank(message = "标讯ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String infoId;

    /**
     * 标讯任务ID
     */
    @NotBlank(message = "标讯任务ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidTaskManagerId;

    /**
     * 商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌
     */
    @NotBlank(message = "商机挂牌类型: GREEN绿牌/NONE未挂牌/RED红牌/YELLOW黄牌不能为空", groups = { AddGroup.class, EditGroup.class })
    private String colorBrand;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectName;

    /**
     * 商机编号（业务层面唯一标识）
     */
    private String oppoCode;

    /**
     * 项目编码
     */
    private String projectCode;

    /**
     * 发布日期
     */
    private Date releaseDate;

    /**
     *  是否有效
     */
    private boolean valid;
}
