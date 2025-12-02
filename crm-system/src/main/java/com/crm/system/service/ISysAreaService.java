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

    /**
     * 获取浙江省下所有地级市
     */
    List<SysAreaVo> getZhejiangCities();

    /**
     * 根据二级地市ID获取对应的三级县市数据
     * @param cityId 二级地市ID
     */
    List<SysAreaVo> getCountiesByCityId(Long cityId);

    /**
     * 根据城市代码前缀获取对应的三级县市数据
     * @param cityCodePrefix 城市代码前缀
     * @return 三级县市列表
     */
    List<SysAreaVo> getCountiesByCityCodePrefix(String cityCodePrefix);
}