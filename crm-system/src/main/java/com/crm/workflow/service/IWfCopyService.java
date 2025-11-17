package com.crm.workflow.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.workflow.domain.bo.WfCopyBo;
import com.crm.workflow.domain.bo.WfTaskBo;
import com.crm.workflow.domain.vo.WfCopyVo;

import java.util.List;

/**
 * 流程抄送Service接口
 *
 * @author KonBAI
 * @date 2022-05-19
 */
public interface IWfCopyService {

    /**
     * 查询流程抄送
     *
     * @param copyId 流程抄送主键
     * @return 流程抄送
     */
    WfCopyVo queryById(Long copyId);

    /**
     * 查询流程抄送列表
     *
     * @param wfCopy 流程抄送
     * @return 流程抄送集合
     */
    TableDataInfo<WfCopyVo> selectPageList(WfCopyBo wfCopy, PageQuery pageQuery);

    /**
     * 查询流程抄送列表
     *
     * @param wfCopy 流程抄送
     * @return 流程抄送集合
     */
    List<WfCopyVo> selectList(WfCopyBo wfCopy);

    /**
     * 抄送
     * @param taskBo
     * @return
     */
    Boolean makeCopy(WfTaskBo taskBo);
}
