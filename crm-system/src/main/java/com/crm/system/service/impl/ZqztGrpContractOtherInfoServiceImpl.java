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
import org.springframework.beans.factory.annotation.Autowired;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.ZqztGrpContractOtherInfoLog;
import com.crm.system.mapper.ZqztGrpContractOtherInfoMapper;
import com.crm.system.mapper.ZqztGrpContractOtherInfoLogMapper;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.crm.system.service.ICustMaintInfoService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 集团合同扩展信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-13
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpContractOtherInfoServiceImpl implements IZqztGrpContractOtherInfoService {

    private final ZqztGrpContractOtherInfoMapper baseMapper;

    @Autowired
    private ZqztGrpContractOtherInfoLogMapper logMapper;

    @Resource
    private ICustMaintInfoService custMaintInfoService;

    /**
     * 查询集团合同扩展信息
     */
    @Override
    public ZqztGrpContractOtherInfoVo queryById(String contractId){
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询集团合同扩展信息列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractOtherInfoVo> queryPageList(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpContractOtherInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询集团合同扩展信息列表
     */
    @Override
    public List<ZqztGrpContractOtherInfoVo> queryList(ZqztGrpContractOtherInfoBo bo) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpContractOtherInfo> buildQueryWrapper(ZqztGrpContractOtherInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpContractOtherInfo::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoTag()), ZqztGrpContractOtherInfo::getInfoTag, bo.getInfoTag());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal()), ZqztGrpContractOtherInfo::getInfoVal, bo.getInfoVal());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal2()), ZqztGrpContractOtherInfo::getInfoVal2, bo.getInfoVal2());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ZqztGrpContractOtherInfo::getIsValid, bo.getIsValid());
        return lqw;
    }


    /**
     * 新增集团合同扩展信息
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractOtherInfoBo bo) {
        ZqztGrpContractOtherInfo add = BeanUtil.toBean(bo, ZqztGrpContractOtherInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改集团合同扩展信息
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractOtherInfoBo bo) {
        // 首先查询原有记录
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ZqztGrpContractOtherInfo::getContractId, bo.getContractId());
        List<ZqztGrpContractOtherInfo> existingRecords = baseMapper.selectList(queryWrapper);

        // 将原有记录备份到日志表
        if (!existingRecords.isEmpty()) {
            for (ZqztGrpContractOtherInfo record : existingRecords) {
                ZqztGrpContractOtherInfoLog logRecord = BeanUtil.toBean(record, ZqztGrpContractOtherInfoLog.class);
                // 设置日志记录的ID为null，以便自动生成新ID
                logRecord.setId(null);
                logMapper.insert(logRecord);
            }
        }

        // 删除原有记录
        baseMapper.delete(queryWrapper);

        // 插入新记录
        ZqztGrpContractOtherInfo newRecord = BeanUtil.toBean(bo, ZqztGrpContractOtherInfo.class);
        validEntityBeforeSave(newRecord);
        boolean result = baseMapper.insert(newRecord) > 0;
        return result;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpContractOtherInfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除集团合同扩展信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 插入日志记录
     */
    public void insertLogRecord(ZqztGrpContractOtherInfoLog logRecord) {
        logMapper.insert(logRecord);
    }

    /**
     * 全量导入集团合同扩展信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllGroupContractOtherInfo(List<ZqztGrpContractOtherInfo> dataList) {
        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        // 使用批量插入方法提高性能
        baseMapper.insertBatch(dataList);

        return dataList.size();
    }

    /**
     * 根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     * 如存在则正常入库（状态=1有效），同时写入服务号码；
     * 否则入库时状态默认=99（迁移失效）
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllGroupContractOtherInfoWithBusinessLogic(List<ZqztGrpContractOtherInfo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        baseMapper.delete(null);

        // 根据user_id验证并设置相应状态
        List<ZqztGrpContractOtherInfo> processedDataList = processBusinessLogic(dataList);

        baseMapper.insertBatch(processedDataList);

        return processedDataList.size();
    }

    /**
     * 处理业务逻辑：根据user_id查询验证并设置相应状态
     * 根据infoVal作为user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     * 如存在则正常入库（状态=1有效），同时写入服务号码；否则入库时状态默认=99（迁移失效）
     *
     * @param dataList 原始数据列表
     * @return 处理后的数据列表
     */
    private List<ZqztGrpContractOtherInfo> processBusinessLogic(List<ZqztGrpContractOtherInfo> dataList) {
        // 提取所有需要验证的user_id
        List<String> userIds = dataList.stream()
            .map(item -> item.getInfoVal()) // 使用infoVal作为user_id
            .filter(userId -> userId != null && !userId.trim().isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.toList());

        // 批量查询用户维护信息
        Map<String, String> svcNumMap = new HashMap<>();
        if (custMaintInfoService != null && !userIds.isEmpty()) {
            svcNumMap = custMaintInfoService.selectValidSvcNumByUserIds(userIds);
        }

        // 处理每个数据项
        List<ZqztGrpContractOtherInfo> processedDataList = new ArrayList<>();
        for (ZqztGrpContractOtherInfo item : dataList) {
            ZqztGrpContractOtherInfo processedItem = new ZqztGrpContractOtherInfo();
            processedItem.setContractId(item.getContractId());
            processedItem.setInfoTag(item.getInfoTag());
            processedItem.setInfoVal(item.getInfoVal());
            processedItem.setCreateTime(item.getCreateTime());

            // 获取user_id并验证
            String userId = item.getInfoVal();
            if (userId != null && svcNumMap.containsKey(userId) && svcNumMap.get(userId) != null) {
                // 用户存在且有效（status=1），正常入库，状态=1有效，同时写入服务号码
                processedItem.setIsValid("1");
                // 获取服务号码
                String svcNum = svcNumMap.get(userId);
                processedItem.setInfoVal2(svcNum);
            } else {
                // 用户不存在或无效，入库时状态默认=99（迁移失效）
                processedItem.setIsValid("99");
                processedItem.setInfoVal2(item.getInfoVal2()); // 保持原始值或为null
            }

            processedDataList.add(processedItem);
        }

        return processedDataList;
    }

    /**
     * 根据合同ID查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @return 集团合同扩展信息列表
     */
    @Override
    public List<ZqztGrpContractOtherInfoVo> selectByContractId(String contractId) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ZqztGrpContractOtherInfo::getContractId, contractId);
        lqw.eq(ZqztGrpContractOtherInfo::getIsValid, "1"); // 只查询有效的记录
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 根据合同ID和服务号码查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码
     * @return 集团合同扩展信息列表
     */
    @Override
    public List<ZqztGrpContractOtherInfoVo> selectByContractIdAndInfoVal2(String contractId, String infoVal2) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ZqztGrpContractOtherInfo::getContractId, contractId);
        lqw.eq(ZqztGrpContractOtherInfo::getIsValid, "1"); // 只查询有效的记录
        lqw.like(infoVal2 != null && !infoVal2.isEmpty(), ZqztGrpContractOtherInfo::getInfoVal2, infoVal2); // 按服务号码模糊查询
        return baseMapper.selectVoList(lqw);
    }


}
