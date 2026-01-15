package com.crm.system.domain.seq;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 sequence
 *
 * @author crm
 * @date 2026-01-14
 */
@Data
@TableName("sequence")
public class SequenceDo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    private String seqName;
    /**
     *
     */
    private Long currentVal;
    /**
     *
     */
    private Long incrementVal;
    /**
     * 查询日期
     */
    private String searchDate;

}
