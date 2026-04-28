package com.crm.askdata.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单轮对话实体（BE-10）
 * <p>
 * 记录每轮澄清对话的关键信息，最多保留 10 轮
 *
 * @author crm
 */
@Data
public class DialogueTurn {

    /** 轮次序号 */
    private Integer turnNumber;

    /** 用户提问（本轮原始输入） */
    private String userQuery;

    /** 用户对本轮追问的回答（由下一轮进入时回填） */
    private String userAnswer;

    /** 是否需要澄清 */
    private Boolean needClarify;

    /** AI 生成的澄清问题（透传 HiAgent 输出） */
    private String clarifyQuestion;

    /** AI 生成的澄清选项（透传 HiAgent 输出） */
    private List<String> clarifyOptions;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 耗时 (ms) */
    private Long latencyMs;
}
