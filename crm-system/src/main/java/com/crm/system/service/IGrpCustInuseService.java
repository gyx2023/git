package com.crm.system.service;

import com.crm.system.domain.GrpCustInuse;
import com.crm.system.domain.vo.GrpCustInuseVo;
import com.crm.system.domain.bo.GrpCustInuseBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 政企实际使用客户类型Service接口
 *
 * @author crm
 * @date 2026-01-20
 */
public interface IGrpCustInuseService {

    /**
     * 查询政企实际使用客户类型
     */
    GrpCustInuseVo queryById(String inuseId);

    /**
     * 查询政企实际使用客户类型列表
     */
    TableDataInfo<GrpCustInuseVo> queryPageList(GrpCustInuseBo bo, PageQuery pageQuery);

    /**
     * 查询政企实际使用客户类型列表
     */
    List<GrpCustInuseVo> queryList(GrpCustInuseBo bo);

    /**
     * 新增政企实际使用客户类型
     */
    Boolean insertByBo(GrpCustInuseBo bo);

    /**
     * 修改政企实际使用客户类型
     */
    Boolean updateByBo(GrpCustInuseBo bo);

    /**
     * 校验并批量删除政企实际使用客户类型信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}
