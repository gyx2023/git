package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.enums.ContractStatusEnum;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.spring.SpringUtils;
import com.crm.system.domain.dao.ZqztGrpContractInfo;
import com.crm.system.domain.dao.ZqztGrpContractInfoLog;
import com.crm.system.domain.dao.ZqztGrpContractOtherInfo;
import com.crm.system.domain.dao.ZqztGrpContractOtherInfoLog;
import com.crm.system.domain.bo.ZqztGrpContractInfoBo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ZqztGrpContractInfoVo;
import com.crm.system.mapper.ZqztGrpContractInfoLogMapper;
import com.crm.system.mapper.ZqztGrpContractInfoMapper;
import com.crm.system.mapper.ZqztGrpContractOtherInfoLogMapper;
import com.crm.system.mapper.ZqztGrpContractOtherInfoMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.service.IZqztGrpContractInfoService;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 集团合同信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpContractInfoServiceImpl implements IZqztGrpContractInfoService {

    private final ZqztGrpContractInfoMapper baseMapper;

    @Autowired
    private SysParamMapper sysParamMapper;

    @Autowired
    private IZqztGrpContractOtherInfoService zqztGrpContractOtherInfoService;

    /**
     * 查询集团合同信息
     */
    @Override
    public ZqztGrpContractInfoVo queryById(String contractId){
        // 构建查询条件，排除状态为7（作废）的合同
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ZqztGrpContractInfo::getContractId, contractId);
        lqw.ne(ZqztGrpContractInfo::getContractStatus, ContractStatusEnum.CANCELLED_FINAL.getCode()); // 排除作废的合同

        ZqztGrpContractInfoVo vo = baseMapper.selectVoOne(lqw);

        return vo;
    }

    /**
     * 查询集团合同信息列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractInfoVo> queryPageList(ZqztGrpContractInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = buildQueryWrapper(bo);
        // 按创建时间倒序排列
        lqw.orderByDesc(ZqztGrpContractInfo::getCreateTime);
        Page<ZqztGrpContractInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);

        List<ZqztGrpContractInfoVo> normalList = result.getRecords().stream()
            .map(this::addBusinessTypesToVo)
            .collect(Collectors.toList());
        result.setRecords(normalList);

        return TableDataInfo.build(result);
    }

    /**
     * 批量导入集团合同信息
     */
    @Override
    public int importAll(List<ZqztGrpContractInfo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        baseMapper.insertBatch(dataList);

        return dataList.size();
    }

    @Override
    public int importAllClear(List<ZqztGrpContractInfo> dataList) {
        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        return dataList.size();
    }

    /**
     * 查询集团合同信息列表
     */
    @Override
    public List<ZqztGrpContractInfoVo> queryList(ZqztGrpContractInfoBo bo) {
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = buildQueryWrapper(bo);
        List<ZqztGrpContractInfoVo> list = baseMapper.selectVoList(lqw);

        return list.stream()
            .map(this::addBusinessTypesToVo)
            .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<ZqztGrpContractInfo> buildQueryWrapper(ZqztGrpContractInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGrpId() != null, ZqztGrpContractInfo::getGrpId, bo.getGrpId());
        lqw.eq(StringUtils.isNotBlank(bo.getErpContractId()), ZqztGrpContractInfo::getErpContractId, bo.getErpContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInuseContractId()), ZqztGrpContractInfo::getInuseContractId, bo.getInuseContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ZqztGrpContractInfo::getContractType, bo.getContractType());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ZqztGrpContractInfo::getContractName, bo.getContractName());
        lqw.eq(bo.getContractEffdate() != null, ZqztGrpContractInfo::getContractEffdate, bo.getContractEffdate());
        lqw.eq(bo.getContractExpdate() != null, ZqztGrpContractInfo::getContractExpdate, bo.getContractExpdate());
        lqw.eq(StringUtils.isNotBlank(bo.getPaytype()), ZqztGrpContractInfo::getPaytype, bo.getPaytype());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNote()), ZqztGrpContractInfo::getContractNote, bo.getContractNote());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ZqztGrpContractInfo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getFiles()), ZqztGrpContractInfo::getFiles, bo.getFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getContractProperty()), ZqztGrpContractInfo::getContractProperty, bo.getContractProperty());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBidding()), ZqztGrpContractInfo::getIsBidding, bo.getIsBidding());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMain()), ZqztGrpContractInfo::getContractMain, bo.getContractMain());
        lqw.eq(StringUtils.isNotBlank(bo.getContractParty()), ZqztGrpContractInfo::getContractParty, bo.getContractParty());
        lqw.eq(StringUtils.isNotBlank(bo.getContractDept()), ZqztGrpContractInfo::getContractDept, bo.getContractDept());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMan()), ZqztGrpContractInfo::getContractMan, bo.getContractMan());
        lqw.eq(bo.getContractMoney2() != null, ZqztGrpContractInfo::getContractMoney2, bo.getContractMoney2());
        lqw.eq(bo.getIsFix() != null, ZqztGrpContractInfo::getIsFix, bo.getIsFix());
        lqw.eq(bo.getAuditDate() != null, ZqztGrpContractInfo::getAuditDate, bo.getAuditDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractPhone()), ZqztGrpContractInfo::getContractPhone, bo.getContractPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getContractOldnum()), ZqztGrpContractInfo::getContractOldnum, bo.getContractOldnum());
        lqw.eq(bo.getYearIncome() != null, ZqztGrpContractInfo::getYearIncome, bo.getYearIncome());
        lqw.eq(bo.getServAmount() != null, ZqztGrpContractInfo::getServAmount, bo.getServAmount());
        lqw.eq(bo.getContractMoney() != null, ZqztGrpContractInfo::getContractMoney, bo.getContractMoney());
        lqw.eq(bo.getContractSigndate() != null, ZqztGrpContractInfo::getContractSigndate, bo.getContractSigndate());
        // 如果用户没有特别指定合同状态为7（作废），则排除作废的合同
        if (StringUtils.isBlank(bo.getContractStatus()) || !ContractStatusEnum.CANCELLED_FINAL.getCode().equals(bo.getContractStatus())) {
            lqw.ne(ZqztGrpContractInfo::getContractStatus, ContractStatusEnum.CANCELLED_FINAL.getCode()); // 排除状态为7（作废）的合同
        } else {
            lqw.eq(ZqztGrpContractInfo::getContractStatus, bo.getContractStatus());
        }
        lqw.eq(bo.getContractCateL1Code() != null, ZqztGrpContractInfo::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL1Name()), ZqztGrpContractInfo::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(bo.getContractCateL2Code() != null, ZqztGrpContractInfo::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL2Name()), ZqztGrpContractInfo::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(bo.getContractCateL3Code() != null, ZqztGrpContractInfo::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL3Name()), ZqztGrpContractInfo::getContractCateL3Name, bo.getContractCateL3Name());
        return lqw;
    }

    /**
     * 新增集团合同信息
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractInfoBo bo) {
        ZqztGrpContractInfo add = BeanUtil.toBean(bo, ZqztGrpContractInfo.class);

        // 自动设置创建人为当前登录用户
        if (add.getCreator() == null || add.getCreator().isEmpty()) {
            add.setCreator(LoginHelper.getNickName());
        }

        if (add.getInuseContractId() == null || add.getInuseContractId().isEmpty()) {
            add.setInuseContractId(generateRandomContractId());
        }

        // 添加 paytype 默认值
        if (add.getPaytype() == null || add.getPaytype().isEmpty()) {
            add.setPaytype("17");
        }

        // 设置默认合同状态，如果未提供
        if (add.getContractStatus() == null || add.getContractStatus().isEmpty()) {
            add.setContractStatus(ContractStatusEnum.IN_PROGRESS.getCode()); // 默认设置为"履行中"
        }

        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());

            // 处理普通合同业务类型，如果有业务类型则存储到扩展表
            if (bo.getBusinessTypes() != null && !bo.getBusinessTypes().trim().isEmpty()) {
                handleContractBusinessTypes(add.getContractId(), bo.getBusinessTypes());
            }
        }
        return flag;
    }

    // 生成15位随机数,且唯一
    public String generateRandomContractId(){
        String inuseContractId;
        int maxAttempts = 10; // 重试次数

        for (int i = 0; i < maxAttempts; i++) {
            // 生成时间戳部分(10位): 年月日时分秒毫秒的后10位
            String timestamp = new java.text.SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
            String timePart = timestamp.substring(Math.max(0, timestamp.length() - 10));

            // 生成随机数部分(5位)
            java.util.Random random = new java.util.Random();
            String randomPart = String.format("%05d", random.nextInt(100000));

            // 组合生成15位合同编号
            inuseContractId = timePart + randomPart;

            // 检查数据库中是否已存在该合同编号
            LambdaQueryWrapper<ZqztGrpContractInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ZqztGrpContractInfo::getInuseContractId, inuseContractId);

            if (baseMapper.selectCount(queryWrapper) == 0) {
                System.out.println("随机InuseContractId:" + inuseContractId);
                return inuseContractId; // 唯一性校验通过，返回合同编号
            }
        }
        throw new RuntimeException("无法生成唯一的15位合同编号，请稍后重试");
    }

    /**
     * 修改集团合同信息
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractInfoBo bo) {
        // 先将原有的扩展信息移动到日志表
        if (bo.getContractId() != null) {
            // 查询现有的扩展信息记录
            LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ZqztGrpContractOtherInfo::getContractId, bo.getContractId());
            List<ZqztGrpContractOtherInfo> existingRecords = SpringUtils.getBean(ZqztGrpContractOtherInfoMapper.class).selectList(queryWrapper);

            // 将现有记录备份到日志表
            if (!existingRecords.isEmpty()) {
                for (ZqztGrpContractOtherInfo record : existingRecords) {
                    ZqztGrpContractOtherInfoLog logRecord = BeanUtil.toBean(record, ZqztGrpContractOtherInfoLog.class);
                    // 设置日志记录的ID为null，以便自动生成新ID
                    logRecord.setId(null);
                    SpringUtils.getBean(ZqztGrpContractOtherInfoLogMapper.class).insert(logRecord);
                }
            }

            // 查询现有的合同主表记录并备份到日志表
            ZqztGrpContractInfo existingRecord = baseMapper.selectById(bo.getContractId());
            if (existingRecord != null) {
                ZqztGrpContractInfoLog logRecord = BeanUtil.toBean(existingRecord, ZqztGrpContractInfoLog.class);
                // 设置日志记录的ID为null，以便自动生成新ID
                //logRecord.setId(null);
                SpringUtils.getBean(ZqztGrpContractInfoLogMapper.class).insert(logRecord);
            }
        }

        ZqztGrpContractInfo update = BeanUtil.toBean(bo, ZqztGrpContractInfo.class);

        // 在编辑时，如果creator字段为空，则自动设置为当前登录用户
        if (update.getCreator() == null || update.getCreator().isEmpty()) {
            update.setCreator(LoginHelper.getNickName());
        }

        boolean result = baseMapper.updateById(update) > 0;

        if (result) {
            // 处理普通合同业务类型，如果有业务类型则存储到扩展表
            if (bo.getBusinessTypes() != null && !bo.getBusinessTypes().trim().isEmpty()) {
                handleContractBusinessTypes(update.getContractId(), bo.getBusinessTypes());
            }
        }

        return result;
    }
    /**
     * 批量删除集团合同信息（逻辑删除，将合同状态改为7作废）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        // 获取当前登录用户
        String currentUser = LoginHelper.getLoginUser().getUsername();

        for (String contractId : ids) {
            // 查询现有记录并备份到日志表
            ZqztGrpContractInfo existingRecord = baseMapper.selectById(contractId);
            if (existingRecord != null) {
                // 备份到日志表
                ZqztGrpContractInfoLog logRecord = BeanUtil.toBean(existingRecord, ZqztGrpContractInfoLog.class);
                SpringUtils.getBean(ZqztGrpContractInfoLogMapper.class).insert(logRecord);
            }

            // 查询并备份扩展表中与该合同ID关联的记录到日志表
            LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ZqztGrpContractOtherInfo::getContractId, contractId);
            List<ZqztGrpContractOtherInfo> existingOtherRecords =
                SpringUtils.getBean(ZqztGrpContractOtherInfoMapper.class).selectList(queryWrapper);

            // 将扩展表的现有记录备份到日志表
            for (ZqztGrpContractOtherInfo otherRecord : existingOtherRecords) {
                ZqztGrpContractOtherInfoLog logRecord = BeanUtil.toBean(otherRecord, ZqztGrpContractOtherInfoLog.class);
                logRecord.setId(null); // 设置日志记录的ID为null，以便自动生成新ID
                SpringUtils.getBean(ZqztGrpContractOtherInfoLogMapper.class).insert(logRecord);
            }

            // 删除扩展表中与该合同ID关联的记录
            SpringUtils.getBean(ZqztGrpContractOtherInfoMapper.class).delete(queryWrapper);

            // 更新合同状态为7（作废）
            ZqztGrpContractInfo updateRecord = new ZqztGrpContractInfo();
            updateRecord.setContractId(contractId);
            updateRecord.setContractStatus(ContractStatusEnum.CANCELLED_FINAL.getCode()); // 7代表作废
            updateRecord.setUpdateBy(currentUser); // 记录操作人
            updateRecord.setUpdateTime(new Date()); // 记录操作时间

            // 执行更新操作
            baseMapper.updateById(updateRecord);
        }

        return true; // 返回成功
    }

    /**
     * 全量导入集团合同信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllGroupContractInfo(List<ZqztGrpContractInfo> dataList) {
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
     * 查询所有集团合同信息
     *
     * @return 集团合同信息列表
     */
    @Override
    public List<ZqztGrpContractInfo> selectAllZqztGrpContractInfo() {
        // 排除状态为7（作废）的合同
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = Wrappers.lambdaQuery();
        lqw.ne(ZqztGrpContractInfo::getContractStatus, ContractStatusEnum.CANCELLED_FINAL.getCode());
        return baseMapper.selectList(lqw);
    }

    /**
     * 处理合同业务类型，将业务类型存储到扩展表中
     *
     * @param contractId 合同ID
     * @param businessTypesStr 业务类型字符串，多个类型以逗号分隔
     */
    private void handleContractBusinessTypes(String contractId, String businessTypesStr) {
        if (contractId == null || contractId.trim().isEmpty() || businessTypesStr == null || businessTypesStr.trim().isEmpty()) {
            return;
        }

        // 删除该合同之前的业务类型记录
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ZqztGrpContractOtherInfo::getContractId, contractId);

        ZqztGrpContractOtherInfoMapper otherInfoMapper =
            (ZqztGrpContractOtherInfoMapper) SpringUtils.getBean(ZqztGrpContractOtherInfoMapper.class);
        otherInfoMapper.delete(deleteWrapper);

        // 解析业务类型并逐条插入扩展表
        String[] businessTypes = businessTypesStr.split(",");
        for (String businessType : businessTypes) {
            if (businessType != null && !businessType.trim().isEmpty()) {
                ZqztGrpContractOtherInfoBo otherInfoBo = new ZqztGrpContractOtherInfoBo();
                otherInfoBo.setContractId(contractId);
                otherInfoBo.setInfoTag("GRP_CONTRACT_BUSI_TYPE");
                otherInfoBo.setInfoVal(businessType.trim());
                // 默认有效
                otherInfoBo.setIsValid("1");

                zqztGrpContractOtherInfoService.insertByBo(otherInfoBo);
            }
        }
    }

    /**
     * 根据合同ID获取业务类型列表
     *
     * @param contractId 合同ID
     * @return 业务类型字符串，多个类型以逗号分隔
     */
    private String getBusinessTypesByContractId(String contractId) {
        if (contractId == null || contractId.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<ZqztGrpContractOtherInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ZqztGrpContractOtherInfo::getContractId, contractId)
                   .eq(ZqztGrpContractOtherInfo::getInfoTag, "GRP_CONTRACT_BUSI_TYPE")
                   .eq(ZqztGrpContractOtherInfo::getIsValid, "1");

        List<ZqztGrpContractOtherInfo> list = SpringUtils.getBean(ZqztGrpContractOtherInfoMapper.class).selectList(queryWrapper);

        if (list == null || list.isEmpty()) {
            return null;
        }

        // 将所有业务类型值用逗号连接
        return list.stream()
                  .map(ZqztGrpContractOtherInfo::getInfoVal) // 从infoVal获取业务类型代码
                  .filter(infoVal -> infoVal != null && !infoVal.trim().isEmpty())
                  .collect(Collectors.joining(","));
    }

    /**
     * 为VO对象添加业务类型信息
     *
     * @param vo VO对象
     * @return 添加了业务类型信息的VO对象
     */
    private ZqztGrpContractInfoVo addBusinessTypesToVo(ZqztGrpContractInfoVo vo) {
        if (vo != null && vo.getContractId() != null) {
            String businessTypes = getBusinessTypesByContractId(vo.getContractId());
            vo.setBusinessTypes(businessTypes);
        }
        return vo;
    }

    @Override
    public Boolean contractCanAssociateUser(String contractId) {
        if (contractId == null || contractId.trim().isEmpty()) {
            return false;
        }

        // 查询合同信息
        LambdaQueryWrapper<ZqztGrpContractInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ZqztGrpContractInfo::getContractId, contractId);

        ZqztGrpContractInfo contractInfo = baseMapper.selectOne(queryWrapper);

        if (contractInfo == null) {
            return false;
        }

        // 判断合同状态是否为履行中（字符9）或办结（字符5）
        String contractStatus = contractInfo.getContractStatus();
        boolean isStatusValid = ContractStatusEnum.IN_PROGRESS.getCode().equals(contractStatus) || ContractStatusEnum.COMPLETED.getCode().equals(contractStatus);

        // 获取合同失效时间
        Date contractExpdate = contractInfo.getContractExpdate();

        if (contractExpdate == null) {
            return false; // 如果没有设置失效时间，则认为不能关联用户
        }

        // 比较合同失效时间是否大于当前时间
        Date currentTime = new Date();
        boolean isNotExpired = contractExpdate.after(currentTime);

        // 合同状态为履行中9或办结5 且 合同失效时间大于当前时间，才能关联用户
        return isStatusValid && isNotExpired;
    }
}
