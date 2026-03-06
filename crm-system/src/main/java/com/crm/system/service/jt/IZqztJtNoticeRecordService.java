package com.crm.system.service.jt;


import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.jt.ZqztJtNoticeRecordBo;
import com.crm.system.domain.vo.jt.ZqztJtNoticeRecordVo;

import java.util.Collection;
import java.util.List;

/**
 * 集团任务前置通知接口记录表 Service接口
 *
 * @author crm
 * @date 2025-12-31
 */
public interface IZqztJtNoticeRecordService {

    /**
     * 查询集团任务前置通知接口记录表
     */
    ZqztJtNoticeRecordVo queryById(Long id);

    /**
     * 查询集团任务前置通知接口记录表列表
     */
    TableDataInfo<ZqztJtNoticeRecordVo> queryPageList(ZqztJtNoticeRecordBo bo, PageQuery pageQuery);

    /**
     * 查询集团任务前置通知接口记录表列表
     */
    List<ZqztJtNoticeRecordVo> queryList(ZqztJtNoticeRecordBo bo);

    /**
     * 新增集团任务前置通知接口记录表
     */
    Boolean insertByBo(ZqztJtNoticeRecordBo bo);

    /**
     * 修改集团任务前置通知接口记录表
     */
    Boolean updateByBo(ZqztJtNoticeRecordBo bo);

    /**
     * 校验并批量删除集团任务前置通知接口记录表
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
