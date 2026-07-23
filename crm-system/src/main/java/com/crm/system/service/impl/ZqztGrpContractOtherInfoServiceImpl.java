package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.encrypt.DataEncryptionUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.ZqztCustMaintInfo;
import com.crm.system.service.cust.IZqztCustMaintInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.dao.ZqztGrpContractOtherInfo;
import com.crm.system.domain.dao.ZqztGrpContractOtherInfoLog;
import com.crm.system.mapper.ZqztGrpContractOtherInfoMapper;
import com.crm.system.mapper.ZqztGrpContractOtherInfoLogMapper;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import com.crm.system.service.ITfFCustGroupMService;
import com.crm.system.enums.InfoTagEnum;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集团合同扩展信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-13
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class ZqztGrpContractOtherInfoServiceImpl implements IZqztGrpContractOtherInfoService {

    private final ZqztGrpContractOtherInfoMapper baseMapper;

    @Autowired
    private ZqztGrpContractOtherInfoLogMapper logMapper;

    @Resource
    private IZqztCustMaintInfoService zqztCustMaintInfoService;

    @Autowired
    private ITfFCustGroupMService tfFCustGroupMService;

    @Autowired
    private DataEncryptionUtil encryptionUtil;

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
     * 批量新增集团合同扩展信息
     */
    @Override
    public Boolean insertBatchByBo(List<ZqztGrpContractOtherInfoBo> boList) {
        if (boList == null || boList.isEmpty()) {
            return false;
        }

        // 对请求参数中的infoVal2字段进行解密处理，并设置固定的info_tag
        for (ZqztGrpContractOtherInfoBo bo : boList) {
            bo.setInfoVal2(encryptionUtil.decryptData(bo.getInfoVal2()));
            // 设置固定的info_tag值
            bo.setInfoTag(InfoTagEnum.GRP_CONTRACT_USER_LIST.getValue());
        }

        List<ZqztGrpContractOtherInfo> entities = boList.stream()
            .map(bo -> {
                ZqztGrpContractOtherInfo entity = BeanUtil.toBean(bo, ZqztGrpContractOtherInfo.class);
                validEntityBeforeSave(entity);
                return entity;
            })
            .collect(Collectors.toList());

        boolean flag = baseMapper.insertBatch(entities);
        if (flag) {
            for (int i = 0; i < boList.size(); i++) {
                boList.get(i).setContractId(entities.get(i).getContractId());
            }
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
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 全量导入集团合同扩展信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllClear(List<ZqztGrpContractOtherInfo> dataList) {
        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

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
    public int importAll(List<ZqztGrpContractOtherInfo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
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
            .collect(Collectors.toList());

        // 批量查询用户维护信息
        Map<String, String> svcNumMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<ZqztCustMaintInfo> custMaintInfoList = zqztCustMaintInfoService.selectExistingUserIds(userIds);
            svcNumMap = custMaintInfoList.stream()
                .collect(Collectors.toMap(
                    ZqztCustMaintInfo::getUserId,
                    ZqztCustMaintInfo::getSvcNum,
                    (existing, replacement) -> existing
                ));
        }

        // 处理每个数据项
        List<ZqztGrpContractOtherInfo> processedDataList = new ArrayList<>();
        for (ZqztGrpContractOtherInfo item : dataList) {
            ZqztGrpContractOtherInfo processedItem = new ZqztGrpContractOtherInfo();
            processedItem.setContractId(item.getContractId());
            processedItem.setInfoTag(item.getInfoTag());
            processedItem.setInfoVal(item.getInfoVal());
            processedItem.setCreateTime(item.getCreateTime());

            // 如果不是 GRP_CONTRACT_USER_LIST 类型，直接设置状态为 1（有效）
            if (!"GRP_CONTRACT_USER_LIST".equals(item.getInfoTag())) {
                processedItem.setIsValid("1");
                processedItem.setInfoVal2(item.getInfoVal2());
            }else {
                // 如果是GRP_CONTRACT_USER_LIST 类型,执行入库逻辑判断
                // 获取user_id并验证
                String userId = item.getInfoVal();
                if (userId != null && svcNumMap.containsKey(userId) && svcNumMap.get(userId) != null) {
                    processedItem.setIsValid("1");
                    processedItem.setInfoVal2(svcNumMap.get(userId));
                } else {
                    // 用户不存在或无效，入库时状态默认=99（迁移失效）
                    processedItem.setIsValid("99");
                    // 保留原始服务号码
                    processedItem.setInfoVal2(item.getInfoVal2());
                }
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
        lqw.eq(ZqztGrpContractOtherInfo::getIsValid, "1");
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
    @Override
    public TableDataInfo<ZqztGrpContractOtherInfoVo> selectContractRelatedUserInfoPage(String contractId, String infoVal2, PageQuery pageQuery) {
        // 构建分页对象
        Page<ZqztGrpContractOtherInfoVo> page = pageQuery.build();

        // 调用Mapper的分页查询方法
        Page<ZqztGrpContractOtherInfoVo> resultPage = baseMapper.selectContractRelatedUserInfoPage(page, contractId, infoVal2);

        // 对查询结果进行后处理（加密infoVal2等）
        List<ZqztGrpContractOtherInfoVo> records = resultPage.getRecords();
        records.stream().map(vo -> {
            vo.setInfoVal2(encryptionUtil.encryptData(vo.getInfoVal2()));
            return vo;
        });
        // 返回分页结果
        return TableDataInfo.build(resultPage);
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

    /**
     * 批量使合同关联用户信息失效
     *
     * @param ids 合同关联信息ID列表
     * @return 操作结果
     */
    @Override
    public Boolean batchInvalidateContractUserRelation(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // 创建更新条件
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ZqztGrpContractOtherInfo::getId, ids);

        // 创建更新对象，将isValid设置为"0"
        ZqztGrpContractOtherInfo updateEntity = new ZqztGrpContractOtherInfo();
        updateEntity.setIsValid("0");

        // 执行更新操作
        int updateCount = baseMapper.update(updateEntity, queryWrapper);
        return updateCount > 0;
    }


}
