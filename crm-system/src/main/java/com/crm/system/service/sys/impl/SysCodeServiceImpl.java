package com.crm.system.service.sys.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.sys.ISysCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一编码Service业务层处理
 *
 * @author crm
 * @date 2025-12-15
 */
@RequiredArgsConstructor
@Service
public class SysCodeServiceImpl implements ISysCodeService {

    private final SysCodeMapper baseMapper;

    /**
     * 查询统一编码
     */
    @Override
    public SysCodeVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询统一编码列表
     */
    @Override
    public TableDataInfo<SysCodeVo> queryPageList(SysCodeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysCode> lqw = buildQueryWrapper(bo);
        Page<SysCodeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询统一编码列表
     */
    @Override
    public List<SysCodeVo> queryList(SysCodeBo bo) {
        LambdaQueryWrapper<SysCode> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysCode> buildQueryWrapper(SysCodeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getName()), SysCode::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), SysCode::getType, bo.getType());
        lqw.eq(StringUtils.isNotBlank(bo.getCode()), SysCode::getCode, bo.getCode());
        lqw.eq(bo.getOrder() != null, SysCode::getOrder, bo.getOrder());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), SysCode::getRemark, bo.getRemark());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSystem()), SysCode::getIsSystem, bo.getIsSystem());
        lqw.eq(bo.getSettingInt1() != null, SysCode::getSettingInt1, bo.getSettingInt1());
        lqw.eq(bo.getSettingInt2() != null, SysCode::getSettingInt2, bo.getSettingInt2());
        lqw.eq(StringUtils.isNotBlank(bo.getSettingStr1()), SysCode::getSettingStr1, bo.getSettingStr1());
        lqw.eq(StringUtils.isNotBlank(bo.getSettingStr2()), SysCode::getSettingStr2, bo.getSettingStr2());
        lqw.eq(StringUtils.isNotBlank(bo.getSettingStr3()), SysCode::getSettingStr3, bo.getSettingStr3());
        return lqw;
    }

    /**
     * 新增统一编码
     */
    @Override
    public Boolean insertByBo(SysCodeBo bo) {
        SysCode add = BeanUtil.toBean(bo, SysCode.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改统一编码
     */
    @Override
    public Boolean updateByBo(SysCodeBo bo) {
        SysCode update = BeanUtil.toBean(bo, SysCode.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysCode entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除统一编码
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public SysCodeVo findCode(String type, String code) {
        SysCodeVo sysCodeVo = new SysCodeVo();
        LambdaQueryWrapper<SysCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(type), SysCode::getType, type);
        lqw.eq(StringUtils.isNotBlank(code), SysCode::getCode, code);
        List<SysCodeVo> list = baseMapper.selectVoList(lqw);
        if (ObjectUtil.isNotEmpty(list)) {
            sysCodeVo = list.get(0);
        }
        return sysCodeVo;
    }

    @Override
    public String findNameByCodes(String type, String codes) {
        if (StringUtils.isEmpty(codes)) {
            return "";
        }

        // 将codes按逗号分割成数组
        String[] codeArray = codes.split(",");

        // 构建IN查询条件
        LambdaQueryWrapper<SysCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(type), SysCode::getType, type);
        lqw.in(ObjectUtil.isNotEmpty(codeArray), SysCode::getCode, (Object[]) codeArray);

        List<SysCode> list = baseMapper.selectList(lqw);

        StringBuilder names = new StringBuilder();
        for (SysCode code : list) {
            names.append(code.getName()).append(",");
        }

        String result = names.toString();
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * 根据类型查询编码列表并转换为LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, String> getCodeMapByType(String type) {
        LambdaQueryWrapper<SysCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysCode::getType, type);
        lqw.orderByAsc(SysCode::getOrder);
        List<SysCode> list = baseMapper.selectList(lqw);

        LinkedHashMap<String, String> codeMap = new LinkedHashMap<>();
        if (ObjectUtil.isNotEmpty(list)) {
            for (SysCode sysCode : list) {
                if (StringUtils.isNotBlank(sysCode.getCode())) {
                    codeMap.put(sysCode.getCode(), sysCode.getName());
                }
            }
        }
        return codeMap;
    }

    /**
     * 根据类型查询编码的settingStr1映射
     */
    @Override
    public HashMap<String, String> getCodeSettingStr1MapByType(String type) {
        LambdaQueryWrapper<SysCode> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysCode::getType, type);
        List<SysCode> list = baseMapper.selectList(lqw);

        HashMap<String, String> codeMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(list)) {
            for (SysCode sysCode : list) {
                if (StringUtils.isNotBlank(sysCode.getCode()) && StringUtils.isNotBlank(sysCode.getSettingStr1())) {
                    codeMap.put(sysCode.getCode(), sysCode.getSettingStr1());
                }
            }
        }
        return codeMap;
    }
}
