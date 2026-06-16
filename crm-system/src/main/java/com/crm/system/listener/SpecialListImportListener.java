package com.crm.system.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListBatchEditVo;
import com.crm.system.domain.vo.SpecialListImportResultVo;
import com.crm.system.domain.vo.SpecialListImportVo;
import com.crm.system.mapper.SpecialListMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 特殊名单导入处理监听器
 *
 * @author crm
 * @date 2026-06-16
 */
@Slf4j
@Component
public class SpecialListImportListener {

    private static final Set<String> VALID_TARGET_TYPES = new HashSet<>(
        Arrays.asList("PHONE", "USER_ID", "CUST_ID", "IP")
    );

    @Resource
    private SpecialListMapper baseMapper;

    /**
     * 处理导入数据（新增）
     *
     * @param dataList 导入数据列表
     * @return 导入结果列表
     */
    public List<SpecialListImportResultVo> processImportData(List<SpecialListImportVo> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        List<SpecialListImportResultVo> resultList = new ArrayList<>();
        List<SpecialListDo> insertList = new ArrayList<>();
        // 用于校验导入数据内的重复
        Set<String> batchKeys = new HashSet<>();

        for (SpecialListImportVo vo : dataList) {
            SpecialListImportResultVo resultVo = new SpecialListImportResultVo();
            // 复制导入字段到结果对象
            BeanUtil.copyProperties(vo, resultVo);

            StringBuilder errorMsg = new StringBuilder();

            // 校验场景编码
            if (StringUtils.isBlank(vo.getSceneCode())) {
                errorMsg.append("场景编码不能为空; ");
            }

            // 校验目标类型
            if (StringUtils.isBlank(vo.getTargetType()) || !VALID_TARGET_TYPES.contains(vo.getTargetType())) {
                errorMsg.append("目标类型无效(需为PHONE/USER_ID/CUST_ID/IP); ");
            }

            // 校验目标值
            if (StringUtils.isBlank(vo.getTargetValue())) {
                errorMsg.append("目标值不能为空; ");
            }

            String batchKey = null;
            if (errorMsg.length() == 0) {
                // 校验导入数据内重复
                batchKey = vo.getSceneCode() + ":" + vo.getTargetType() + ":" + vo.getTargetValue();
                if (batchKeys.contains(batchKey)) {
                    errorMsg.append("导入数据中存在重复记录; ");
                } else {
                    // 校验数据库重复
                    LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
                    lqw.eq(SpecialListDo::getSceneCode, vo.getSceneCode())
                        .eq(SpecialListDo::getTargetType, vo.getTargetType())
                        .eq(SpecialListDo::getTargetValue, vo.getTargetValue());
                    if (baseMapper.selectCount(lqw) > 0) {
                        errorMsg.append("数据库中已存在相同记录; ");
                    }
                }
            }

            if (errorMsg.length() > 0) {
                resultVo.setResult("失败");
                resultVo.setErrorMsg(errorMsg.toString());
            } else {
                // 构建新增对象
                SpecialListDo insertDo = new SpecialListDo();
                insertDo.setSceneCode(vo.getSceneCode());
                insertDo.setTargetType(vo.getTargetType());
                insertDo.setTargetValue(vo.getTargetValue());
                insertDo.setStatus("1");
                insertDo.setEffectiveTime(vo.getEffectiveTime());
                insertDo.setExpireTime(vo.getExpireTime());
                insertDo.setReason(vo.getReason());
                insertDo.setCreateTime(new Date());
                insertDo.setUpdateTime(new Date());
                insertList.add(insertDo);

                batchKeys.add(batchKey);
                resultVo.setResult("成功");
            }

            resultList.add(resultVo);
        }

        // 批量插入有效记录
        if (CollUtil.isNotEmpty(insertList)) {
            baseMapper.insertBatch(insertList, 100);
            log.info("特殊名单导入完成，成功插入 {} 条记录", insertList.size());
        }

        return resultList;
    }

    /**
     * 处理批量编辑数据（修改）
     *
     * @param dataList 批量编辑数据列表
     * @return 处理结果列表
     */
    public List<SpecialListImportResultVo> processBatchEditData(List<SpecialListBatchEditVo> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        List<SpecialListImportResultVo> resultList = new ArrayList<>();
        List<SpecialListDo> updateList = new ArrayList<>();

        for (SpecialListBatchEditVo vo : dataList) {
            SpecialListImportResultVo resultVo = new SpecialListImportResultVo();
            // 复制匹配的导入字段到结果对象
            resultVo.setSceneCode(vo.getSceneCode());
            resultVo.setTargetType(vo.getTargetType());
            resultVo.setTargetValue(vo.getTargetValue());
            resultVo.setEffectiveTime(vo.getEffectiveTime());
            resultVo.setExpireTime(vo.getExpireTime());
            resultVo.setReason(vo.getReason());

            StringBuilder errorMsg = new StringBuilder();

            // 校验场景编码
            if (StringUtils.isBlank(vo.getSceneCode())) {
                errorMsg.append("场景编码不能为空; ");
            }

            // 校验目标类型
            if (StringUtils.isBlank(vo.getTargetType()) || !VALID_TARGET_TYPES.contains(vo.getTargetType())) {
                errorMsg.append("目标类型无效(需为PHONE/USER_ID/CUST_ID/IP); ");
            }

            // 校验目标值
            if (StringUtils.isBlank(vo.getTargetValue())) {
                errorMsg.append("目标值不能为空; ");
            }

            // 校验状态
            if (StringUtils.isBlank(vo.getStatus())) {
                errorMsg.append("状态不能为空; ");
            }

            if (errorMsg.length() == 0) {
                // 查找已存在的记录
                LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
                lqw.eq(SpecialListDo::getSceneCode, vo.getSceneCode())
                    .eq(SpecialListDo::getTargetType, vo.getTargetType())
                    .eq(SpecialListDo::getTargetValue, vo.getTargetValue());
                SpecialListDo existing = baseMapper.selectOne(lqw);

                if (existing != null) {
                    existing.setStatus(vo.getStatus());
                    existing.setEffectiveTime(vo.getEffectiveTime());
                    existing.setExpireTime(vo.getExpireTime());
                    existing.setReason(vo.getReason());
                    existing.setUpdateTime(new Date());
                    updateList.add(existing);
                    resultVo.setResult("成功");
                } else {
                    errorMsg.append("未找到匹配的记录; ");
                }
            }

            if (errorMsg.length() > 0) {
                resultVo.setResult("失败");
                resultVo.setErrorMsg(errorMsg.toString());
            }

            resultList.add(resultVo);
        }

        // 批量更新有效记录
        if (CollUtil.isNotEmpty(updateList)) {
            baseMapper.updateBatchById(updateList, 100);
            log.info("特殊名单批量编辑完成，成功更新 {} 条记录", updateList.size());
        }

        return resultList;
    }

}
