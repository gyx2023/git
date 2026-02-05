package com.crm.system.service.identify;

import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2026-02-02
 */
public interface IIdentifyScoreService {

    /**
     * 查询【请填写功能名称】
     */
    IdentifyScoreVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<IdentifyScoreVo> queryPageList(IdentifyScoreBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<IdentifyScoreVo> queryList(IdentifyScoreBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(IdentifyScoreBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(IdentifyScoreBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    Boolean save(Long[] ossIds);

    IdentifyScoreDo queryByCustomerId(Long custmoerId,Long taskId);


}
