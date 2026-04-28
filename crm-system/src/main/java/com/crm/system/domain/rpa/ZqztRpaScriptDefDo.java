package com.crm.system.domain.rpa;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * rpa行级脚本定义对象 zqzt_rpa_script_def
 *
 * @author crm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_rpa_script_def")
public class ZqztRpaScriptDefDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private String scriptCode;

    private String scriptName;

    private String scriptContent;

    private String status;

    private String scriptVersion;

    private String remark;
}
