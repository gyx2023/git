package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 行业大类-亚类-细类分类对象 sys_category
 *
 * @author crm
 * @date 2025-11-24
 */
@Data
@TableName("zqzt_sys_category")
public class SysCategory implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     * 行业唯一编码，层级联动
     */
    @TableId(value = "id")
    private String id;
    /**
     * 行业名称
     */
    private String name;
    /**
     * 层级类型（大类、亚类、细类）
     */
    private String type;
    /**
     * 父级编码，关联上级ID
     */
    private String parent;
    /**
     * 行业名称拼音，用于检索
     */
    private String pinyin;

}
