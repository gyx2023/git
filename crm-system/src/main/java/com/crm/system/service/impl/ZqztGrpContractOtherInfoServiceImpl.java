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
import com.crm.system.service.external.IExternalCustMaintInfoService;
import com.crm.common.utils.spring.SpringUtils;

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
    private IExternalCustMaintInfoService externalCustMaintInfoService;

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
     * 实现逻辑：先将原有记录移动到日志表，然后删除原记录，再插入新记录
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
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        // 使用批量插入方法提高性能
        baseMapper.insertBatch(dataList);

        return dataList.size();
    }

    /**
     * 查询所有集团合同扩展信息
     *
     * @return 集团合同扩展信息列表
     */
    @Override
    public List<ZqztGrpContractOtherInfo> selectAllZqztGrpContractOtherInfo() {
        return baseMapper.selectList(null);
    }

    /**
     * 全量导入集团合同扩展信息数据（包含业务逻辑处理）
     * 根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     * 如存在则正常入库（状态=1有效），同时写入服务号码；否则入库时状态默认=99（迁移失效）
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllGroupContractOtherInfoWithBusinessLogic(List<ZqztGrpContractOtherInfo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        // 处理业务逻辑：根据user_id验证并设置相应状态
        List<ZqztGrpContractOtherInfo> processedDataList = processBusinessLogic(dataList);

        // 使用批量插入方法提高性能
        baseMapper.insertBatch(processedDataList);

        return processedDataList.size();
    }

    /**
     * 处理业务逻辑：根据user_id查询验证并设置相应状态
     *
     * @param dataList 原始数据列表
     * @return 处理后的数据列表
     */
    private List<ZqztGrpContractOtherInfo> processBusinessLogic(List<ZqztGrpContractOtherInfo> dataList) {
        // 提取所有需要验证的user_id
        List<String> userIds = dataList.stream()
            .map(item -> extractUserId(item.getInfoVal()))
            .filter(userId -> userId != null && !userId.trim().isEmpty())
            .distinct()
            .collect(Collectors.toList());

        // 批量查询用户维护信息
        Map<String, Map<String, Object>> custMaintInfoMap = new HashMap<>();
        if (externalCustMaintInfoService != null && !userIds.isEmpty()) {
            custMaintInfoMap = externalCustMaintInfoService.batchGetCustMaintInfoByUserIds(userIds);
        }

        // 处理每个数据项
        List<ZqztGrpContractOtherInfo> processedDataList = new ArrayList<>();
        for (ZqztGrpContractOtherInfo item : dataList) {
            ZqztGrpContractOtherInfo processedItem = new ZqztGrpContractOtherInfo();
            processedItem.setContractId(item.getContractId());
            processedItem.setInfoTag(item.getInfoTag());
            processedItem.setInfoVal(item.getInfoVal());

            // 获取user_id并验证
            String userId = extractUserId(item.getInfoVal());
            if (userId != null && custMaintInfoMap.containsKey(userId)) {
                // 用户存在且有效（status=1），正常入库，可设置额外字段
                // 注意：实体类中可能需要添加状态字段来存储验证结果
                // processedItem.setStatus("1"); // 如果实体类中有status字段
            } else {
                // 用户不存在或无效，可能需要特殊处理
                // processedItem.setStatus("99"); // 如果实体类中有status字段
            }

            processedDataList.add(processedItem);
        }

        return processedDataList;
    }

    /**
     * 从infoVal中提取user_id信息
     *
     * @param infoVal 信息值
     * @return user_id
     */
    private String extractUserId(String infoVal) {
        // 根据实际数据格式实现提取逻辑
        // 这里只是一个示例实现
        if (infoVal != null && infoVal.contains(":")) {
            return infoVal.split(":")[0]; // 假设格式为 userId:value
        }
        return infoVal;
    }
}
