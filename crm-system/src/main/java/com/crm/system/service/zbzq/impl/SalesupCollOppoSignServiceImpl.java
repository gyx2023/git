package com.crm.system.service.zbzq.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.zbzq.SalesupCollOppoSignBo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoSignVo;
import com.crm.system.domain.dao.zbzq.SalesupCollOppoSignDo;
import com.crm.system.mapper.zbzq.SalesupCollOppoSignMapper;
import com.crm.system.service.zbzq.ISalesupCollOppoSignService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * VIEWService业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class SalesupCollOppoSignServiceImpl implements ISalesupCollOppoSignService {

    private final SalesupCollOppoSignMapper baseMapper;

    /**
     * 关联总部政企平台时商机编码开头会加上36-SJ-前缀
     */
    private static final String ZBZQ_TOPP_OPPO_ID_HEADER = "36-SJ-";

    /**
     * 查询VIEW
     */
    @Override
    public SalesupCollOppoSignVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询VIEW列表
     */
    @Override
    public TableDataInfo<SalesupCollOppoSignVo> queryPageList(SalesupCollOppoSignBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SalesupCollOppoSignDo> lqw = buildQueryWrapper(bo);
        Page<SalesupCollOppoSignVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询VIEW列表
     */
    @Override
    public List<SalesupCollOppoSignVo> queryList(SalesupCollOppoSignBo bo) {
        LambdaQueryWrapper<SalesupCollOppoSignDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SalesupCollOppoSignDo> buildQueryWrapper(SalesupCollOppoSignBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SalesupCollOppoSignDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getIfSign()), SalesupCollOppoSignDo::getIfSign, bo.getIfSign());
        lqw.eq(bo.getSignType() != null, SalesupCollOppoSignDo::getSignType, bo.getSignType());
        lqw.like(StringUtils.isNotBlank(bo.getSignName()), SalesupCollOppoSignDo::getSignName, bo.getSignName());
        lqw.eq(StringUtils.isNotBlank(bo.getClientoId()), SalesupCollOppoSignDo::getClientoId, bo.getClientoId());
        lqw.like(StringUtils.isNotBlank(bo.getClientoName()), SalesupCollOppoSignDo::getClientoName, bo.getClientoName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), SalesupCollOppoSignDo::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getSignDate()), SalesupCollOppoSignDo::getSignDate, bo.getSignDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), SalesupCollOppoSignDo::getContractType, bo.getContractType());
        lqw.eq(StringUtils.isNotBlank(bo.getSignSub()), SalesupCollOppoSignDo::getSignSub, bo.getSignSub());
        lqw.eq(bo.getContractIncome() != null, SalesupCollOppoSignDo::getContractIncome, bo.getContractIncome());
        lqw.eq(StringUtils.isNotBlank(bo.getEffectDate()), SalesupCollOppoSignDo::getEffectDate, bo.getEffectDate());
        lqw.eq(StringUtils.isNotBlank(bo.getEndDate()), SalesupCollOppoSignDo::getEndDate, bo.getEndDate());
        lqw.eq(bo.getSignEndTime() != null, SalesupCollOppoSignDo::getSignEndTime, bo.getSignEndTime());
        lqw.eq(bo.getAnnualIncome() != null, SalesupCollOppoSignDo::getAnnualIncome, bo.getAnnualIncome());
        lqw.eq(bo.getOppoId() != null, SalesupCollOppoSignDo::getOppoId, bo.getOppoId());
        lqw.eq(StringUtils.isNotBlank(bo.getCreateDate()), SalesupCollOppoSignDo::getCreateDate, bo.getCreateDate());
        lqw.eq(bo.getOrderId() != null, SalesupCollOppoSignDo::getOrderId, bo.getOrderId());
        lqw.eq(bo.getMainIncom() != null, SalesupCollOppoSignDo::getMainIncom, bo.getMainIncom());
        lqw.eq(StringUtils.isNotBlank(bo.getJsDate()), SalesupCollOppoSignDo::getJsDate, bo.getJsDate());
        lqw.eq(StringUtils.isNotBlank(bo.getPayPattern()), SalesupCollOppoSignDo::getPayPattern, bo.getPayPattern());
        lqw.eq(StringUtils.isNotBlank(bo.getStaDimension()), SalesupCollOppoSignDo::getStaDimension, bo.getStaDimension());
        lqw.eq(bo.getExecutorContract() != null, SalesupCollOppoSignDo::getExecutorContract, bo.getExecutorContract());
        lqw.eq(StringUtils.isNotBlank(bo.getExecutorContractLogin()), SalesupCollOppoSignDo::getExecutorContractLogin, bo.getExecutorContractLogin());
        lqw.like(StringUtils.isNotBlank(bo.getExecutorContractName()), SalesupCollOppoSignDo::getExecutorContractName, bo.getExecutorContractName());
        lqw.eq(bo.getSignExpense() != null, SalesupCollOppoSignDo::getSignExpense, bo.getSignExpense());
        lqw.eq(StringUtils.isNotBlank(bo.getDrafterContract()), SalesupCollOppoSignDo::getDrafterContract, bo.getDrafterContract());
        lqw.eq(StringUtils.isNotBlank(bo.getLocalsn()), SalesupCollOppoSignDo::getLocalsn, bo.getLocalsn());
        return lqw;
    }

    /**
     * 新增VIEW
     */
    @Override
    public Boolean insertByBo(SalesupCollOppoSignBo bo) {
        SalesupCollOppoSignDo add = BeanUtil.toBean(bo, SalesupCollOppoSignDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改VIEW
     */
    @Override
    public Boolean updateByBo(SalesupCollOppoSignBo bo) {
        SalesupCollOppoSignDo update = BeanUtil.toBean(bo, SalesupCollOppoSignDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SalesupCollOppoSignDo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除VIEW
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
    /**
     * 根据创建时间和编号前缀查询签约商机数据
     *
     * @param days 天数参数
     * @return 签约商机列表
     */
    @Override
    public List<SalesupCollOppoSignVo> queryContractSignData(int days) {
        return baseMapper.selectContractSignData(days, ZBZQ_TOPP_OPPO_ID_HEADER);
    }
}
