package com.crm.system.service.topp;

import com.crm.system.domain.vo.topp.ToppOppPreEstimateVo;
import com.crm.system.domain.bo.topp.ToppOppPreEstimateBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商机前评估信息Service接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface IToppOppPreEstimateService {

    /**
     * 查询商机前评估信息
     */
    ToppOppPreEstimateVo queryById(Long id);

    /**
     * 查询商机前评估信息列表
     */
    TableDataInfo<ToppOppPreEstimateVo> queryPageList(ToppOppPreEstimateBo bo, PageQuery pageQuery);

    /**
     * 查询商机前评估信息列表
     */
    List<ToppOppPreEstimateVo> queryList(ToppOppPreEstimateBo bo);

    /**
     * 新增商机前评估信息
     */
    Boolean insertByBo(ToppOppPreEstimateBo bo);

    /**
     * 修改商机前评估信息
     */
    Boolean updateByBo(ToppOppPreEstimateBo bo);

    /**
     * 校验并批量删除商机前评估信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    ToppOppPreEstimateVo getPreEstimateByOppId(Long oppId);

}
