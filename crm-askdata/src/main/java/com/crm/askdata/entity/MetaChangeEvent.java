package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 元数据变更事件记录
 * <p>
 * 记录指标/维度的每次变更，用于审计追溯
 *
 * @author crm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("meta_change_event")
public class MetaChangeEvent {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件类型：CREATE/UPDATE/DELETE
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 实体类型：METRIC/DIMENSION
     */
    @TableField("entity_type")
    private String entityType;

    /**
     * 实体 ID
     */
    @TableField("entity_id")
    private Long entityId;

    /**
     * 实体代码
     */
    @TableField("entity_code")
    private String entityCode;

    /**
     * 旧版本
     */
    @TableField("old_version")
    private Integer oldVersion;

    /**
     * 新版本
     */
    @TableField("new_version")
    private Integer newVersion;

    /**
     * 变更人
     */
    @TableField("changed_by")
    private String changedBy;

    /**
     * 变更时间
     */
    @TableField("changed_at")
    private Date changedAt;
}
