package com.crm.system.service.topp;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.topp.ToppMemberBo;
import com.crm.system.domain.vo.topp.ToppMemberVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-02
 */
public interface IToppMemberService {

    /**
     * 查询【请填写功能名称】
     */
    ToppMemberVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppMemberVo> queryPageList(ToppMemberBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppMemberVo> queryList(ToppMemberBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppMemberBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppMemberBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
