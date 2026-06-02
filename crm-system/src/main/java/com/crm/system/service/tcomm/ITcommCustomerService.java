package com.crm.system.service.tcomm;

import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.domain.vo.tcomm.TcommCustomerVo;
import com.crm.system.domain.bo.tcomm.TcommCustomerBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.Date;
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

    /**
     * 查询满足过滤条件的客户数据
     * 条件：在tcomm_customer表中的id_存在于topp_opportunity表中创建时间在指定时间之后的cust_id列表中
     *
     * @param createTime 创建时间下限
     * @return 满足条件的客户数据列表
     */
    List<TcommCustomer> selectFilteredCustomers(Date createTime);
}
