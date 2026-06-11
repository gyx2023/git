package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author PENGJ
 */
@TableName("sequence")
@Data
public class Sequence implements Serializable {
    private static final long serialVersionUID = -8905057588463374080L;
    @TableField("seq_name")
    private String seqName;
    @TableField("current_val")
    private Integer currentVal;
    @TableField("increment_val")
    private Integer incrementVal;
    @TableField("search_date")
    private String searchDate;

}
