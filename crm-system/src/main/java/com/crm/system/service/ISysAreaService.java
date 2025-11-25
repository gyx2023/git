package com.crm.system.service;

import com.crm.system.domain.SysArea;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.bo.SysAreaBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface ISysAreaService {

    /**
     * 查询【请填写功能名称】
     */
    SysAreaVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<SysAreaVo> queryPageList(SysAreaBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<SysAreaVo> queryList(SysAreaBo bo);

    /**
     * 查询浙江省地级市列表
     */
    List<SysAreaVo> queryZhejiangCities();

    /**
     * 根据地级市ID查询其下辖的区县列表
     */
    List<SysAreaVo> queryCountyListByCityId(String areaId);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(SysAreaBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(SysAreaBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
