package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.Desensitization;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.grp.TfFCustGroupMDo;
import com.crm.system.mapper.CustMaintInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import com.crm.common.enums.InfoTagEnum;
import com.crm.common.helper.LoginHelper;

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

        // 对请求参数中的infoVal2字段进行解密处理，并设置固定的info_tag
        for (ZqztGrpContractOtherInfoBo bo : boList) {
            // 设置固定的info_tag值
            bo.setInfoTag(InfoTagEnum.GRP_CONTRACT_USER_LIST.getValue());

            if (bo.getInfoVal2() != null && !bo.getInfoVal2().isEmpty()) {
                try {
                    // 解密infoVal2字段
                    String decryptedInfoVal2 = decryptInfoVal2(bo.getInfoVal2());
                    bo.setInfoVal2(decryptedInfoVal2);
                } catch (Exception e) {
                    log.error("解密infoVal2失败: {}", bo.getInfoVal2(), e);
                    throw new RuntimeException("参数解密失败: " + e.getMessage());
                }
            }
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
                processedItem.setIsValid("1");
                processedItem.setInfoVal2(svcNumMap.get(userId));
            } else {
                // 用户不存在或无效，入库时状态默认=99（迁移失效）
                processedItem.setIsValid("99");
                // 保留原始服务号码
                processedItem.setInfoVal2(item.getInfoVal2());
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
    public List<ZqztGrpContractOtherInfoVo> selectContractRelatedUserInfo(String contractId, String infoVal2) {
        // 第一步：根据合同ID和服务号码查询集团合同扩展信息
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ZqztGrpContractOtherInfo::getContractId, contractId);
        lqw.eq(ZqztGrpContractOtherInfo::getIsValid, "1"); // 只查询有效的记录
        lqw.like(infoVal2 != null && !infoVal2.isEmpty(), ZqztGrpContractOtherInfo::getInfoVal2, infoVal2);

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
        // INNER JOIN逻辑：只返回在两个表中都有匹配的数据
        // 如果任何一个contractOtherInfo在custMaintInfo中没有匹配，则整条记录都不返回
        List<ZqztGrpContractOtherInfoVo> result = new ArrayList<>();

        for (ZqztGrpContractOtherInfoVo contractOtherInfo : contractOtherInfos) {
            String userId = contractOtherInfo.getInfoVal();
            // INNER JOIN条件：必须在客户维护信息中找到匹配的记录
            if (userId != null && custMaintInfoMap.containsKey(userId) && custMaintInfoMap.get(userId) != null) {
                // 对infoVal2进行加密后返回给前端
                String originalInfoVal2 = contractOtherInfo.getInfoVal2();
                if (originalInfoVal2 != null && !originalInfoVal2.isEmpty()) {
                    String encryptedInfoVal2 = encryptInfoVal2(originalInfoVal2);
                    contractOtherInfo.setInfoVal2(encryptedInfoVal2);
                }

                // 添加从客户维护信息查询到的字段
                Map<String, Object> custInfo = custMaintInfoMap.get(userId);

                // 根据权限判断是否脱敏处理手机号码
                String svcNum = (String) custInfo.get("svcNum");
                contractOtherInfo.setSvcNum(svcNum);
                contractOtherInfo.setUserId(userId);
                contractOtherInfo.setCustName((String) custInfo.get("custName"));
                contractOtherInfo.setCustGrpName((String) custInfo.get("custGrpName"));

                result.add(contractOtherInfo);
            }
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


    // 查询未绑定合同的用户列表
    @Override
    public List<ZqztGrpContractOtherInfoVo> selectUncontractedCustomersByHqGrpCode(String hqGrpCode, String contractId) {
        // 1. 根据总部集团编码(hqGrpCode)在tf_f_cust_group_m表中查询GROUP_ID，获取CUST_ID
        TfFCustGroupMDo custGroup = tfFCustGroupMService.selectByGroupId(hqGrpCode);
        String custId = null;
        if (custGroup != null && custGroup.getCustId() != null && !custGroup.getCustId().isEmpty()) {
            custId = custGroup.getCustId();
        }

        // 2. 分别执行两个SQL查询并合并结果（去重处理）
        // 使用Set基于userId去重，避免同一个用户在两个查询条件中都出现导致重复
        Set<String> processedUserIds = new HashSet<>();
        List<CustMaintInfo> allCustMaintInfosFromDb = new ArrayList<>();

        // SQL 1: SELECT user_id,svc_num,type_lvl2_name FROM IOB_GOV2.CUST_MAINT_INFO WHERE (STATUS = '1' AND CUST_ID ='xxx')
        if (custId != null && !custId.isEmpty()) {
            QueryWrapper<CustMaintInfo> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.select("USER_ID", "SVC_NUM", "TYPE_LVL2_NAME")
                .eq("STATUS", "1")
                .eq("CUST_ID", custId);
            List<CustMaintInfo> records1 = SpringUtils.getBean(CustMaintInfoMapper.class).selectList(queryWrapper1);
            // 过滤并添加不重复的记录
            for (CustMaintInfo record : records1) {
                if (record.getUserId() != null && !processedUserIds.contains(record.getUserId())) {
                    allCustMaintInfosFromDb.add(record);
                    processedUserIds.add(record.getUserId());
                }
            }
        }

        // SQL 2: SELECT user_id,svc_num,type_lvl2_name FROM IOB_GOV2.CUST_MAINT_INFO WHERE (STATUS = '1' AND CUST_GRP_ID = 'xxx')
        QueryWrapper<CustMaintInfo> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.select("USER_ID", "SVC_NUM", "TYPE_LVL2_NAME")
            .eq("STATUS", "1")
            .eq("CUST_GRP_ID", hqGrpCode);
        List<CustMaintInfo> records2 = SpringUtils.getBean(CustMaintInfoMapper.class).selectList(queryWrapper2);
        // 过滤并添加不重复的记录
        for (CustMaintInfo record : records2) {
            if (record.getUserId() != null && !processedUserIds.contains(record.getUserId())) {
                allCustMaintInfosFromDb.add(record);
                processedUserIds.add(record.getUserId());
            }
        }

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

        // 判断当前用户是否需要脱敏（仿照集团客户管理页面的逻辑）
        boolean needDesensitize = !LoginHelper.isAdmin();

        // 4. 过滤掉已经有合同的用户，只返回未签约的用户
        List<ZqztGrpContractOtherInfoVo> unContractedUsers = allCustMaintInfosFromDb.stream()
            .filter(custMaintInfo -> {
                String userId = custMaintInfo.getUserId();
                return userId != null && !contractedUserIds.contains(userId);
            })
            .map(custMaintInfo -> {
                ZqztGrpContractOtherInfoVo user = new ZqztGrpContractOtherInfoVo();
                String svcNum = custMaintInfo.getSvcNum();

                // 对SVC_NUM进行加密处理
                String encryptedSvcNum = encryptSvcNum(svcNum);
                user.setEncryptedSvcNum(encryptedSvcNum);
                user.setUserId(custMaintInfo.getUserId());
                user.setTypeLvl2Name(custMaintInfo.getTypeLvl2Name());

                return user;
            })
            .collect(Collectors.toList());

        return unContractedUsers;
    }

    /**
     * 对SVC_NUM进行简单混淆处理（避免加密库兼容性问题）
     * @param svcNum 原始服务号码
     * @return 混淆后的字符串
     */
    private String encryptSvcNum(String svcNum) {
        if (svcNum == null || svcNum.isEmpty()) {
            return "";
        }

        try {
            // 使用简单的Base64编码+字符替换作为混淆处理
            String base64Encoded = java.util.Base64.getEncoder().encodeToString(svcNum.getBytes("UTF-8"));
            // 简单的字符替换混淆
            return base64Encoded.replace('=', '_').replace('+', '-').replace('/', '_');
        } catch (Exception e) {
            log.error("SVC_NUM混淆处理失败: {}", svcNum, e);
            return ""; // 处理失败返回空字符串
        }
    }

    /**
     * 加密infoVal2字段
     * @param infoVal2 原始的信息值2
     * @return 加密后的字符串
     */
    private String encryptInfoVal2(String infoVal2) {
        if (infoVal2 == null || infoVal2.isEmpty()) {
            return "";
        }

        try {
            // 使用Base64编码+字符替换作为混淆处理
            String base64Encoded = java.util.Base64.getEncoder().encodeToString(infoVal2.getBytes("UTF-8"));
            // 简单的字符替换混淆
            return base64Encoded.replace('=', '_').replace('+', '-').replace('/', '_');
        } catch (Exception e) {
            log.error("infoVal2加密失败: {}", infoVal2, e);
            return ""; // 处理失败返回空字符串
        }
    }

    /**
     * 解密加密的服务号码
     * @param encryptedSvcNum 加密的服务号码
     * @return 解密后的原始服务号码
     */
    @Override
    public String decryptSvcNum(String encryptedSvcNum) {
        return decryptInfoVal2(encryptedSvcNum);
    }

    /**
     * 解密infoVal2字段（用于批量新增接口）
     * @param encryptedInfoVal2 加密的信息值2
     * @return 解密后的原始字符串
     */
    private String decryptInfoVal2(String encryptedInfoVal2) {
        if (encryptedInfoVal2 == null || encryptedInfoVal2.isEmpty()) {
            return "";
        }

        try {
            // 正确的字符还原逻辑：将混淆字符换回原始Base64字符
            // _ -> =, - -> +, / -> _ (然后再将_ -> /)
            String restored = encryptedInfoVal2.replace('_', '=').replace('-', '+').replace('/', '_');
            // 再将'_'换回'/'
            restored = restored.replace('_', '/');

            // Base64解码
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(restored);
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            log.error("infoVal2解密失败: {}", encryptedInfoVal2, e);
            throw new RuntimeException("解密失败: " + e.getMessage());
        }
    }

    // 查询未绑定合同的用户列表（用于后端批量处理，不脱敏）
    @Override
    public List<Map<String, Object>> selectUncontractedCustomersByHqGrpCodeForBatch(String hqGrpCode, String contractId) {
        // 1. 根据总部集团编码(hqGrpCode)在tf_f_cust_group_m表中查询GROUP_ID，获取CUST_ID
        TfFCustGroupMDo custGroup = tfFCustGroupMService.selectByGroupId(hqGrpCode);
        String custId = null;
        if (custGroup != null && custGroup.getCustId() != null && !custGroup.getCustId().isEmpty()) {
            custId = custGroup.getCustId();
        }

        // 2. 分别执行两个SQL查询并合并结果（去重处理）
        // 使用Set基于userId去重，避免同一个用户在两个查询条件中都出现导致重复
        Set<String> processedUserIds = new HashSet<>();
        List<CustMaintInfo> allCustMaintInfosFromDb = new ArrayList<>();

        // SQL 1: SELECT user_id,svc_num,type_lvl2_name FROM IOB_GOV2.CUST_MAINT_INFO WHERE (STATUS = '1' AND CUST_ID ='xxx')
        if (custId != null && !custId.isEmpty()) {
            QueryWrapper<CustMaintInfo> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.select("USER_ID", "SVC_NUM", "TYPE_LVL2_NAME")
                .eq("STATUS", "1")
                .eq("CUST_ID", custId);
            List<CustMaintInfo> records1 = SpringUtils.getBean(CustMaintInfoMapper.class).selectList(queryWrapper1);
            // 过滤并添加不重复的记录
            for (CustMaintInfo record : records1) {
                if (record.getUserId() != null && !processedUserIds.contains(record.getUserId())) {
                    allCustMaintInfosFromDb.add(record);
                    processedUserIds.add(record.getUserId());
                }
            }
        }

        // SQL 2: SELECT user_id,svc_num,type_lvl2_name FROM IOB_GOV2.CUST_MAINT_INFO WHERE (STATUS = '1' AND CUST_GRP_ID = 'xxx')
        QueryWrapper<CustMaintInfo> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.select("USER_ID", "SVC_NUM", "TYPE_LVL2_NAME")
            .eq("STATUS", "1")
            .eq("CUST_GRP_ID", hqGrpCode);
        List<CustMaintInfo> records2 = SpringUtils.getBean(CustMaintInfoMapper.class).selectList(queryWrapper2);
        // 过滤并添加不重复的记录
        for (CustMaintInfo record : records2) {
            if (record.getUserId() != null && !processedUserIds.contains(record.getUserId())) {
                allCustMaintInfosFromDb.add(record);
                processedUserIds.add(record.getUserId());
            }
        }

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

        // 4. 过滤掉已经有合同的用户，只返回未签约的用户（不进行脱敏处理）
        List<Map<String, Object>> uncontractedUsers = allCustMaintInfosFromDb.stream()
            .filter(custMaintInfo -> {
                String userId = custMaintInfo.getUserId();
                return userId != null && !contractedUserIds.contains(userId);
            })
            .map(custMaintInfo -> {
                Map<String, Object> userMap = new HashMap<>();
                String svcNum = custMaintInfo.getSvcNum();

                // 直接返回原始服务号码，不进行脱敏
                userMap.put("svcNum", svcNum); // 原始号码

                // 对SVC_NUM进行加密处理（用于后续匹配）
                String encryptedSvcNum = encryptSvcNum(svcNum);
                userMap.put("encryptedSvcNum", encryptedSvcNum); // 加密后的号码

                userMap.put("userId", custMaintInfo.getUserId()); // 用户编号
                userMap.put("typeLvl2Name", custMaintInfo.getTypeLvl2Name()); // 业务类型
                return userMap;
            })
            .collect(Collectors.toList());

        return uncontractedUsers;
    }
}
