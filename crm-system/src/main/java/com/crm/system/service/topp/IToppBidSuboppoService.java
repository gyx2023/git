package com.crm.system.service.topp;

import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.topp.ToppBidSuboppoBo;
import com.crm.system.domain.topp.ToppBidSuboppoDo;
import com.crm.system.domain.vo.topp.ToppBidSuboppoVo;

import java.util.Collection;
import java.util.List;

/**
 * 商机标讯信息Service接口
 *
 * @author crm
 * @date 2026-01-09
 */
public interface IToppBidSuboppoService {

    /**
     * 查询商机标讯信息
     */
    ToppBidSuboppoVo queryById(Long id);

    /**
     * 查询商机标讯信息列表
     */
    TableDataInfo<ToppBidSuboppoVo> queryPageList(ToppBidSuboppoBo bo, PageQuery pageQuery);

    /**
     * 查询商机标讯信息列表
     */
    List<ToppBidSuboppoVo> queryList(ToppBidSuboppoBo bo);

    /**
     * 新增商机标讯信息
     */
    Boolean insertByBo(ToppBidSuboppoBo bo);

    /**
     * 修改商机标讯信息
     */
    Boolean updateByBo(ToppBidSuboppoBo bo);

    /**
     * 校验并批量删除商机标讯信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<String>  selectBidSuboppoList(List< String> oppCodes);

    void  insertDoBatch(List<ToppBidSuboppoDo> list);
}
