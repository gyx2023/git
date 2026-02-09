package com.crm.workflow.domain.vo;

import lombok.Data;

/**
 * 部署实例和表单关联视图对象
 *
 * @author KonBAI
 * @createTime 2022/7/17 18:29
 */
@Data
public class WfDeployFormVo {

    private static final long serialVersionUID = 1L;

    /**
     * 流程部署主键
     */
    private String deployId;

    /**
     * 表单Key
     */
    private String formKey;

    /**
     * 节点Key
     */
    private String nodeKey;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 表单内容
     */
    private String content;

    /**
     * 表单类型：system-系统表单，custom-自定义URL
     */
    private String formType;

    /**
     * 自定义表单URL路径
     */
    private String formUrl;

}
