package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 钉钉消息对象 ding_message
 *
 * @author crm
 */
@Data
@TableName("ding_message")
public class DingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息类型:text oa link
     */
    @TableField(value = "msg_type")
    private String msgType;

    /**
     * 消息内容
     */
    @TableField(value = "content_")
    private String content;

    /**
     * 消息链接
     */
    @TableField(value = "msg_url")
    private String msgUrl;

    /**
     * oa消息的body form 的json
     */
    @TableField(value = "body_form")
    private String bodyForm;

    /**
     * 发送人逗号分开
     */
    @TableField(value = "user_ids")
    private String userIds;

    /**
     * 发送成功时间
     */
    @TableField(value = "send_time")
    private Date sendTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 是否发送
     */
    @TableField(value = "is_send")
    private Short isSend;

    /**
     * 标题
     */
    @TableField(value = "titile_")
    private String titile;

    /**
     * 短信发送
     */
    @TableField(value = "short_send")
    private String shortSend;

    /**
     * 短信发送时间
     */
    @TableField(value = "short_send_time")
    private Date shortSendTime;

    /**
     * 钉钉消息来源 W-维系助理,其他均为商机应用
     */
    @TableField(value = "ding_msg_source")
    private String dingMsgSource;

    /**
     * 预设发送时间,到达发送时间才发送,否则不发送
     */
    @TableField(value = "setting_send_time")
    private Date settingSendTime;
}
