package com.crm.system.service.identify.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.identify.IdentifyScoreCustomerDo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;
import com.crm.system.mapper.identify.IdentifyScoreCustomerMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 三清单智能评分记录Service业务层处理
 *
 * @author crm
 * @date 2026-02-02
 */
@RequiredArgsConstructor
@Service
public class IdentifyScoreCustomerServiceImpl implements IIdentifyScoreCustomerService {
    @Resource
    private  IdentifyScoreCustomerMapper identifyScoreCustomerMapper;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ISysAreaService iSysAreaService;

    @Resource
    private ISysDeptService sysDeptService;
    @Resource
    private ISysAreaService sysAreaService;

    /**
     * 查询三清单智能评分记录
     */
    @Override
    public IdentifyScoreCustomerVo queryById(Long id) {
        return identifyScoreCustomerMapper.selectVoById(id);
    }

    /**
     * 查询三清单智能评分记录列表
     */
    @Override
    public TableDataInfo<IdentifyScoreCustomerVo> queryPageList(IdentifyScoreCustomerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IdentifyScoreCustomerDo> lqw = buildQueryWrapper(bo);
        if(ObjectUtil.isNotEmpty(bo.getCreateTimeBegin())){
            lqw.ge(IdentifyScoreCustomerDo::getCreateTime, bo.getCreateTimeBegin()) ;
        }
        if(ObjectUtil.isNotEmpty(bo.getCreateTimeEnd())){
            lqw.le(IdentifyScoreCustomerDo::getCreateTime, bo.getCreateTimeEnd()) ;
        }
        if(ObjectUtil.isNotEmpty(bo.getMinScore())){
            lqw.ge(IdentifyScoreCustomerDo::getTotalScore, bo.getMinScore()) ;
        }
        if(ObjectUtil.isNotEmpty(bo.getMaxScore())){
            lqw.le(IdentifyScoreCustomerDo::getTotalScore, bo.getMaxScore()) ;
        }
        Page<IdentifyScoreCustomerVo> result = identifyScoreCustomerMapper.selectVoPage(pageQuery.build(), lqw);
        Map<Long, String > userMap = sysUserService.listAll();
        Map<Long, String> areaMap =iSysAreaService.listAll();
        result.getRecords().forEach(item -> {
            item.setUserName(userMap.get(item.getCreatorId()));
            item.setAreaName(areaMap.get(item.getAreaId()));
            item.setCountyName(areaMap.get(item.getCountyId()));
        });
        return TableDataInfo.build(result);
    }

    /**
     * 查询三清单智能评分记录列表
     */
    @Override
    public List<IdentifyScoreCustomerVo> queryList(IdentifyScoreCustomerBo bo) {
        LambdaQueryWrapper<IdentifyScoreCustomerDo> lqw = buildQueryWrapper(bo);
        return identifyScoreCustomerMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<IdentifyScoreCustomerDo> buildQueryWrapper(IdentifyScoreCustomerBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IdentifyScoreCustomerDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getAreaId() != null, IdentifyScoreCustomerDo::getAreaId, bo.getAreaId());
        lqw.eq(bo.getCountyId() != null, IdentifyScoreCustomerDo::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), IdentifyScoreCustomerDo::getCustId, bo.getCustId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), IdentifyScoreCustomerDo::getCustName, bo.getCustName());
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), IdentifyScoreCustomerDo::getFileName, bo.getFileName());
        lqw.eq(bo.getFileUrlId() != null, IdentifyScoreCustomerDo::getFileUrlId, bo.getFileUrlId());
        lqw.eq(StringUtils.isNotBlank(bo.getTotalScore()), IdentifyScoreCustomerDo::getTotalScore, bo.getTotalScore());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), IdentifyScoreCustomerDo::getContent, bo.getContent());
        lqw.like(StringUtils.isNotBlank(bo.getScoreFileName()), IdentifyScoreCustomerDo::getScoreFileName, bo.getScoreFileName());
        lqw.eq(bo.getScoreFileUrlId() != null, IdentifyScoreCustomerDo::getScoreFileUrlId, bo.getScoreFileUrlId());
        lqw.eq(bo.getCreatorId() != null, IdentifyScoreCustomerDo::getCreatorId, bo.getCreatorId());
        lqw.eq(bo.getUpdaterId() != null, IdentifyScoreCustomerDo::getUpdaterId, bo.getUpdaterId());
        return lqw;
    }

    /**
     * 新增三清单智能评分记录
     */
    @Override
    public Boolean insertByBo(IdentifyScoreCustomerBo bo) {
        IdentifyScoreCustomerDo add = BeanUtil.toBean(bo, IdentifyScoreCustomerDo.class);
        validEntityBeforeSave(add);
        boolean flag = identifyScoreCustomerMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改三清单智能评分记录
     */
    @Override
    public Boolean updateByBo(IdentifyScoreCustomerBo bo) {
        IdentifyScoreCustomerDo update = BeanUtil.toBean(bo, IdentifyScoreCustomerDo.class);
        validEntityBeforeSave(update);
        return identifyScoreCustomerMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(IdentifyScoreCustomerDo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除三清单智能评分记录
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return identifyScoreCustomerMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public IdentifyScoreCustomerDo queryByCustName(String custName, Long userId) {
        IdentifyScoreCustomerBo bo = new IdentifyScoreCustomerBo();
        bo.setCustName(custName);
        bo.setCreatorId(userId);
        LambdaQueryWrapper<IdentifyScoreCustomerDo> lqw = buildQueryWrapper(bo);
        return identifyScoreCustomerMapper.selectOne(lqw);
    }

    /**
     * 初始化客户数据（抽取重复逻辑）
     */
    @Override
    public IdentifyScoreCustomerBo initCustomerData(SysUser sysUser, SysOssVo sysOss) {
        IdentifyScoreCustomerBo customerDo = new IdentifyScoreCustomerBo();
        Long ds = null;
        Long xf = null;

        SysDept sysDept = sysDeptService.selectDeptById(sysUser.getDeptId());
        SysAreaVo sysAreaVo = sysAreaService.queryById(Long.valueOf(sysDept.getAreaCode()));
        if(ObjectUtil.equals("一级", sysAreaVo.getType())){

        }else if (ObjectUtil.equals("二级", sysAreaVo.getType())) {
            ds = sysAreaVo.getId();
        } else {
            ds = sysAreaService.queryById(sysAreaVo.getParent()).getId();
            xf = sysAreaVo.getId();
        }

        customerDo.setFileName(sysOss.getOriginalName());
        customerDo.setFileUrlId(sysOss.getOssId());
        customerDo.setCreatorId(sysUser.getUserId());
        customerDo.setUpdaterId(sysUser.getUserId());
        customerDo.setAreaId(ds);
        customerDo.setCountyId(xf);

        return customerDo;
    }
}
