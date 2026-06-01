package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 【请填写功能名称】对象 topp_bid_new_score
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_bid_new_score")
public class ToppBidNewScoreDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 新招投标id
     */
    private Long toppBidNewId;
    /**
     * 商机编码
     */
    private String opportunityCode;
    /**
     * 支撑经理姓名
     */
    private String name;
    /**
     * 角色
     */
    private String role;
    /**
     * 是否录入支撑工单
     */
    private String isNotSupport;
    /**
     * 贡献度
     */
    private String contribution;
    /**
     * 创建人员
     */
    private String creator;
    /**
     * 状态0:保存,1:审核中，2:结束
     */
    private String status;

}
