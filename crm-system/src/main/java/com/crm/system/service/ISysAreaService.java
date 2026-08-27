package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.bo.SysAreaBo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysAreaTreeVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * 根据地级市BSS编码查询其下辖的区县列表（维系预警Tab3/4/5 客户维度用）
     */
    List<SysAreaVo> queryCountyListByBssCode(String bssCode);

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

    Map<Long, String> listAll();

    List<SysArea> queryLikeList(String city);

    List<SysArea> findEmpAreaIdList(Integer empAreaId);

    /**
     * 查询区域树（递归包含所有子节点）
     */
    List<SysAreaTreeVo> queryAreaTree(Long areaId);

    /**
     * 根据区域ID解析为地市级范围（含其下辖区县）。
     * <ul>
     * <li>三级(区县)：向上找到所属地市 → 返回 [地市 + 下辖区县]</li>
     * <li>二级(地市)：返回 [地市 + 下辖区县]</li>
     * <li>一级(省级)：返回 [省级]</li>
     * </ul>
     * 未找到上级时回退为返回当前区域ID。
     *
     * @param areaId 当前区域ID
     * @return 区域ID列表
     */
    List<Long> resolveCityLevelScope(Long areaId);

    /**
     * 根据区域ID解析人员搜索范围（含子区域）。
     * <ul>
     * <li>三级(区县)：返回 [本区县]</li>
     * <li>二级(地市)：返回 [地市 + 下辖区县]</li>
     * <li>一级(省级)：返回 null（全省，不限制）</li>
     * </ul>
     *
     * @param areaId 当前区域ID
     * @return 区域ID列表，null 表示不限制范围
     */
    List<Long> resolveAreaScope(Long areaId);
}
