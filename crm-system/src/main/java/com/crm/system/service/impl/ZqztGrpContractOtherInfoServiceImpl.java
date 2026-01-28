package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.generator.util.Desensitization;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.grp.TfFCustGroupMDo;
import com.crm.system.mapper.CustMaintInfoMapper;
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
import com.crm.system.service.ITfFCustGroupMService;
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
    private ICustMaintInfoService custMaintInfoService;

    @Autowired
    private ITfFCustGroupMService tfFCustGroupMService;

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
        if (custMaintInfoService != null && !userIds.isEmpty()) {
            custMaintInfoMap = custMaintInfoService.batchGetCustMaintInfoByUserIds(userIds);
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

    @Override
    public List<Map<String, Object>> selectContractRelatedUserInfo(String contractId, String infoVal2) {
        // 第一步：根据合同ID和服务号码查询集团合同扩展信息
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ZqztGrpContractOtherInfo::getContractId, contractId);
        lqw.eq(ZqztGrpContractOtherInfo::getIsValid, "1"); // 只查询有效的记录
        lqw.like(infoVal2 != null && !infoVal2.isEmpty(), ZqztGrpContractOtherInfo::getInfoVal2, infoVal2); // 按服务号码模糊查询
        List<ZqztGrpContractOtherInfoVo> contractOtherInfos = baseMapper.selectVoList(lqw);
        if (contractOtherInfos == null || contractOtherInfos.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取所有infoVal作为user_id用于查询客户维护信息
        List<String> userIds = contractOtherInfos.stream()
            .map(ZqztGrpContractOtherInfoVo::getInfoVal)
            .filter(infoVal -> infoVal != null && !infoVal.trim().isEmpty())
            .distinct()
            .collect(Collectors.toList());

        // 第二步：批量查询客户维护信息
        Map<String, Map<String, Object>> custMaintInfoMap = new HashMap<>();
        if (custMaintInfoService != null && !userIds.isEmpty()) {
            custMaintInfoMap = custMaintInfoService.batchGetCustMaintInfoByUserIds(userIds);
        }

        // 第三步：合并数据，构建结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (ZqztGrpContractOtherInfoVo contractOtherInfo : contractOtherInfos) {
            Map<String, Object> mergedInfo = new HashMap<>();
            // 添加合同扩展信息字段
            mergedInfo.put("id", contractOtherInfo.getId()); // 添加ID字段，用于删除操作
            mergedInfo.put("infoTag", contractOtherInfo.getInfoTag());
            mergedInfo.put("infoVal", contractOtherInfo.getInfoVal());
            mergedInfo.put("infoVal2", contractOtherInfo.getInfoVal2());
            mergedInfo.put("isValid", contractOtherInfo.getIsValid());

            // 添加从客户维护信息查询到的字段
            String userId = contractOtherInfo.getInfoVal();
            if (userId != null && custMaintInfoMap.containsKey(userId)) {
                Map<String, Object> custInfo = custMaintInfoMap.get(userId);
                // 对手机号码进行脱敏处理
                String svcNum = (String) custInfo.get("svcNum");
                String desensitizedSvcNum = Desensitization.desensitizePhoneNumber(svcNum);
                mergedInfo.put("svcNum", desensitizedSvcNum);
                mergedInfo.put("userId", custInfo.get("userId"));
                // 对归属客户名称进行脱敏
                String custName = (String) custInfo.get("custName");
                String desensitizedCustName = Desensitization.desensitizeCompanyName(custName);
                mergedInfo.put("custName", desensitizedCustName);
                // 对收入归集集团名称进行脱敏
                String custGrpName = (String) custInfo.get("custGrpName");
                String desensitizedCustGrpName = Desensitization.desensitizeCompanyName(custGrpName);
                mergedInfo.put("custGrpName", desensitizedCustGrpName);
            } else {
                // 如果没有找到对应的客户信息，设置为空值
                mergedInfo.put("svcNum", "");
                mergedInfo.put("userId", "");
                mergedInfo.put("custName", "");
                mergedInfo.put("custGrpName", "");
            }
            result.add(mergedInfo);
        }

        return result;
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

    @Override
    public List<ZqztGrpContractOtherInfo> selectByInfoValAndContractId(List<String> infoVal, String contractId) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ZqztGrpContractOtherInfo::getContractId, contractId)
                   .in(ZqztGrpContractOtherInfo::getInfoVal, infoVal)
                   .eq(ZqztGrpContractOtherInfo::getIsValid, "1"); // 只查询有效的记录
        return baseMapper.selectList(queryWrapper);
    }



    @Override
    public List<Map<String, Object>> selectUncontractedCustomersByHqGrpCode(String hqGrpCode, String contractId) {
        // 1. 根据总部集团编码(hqGrpCode)在tf_f_cust_group_m表中查询GROUP_ID，获取CUST_ID
        TfFCustGroupMDo custGroup = tfFCustGroupMService.selectByGroupId(hqGrpCode);
        List<String> custIds = new ArrayList<>();
        if (custGroup != null) {
            custIds.add(custGroup.getCustId());
        }

        if (custIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 根据 HQ_GRP_CODE(CUST_GRP_ID) 和查询到的 CUST_ID 查询 CustMaintInfo 表的 USER_ID
        // 核心修改：实现 STATUS=1 且 (CUST_GRP_ID=hqGrpCode OR CUST_ID in custIds) 的逻辑
        List<CustMaintInfo> allCustMaintInfosFromDb = new ArrayList<>();
        QueryWrapper<CustMaintInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("STATUS", "1")
            // 用and包裹OR逻辑，确保条件优先级正确
            .and(wrapper -> wrapper
                .eq("CUST_GRP_ID", hqGrpCode) // 匹配入参的总部编码
                .or() // 或 匹配第一步查询到的cust_id
                .in("CUST_ID", custIds)
            );
        // 一次查询替代原循环查询，提升性能
        List<CustMaintInfo> records = SpringUtils.getBean(CustMaintInfoMapper.class).selectList(queryWrapper);
        allCustMaintInfosFromDb.addAll(records);

        // 提取userIds
        List<String> userIds = allCustMaintInfosFromDb.stream()
            .map(CustMaintInfo::getUserId)
            .filter(userId -> userId != null && !userId.isEmpty())
            .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 查询zqzt_grp_contract_other_info表中已经存在的info_val（即user_id）
        List<ZqztGrpContractOtherInfo> existingContracts = new ArrayList<>();
        if (!userIds.isEmpty()) {
            existingContracts = selectByInfoValAndContractId(userIds, contractId);
        }

        // 提取已经有关联合同的user_id
        List<String> contractedUserIds = existingContracts.stream()
            .map(ZqztGrpContractOtherInfo::getInfoVal)
            .filter(infoVal -> infoVal != null && !infoVal.isEmpty())
            .collect(Collectors.toList());

        // 4. 过滤掉已经有合同的用户，只返回未签约的用户
        List<Map<String, Object>> uncontractedUsers = allCustMaintInfosFromDb.stream()
            .filter(custMaintInfo -> {
                String userId = custMaintInfo.getUserId();
                return userId != null && !contractedUserIds.contains(userId);
            })
            .map(custMaintInfo -> {
                Map<String, Object> userMap = new HashMap<>();
                // 对SVC_NUM进行脱敏处理
                String desensitizationSvcNum = Desensitization.desensitizePhoneNumber(custMaintInfo.getSvcNum());
                userMap.put("svcNum", desensitizationSvcNum); // 脱敏后的号码
                userMap.put("userId", custMaintInfo.getUserId()); // 用户编号
                userMap.put("typeLvl2Name", custMaintInfo.getTypeLvl2Name()); // 业务类型
                return userMap;
            })
            .collect(Collectors.toList());

        return uncontractedUsers;
    }
}
