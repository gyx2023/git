package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.SysAreaBo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.SysArea;
import com.crm.system.mapper.SysAreaMapper;
import com.crm.system.service.ISysAreaService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crm.common.constant.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Service
public class SysAreaServiceImpl implements ISysAreaService {

    private final SysAreaMapper baseMapper;
    private static final Logger logger = LoggerFactory.getLogger(SysAreaServiceImpl.class);

    /**
     * 查询【请填写功能名称】
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_AREA, key = "#id")
    public SysAreaVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<SysAreaVo> queryPageList(SysAreaBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysArea> lqw = buildQueryWrapper(bo);
        Page<SysAreaVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<SysAreaVo> queryList(SysAreaBo bo) {
        LambdaQueryWrapper<SysArea> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询浙江省地级市列表
     */
    @Override
    public List<SysAreaVo> queryZhejiangCities() {
        LambdaQueryWrapper<SysArea> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysArea::getParent, "330000");
        lqw.eq(SysArea::getType, "二级");
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 根据地级市ID查询其下辖的区县列表
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_AREA_COUNTY, key = "#areaId")
    public List<SysAreaVo> queryCountyListByCityId(String areaId) {
        LambdaQueryWrapper<SysArea> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysArea::getParent, areaId);
        lqw.eq(SysArea::getType, "三级");
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysArea> buildQueryWrapper(SysAreaBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysArea> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getName()), SysArea::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), SysArea::getType, bo.getType());
        lqw.eq(bo.getParent() != null, SysArea::getParent, bo.getParent());
        lqw.eq(StringUtils.isNotBlank(bo.getPinyin()), SysArea::getPinyin, bo.getPinyin());
        lqw.eq(StringUtils.isNotBlank(bo.getLatlng()), SysArea::getLatlng, bo.getLatlng());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), SysArea::getRemark, bo.getRemark());
        lqw.eq(StringUtils.isNotBlank(bo.getBssCode()), SysArea::getBssCode, bo.getBssCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBssCodeParent()), SysArea::getBssCodeParent, bo.getBssCodeParent());
        lqw.eq(bo.getOrder() != null, SysArea::getOrder, bo.getOrder());
        lqw.eq(bo.getZqType() != null, SysArea::getZqType, bo.getZqType());
        lqw.eq(bo.getZbArea() != null, SysArea::getZbArea, bo.getZbArea());
        lqw.eq(StringUtils.isNotBlank(bo.getJtCode()), SysArea::getJtCode, bo.getJtCode());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    @CacheEvict(cacheNames = {CacheNames.SYS_AREA, CacheNames.SYS_AREA_COUNTY}, allEntries = true)
    public Boolean insertByBo(SysAreaBo bo) {
        SysArea add = BeanUtil.toBean(bo, SysArea.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    @CacheEvict(cacheNames = {CacheNames.SYS_AREA, CacheNames.SYS_AREA_COUNTY}, allEntries = true)
    public Boolean updateByBo(SysAreaBo bo) {
        SysArea update = BeanUtil.toBean(bo, SysArea.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysArea entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    @CacheEvict(cacheNames = {CacheNames.SYS_AREA, CacheNames.SYS_AREA_COUNTY}, allEntries = true)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 获取浙江省下所有地级市
     */
    @Override
    public List<SysAreaVo> getZhejiangCities() {
        logger.info("开始查询浙江省下所有地级市");

        // 根据用户提供的信息，浙江省的ID是330000，名称是"省本级"
        Long zhejiangId = 330000L;

        try {
            // 直接查询所有父级为浙江省(ID=330000)的地区
            List<SysAreaVo> cities = baseMapper.selectVoList(new LambdaQueryWrapper<SysArea>()
                .eq(SysArea::getParent, zhejiangId));
            logger.info("查询到浙江省(ID=330000)下地区数量: {}", cities.size());
            return cities;
        } catch (Exception e) {
            logger.error("查询浙江省下地级市时发生异常: {}", e.getMessage());
        }

        logger.warn("未找到浙江省(ID=330000)下的相关数据");
        return new ArrayList<>();
    }

    /**
     * 根据二级地市ID获取对应的三级县市数据
     */
    @Override
    public List<SysAreaVo> getCountiesByCityId(Long cityId) {
        logger.info("开始根据地市ID查询下属县市，cityId: {}", cityId);

        if (cityId == null) {
            logger.warn("传入的cityId为null");
            return new ArrayList<>();
        }

        try {
            // 先查询指定ID的地市信息
            SysArea city = baseMapper.selectById(cityId);
            if (city != null) {
                logger.info("找到地市信息: {}, Type: {}", city.getName(), city.getType());

                // 查询所有父级为指定地市的地区，不依赖固定的type值
                List<SysAreaVo> counties = baseMapper.selectVoList(new LambdaQueryWrapper<SysArea>()
                    .eq(SysArea::getParent, cityId));
                logger.info("查询到下属县市数量: {}", counties.size());
                return counties;
            } else {
                logger.warn("未找到ID为{}的地市信息", cityId);
            }
        } catch (Exception e) {
            logger.error("查询下属县市时发生异常: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * 根据城市代码前缀获取对应的三级县市数据
     */
    @Override
    public List<SysAreaVo> getCountiesByCityCodePrefix(String cityCodePrefix) {
        logger.info("开始根据城市代码前缀查询下属县市，cityCodePrefix: {}", cityCodePrefix);

        if (cityCodePrefix == null) {
            logger.warn("传入的cityCodePrefix为null");
            return new ArrayList<>();
        }

        try {
            // 查询所有ID前缀匹配且类型为"三级"的地区
            LambdaQueryWrapper<SysArea> lqw = Wrappers.lambdaQuery();
            lqw.apply("id_ LIKE {0}", cityCodePrefix + "%")
               .eq(SysArea::getType, "三级");

            List<SysAreaVo> counties = baseMapper.selectVoList(lqw);
            logger.info("查询到下属县市数量: {}", counties.size());
            return counties;
        } catch (Exception e) {
            logger.error("查询下属县市时发生异常: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    @Override
    public Map<Long, String> listAll() {
        return baseMapper.selectList()
            .stream()
            .collect(Collectors.toMap(SysArea::getId, SysArea::getName));
    }
}
