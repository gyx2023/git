package com.crm.system.service.tcomm;

import com.crm.system.domain.tcomm.TcommCustomer;
import com.crm.system.domain.vo.tcomm.TcommCustomerVo;
import com.crm.system.domain.bo.tcomm.TcommCustomerBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-18
 */
public interface ITcommCustomerService {

    /**
     * 查询【请填写功能名称】
     */
    TcommCustomerVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<TcommCustomerVo> queryPageList(TcommCustomerBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<TcommCustomerVo> queryList(TcommCustomerBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(TcommCustomerBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(TcommCustomerBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
