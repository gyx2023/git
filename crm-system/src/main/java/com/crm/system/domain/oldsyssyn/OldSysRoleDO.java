package com.crm.system.domain.oldsyssyn;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
@ExcelIgnoreUnannotated
public class OldSysRoleDo {


    @TableId("id_")
    private Integer id;

    @TableField("name_")
    private String name;

    // 角色类型
    @TableField("type_")
    private String type;

    // 是否有效
    @TableField("is_valid")
    private Boolean valid;

	/* gxc 20141203 取消组织和角色之间的关系
	// 所属组织
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "org_id")
	private SysOrg org;
	*/

    // 功能列表
    @TableField("function_list")
    private String functionList;

    // 备注
    @TableField("remark_")
    private String remark;

    //是否全省通用角色 0:是 1:否
    @TableField("is_universal")
    private int isUniversal=1;
}
