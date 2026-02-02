package com.crm.system.service.identify;

import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;

import java.util.Collection;
import java.util.List;

/**
 * 三清单智能评分记录Service接口
 *
 * @author crm
 * @date 2026-02-02
 */
public interface IIdentifyScoreCustomerService {

    /**
     * 查询三清单智能评分记录
     */
    IdentifyScoreCustomerVo queryById(Long id);

    /**
     * 查询三清单智能评分记录列表
     */
    TableDataInfo<IdentifyScoreCustomerVo> queryPageList(IdentifyScoreCustomerBo bo, PageQuery pageQuery);

    /**
     * 查询三清单智能评分记录列表
     */
    List<IdentifyScoreCustomerVo> queryList(IdentifyScoreCustomerBo bo);

    /**
     * 新增三清单智能评分记录
     */
    Boolean insertByBo(IdentifyScoreCustomerBo bo);

    /**
     * 修改三清单智能评分记录
     */
    Boolean updateByBo(IdentifyScoreCustomerBo bo);

    /**
     * 校验并批量删除三清单智能评分记录信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
