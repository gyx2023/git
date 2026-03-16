package com.crm.system.service;

import com.crm.system.domain.GrpCustInuseUserlist;
import com.crm.system.domain.vo.GrpCustInuseUserlistVo;
import com.crm.system.domain.bo.GrpCustInuseUserlistBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 政企实际使用客户类型用户清单Service接口
 *
 * @author crm
 * @date 2026-01-20
 */
public interface IGrpCustInuseUserlistService {

    /**
     * 查询政企实际使用客户类型用户清单
     */
    GrpCustInuseUserlistVo queryById(String inuseId);

    /**
     * 查询政企实际使用客户类型用户清单列表
     */
    TableDataInfo<GrpCustInuseUserlistVo> queryPageList(GrpCustInuseUserlistBo bo, PageQuery pageQuery);

    /**
     * 查询政企实际使用客户类型用户清单列表
     */
    List<GrpCustInuseUserlistVo> queryList(GrpCustInuseUserlistBo bo);

    /**
     * 新增政企实际使用客户类型用户清单
     */
    Boolean insertByBo(GrpCustInuseUserlistBo bo);

    /**
     * 修改政企实际使用客户类型用户清单
     */
    Boolean updateByBo(GrpCustInuseUserlistBo bo);

    /**
     * 校验并批量删除政企实际使用客户类型用户清单信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
}
