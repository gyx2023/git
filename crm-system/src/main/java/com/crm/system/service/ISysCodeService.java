package com.crm.system.service;

import com.crm.system.domain.SysCode;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 统一编码Service接口
 *
 * @author crm
 * @date 2025-12-15
 */
public interface ISysCodeService {

    /**
     * 查询统一编码
     */
    SysCodeVo queryById(Long id);

    /**
     * 查询统一编码列表
     */
    TableDataInfo<SysCodeVo> queryPageList(SysCodeBo bo, PageQuery pageQuery);

    /**
     * 查询统一编码列表
     */
    List<SysCodeVo> queryList(SysCodeBo bo);

    /**
     * 新增统一编码
     */
    Boolean insertByBo(SysCodeBo bo);

    /**
     * 修改统一编码
     */
    Boolean updateByBo(SysCodeBo bo);

    /**
     * 校验并批量删除统一编码信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    SysCodeVo findCode(String type, String code);
}
