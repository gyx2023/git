package com.crm.system.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListBatchEditVo;
import com.crm.system.domain.vo.SpecialListImportResultVo;
import com.crm.system.domain.vo.SpecialListImportVo;
import com.crm.system.mapper.SpecialListMapper;
import com.crm.system.service.sys.ISysDictTypeService;
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

    /** 默认失效时间：2050-12-31 23:59:59（导入未填失效时间时使用） */
    private static final Date DEFAULT_EXPIRE_TIME = buildDefaultExpireTime();

    private static Date buildDefaultExpireTime() {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2050-12-31 23:59:59");
        } catch (java.text.ParseException e) {
            // 理论上不会发生（字面量固定），兜底用日历构造
            Calendar c = Calendar.getInstance();
            c.set(2050, Calendar.DECEMBER, 31, 23, 59, 59);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        }
    }

    @Resource
    private SpecialListMapper baseMapper;

    @Resource
    private ISysDictTypeService dictTypeService;

    /**
     * 场景名称(字典label) → 场景编码(字典value) 映射
     */
    private Map<String, String> buildSceneLabelToValueMap() {
        return buildLabelToValueMap("yunxi_type");
    }

    /**
     * 状态(字典label) → 状态码(字典value) 映射
     */
    private Map<String, String> buildStatusLabelToValueMap() {
        return buildLabelToValueMap("special_list_status");
    }

    private Map<String, String> buildLabelToValueMap(String dictType) {
        return dictTypeService.selectDictDataByType(dictType).stream()
            .collect(HashMap::new, (m, d) -> m.put(d.getDictLabel(), d.getDictValue()), HashMap::putAll);
    }

    /**
     * 处理导入数据（新增）
     *
     * @param dataList 导入数据列表
     * @param createUserId 创建人用户ID（从任务中获取，异步执行时无登录上下文）
     * @return 导入结果列表
     */
    public List<SpecialListImportResultVo> processImportData(List<SpecialListImportVo> dataList, Long createUserId) {
        if (CollUtil.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        List<SpecialListImportResultVo> resultList = new ArrayList<>();
        List<SpecialListDo> insertList = new ArrayList<>();
        // 用于校验导入数据内的重复
        Set<String> batchKeys = new HashSet<>();
        // 场景名称 label -> value 映射
        Map<String, String> sceneLabelValueMap = buildSceneLabelToValueMap();

        for (SpecialListImportVo vo : dataList) {
            SpecialListImportResultVo resultVo = new SpecialListImportResultVo();
            // 复制导入字段到结果对象
            BeanUtil.copyProperties(vo, resultVo);

            StringBuilder errorMsg = new StringBuilder();

            // 校验场景名称，并反查出字典value
            String sceneCodeValue = null;
            if (StringUtils.isBlank(vo.getSceneCode())) {
                errorMsg.append("场景名称不能为空; ");
            } else {
                sceneCodeValue = sceneLabelValueMap.get(vo.getSceneCode());
                if (sceneCodeValue == null) {
                    errorMsg.append("场景名称「").append(vo.getSceneCode()).append("」不在字典中; ");
                }
            }

            // 校验特殊名单值
            if (StringUtils.isBlank(vo.getTargetValue())) {
                errorMsg.append("特殊名单值不能为空; ");
            }

            String batchKey = null;
            if (errorMsg.length() == 0) {
                // 校验导入数据内重复
                batchKey = sceneCodeValue + ":" + vo.getTargetValue();
                if (batchKeys.contains(batchKey)) {
                    errorMsg.append("导入数据中存在重复记录; ");
                } else {
                    // 校验数据库重复
                    LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
                    lqw.eq(SpecialListDo::getSceneCode, sceneCodeValue)
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
                // 结果对象写上字典值
                resultVo.setSceneCode(sceneCodeValue);
                // 构建新增对象
                SpecialListDo insertDo = new SpecialListDo();
                insertDo.setSceneCode(sceneCodeValue);
                insertDo.setTargetValue(vo.getTargetValue());
                insertDo.setStatus("1");
                // 生效/失效时间为空时给默认值：生效时间=当前时间，失效时间=2050-12-31 23:59:59
                insertDo.setEffectiveTime(!ObjectUtil.isEmpty(vo.getEffectiveTime()) ? vo.getEffectiveTime() : new Date());
                insertDo.setExpireTime(!ObjectUtil.isEmpty(vo.getExpireTime()) ? vo.getExpireTime() : DEFAULT_EXPIRE_TIME);
                insertDo.setReason(vo.getReason());
                if (createUserId != null) {
                    insertDo.setCreateBy(createUserId.intValue());
                    insertDo.setUpdateBy(createUserId.intValue());
                }
                insertDo.setCreateTime(new Date());
                insertDo.setUpdateTime(new Date());
                insertList.add(insertDo);

                batchKeys.add(batchKey);
                resultVo.setResult("成功");
            }

            resultList.add(resultVo);
        }

        // 逐条插入有效记录
        if (CollUtil.isNotEmpty(insertList)) {
            for (SpecialListDo insertDo : insertList) {
                baseMapper.insert(insertDo);
            }
        }
        log.info("特殊名单导入完成，成功插入 {} 条记录", insertList.size());

        return resultList;
    }

    /**
     * 处理批量编辑数据（修改）
     *
     * @param dataList 批量编辑数据列表
     * @param createUserId 操作人用户ID（从任务中获取，异步执行时无登录上下文）
     * @return 处理结果列表
     */
    public List<SpecialListImportResultVo> processBatchEditData(List<SpecialListBatchEditVo> dataList, Long createUserId) {
        if (CollUtil.isEmpty(dataList)) {
            return Collections.emptyList();
        }

        List<SpecialListImportResultVo> resultList = new ArrayList<>();
        List<SpecialListDo> updateList = new ArrayList<>();
        // 场景名称 label -> value 映射
        Map<String, String> sceneLabelValueMap = buildSceneLabelToValueMap();
        // 状态 label -> value 映射
        Map<String, String> statusLabelValueMap = buildStatusLabelToValueMap();

        for (SpecialListBatchEditVo vo : dataList) {
            SpecialListImportResultVo resultVo = new SpecialListImportResultVo();
            resultVo.setTargetValue(vo.getTargetValue());
            resultVo.setEffectiveTime(vo.getEffectiveTime());
            resultVo.setExpireTime(vo.getExpireTime());
            resultVo.setReason(vo.getReason());

            StringBuilder errorMsg = new StringBuilder();

            // 校验场景名称，并反查出字典value
            String sceneCodeValue = null;
            if (StringUtils.isBlank(vo.getSceneCode())) {
                errorMsg.append("场景名称不能为空; ");
            } else {
                sceneCodeValue = sceneLabelValueMap.get(vo.getSceneCode());
                if (sceneCodeValue == null) {
                    errorMsg.append("场景名称「").append(vo.getSceneCode()).append("」不在字典中; ");
                }
            }

            // 校验特殊名单值
            if (StringUtils.isBlank(vo.getTargetValue())) {
                errorMsg.append("特殊名单值不能为空; ");
            }

            // 校验状态，并反查出字典value
            String statusValue = null;
            if (StringUtils.isBlank(vo.getStatus())) {
                errorMsg.append("状态不能为空; ");
            } else {
                statusValue = statusLabelValueMap.get(vo.getStatus());
                if (statusValue == null) {
                    errorMsg.append("状态「").append(vo.getStatus()).append("」不在字典中; ");
                }
            }

            if (errorMsg.length() == 0) {
                // 反填字典值到结果对象
                resultVo.setSceneCode(sceneCodeValue);
                resultVo.setTargetValue(vo.getTargetValue());
                resultVo.setEffectiveTime(vo.getEffectiveTime());
                resultVo.setExpireTime(vo.getExpireTime());
                resultVo.setReason(vo.getReason());
                // 查找已存在的记录
                LambdaQueryWrapper<SpecialListDo> lqw = Wrappers.lambdaQuery();
                lqw.eq(SpecialListDo::getSceneCode, sceneCodeValue)
                    .eq(SpecialListDo::getTargetValue, vo.getTargetValue());
                SpecialListDo existing = baseMapper.selectOne(lqw);

                if (existing != null) {
                    existing.setStatus(statusValue);
                    existing.setEffectiveTime(vo.getEffectiveTime());
                    existing.setExpireTime(vo.getExpireTime());
                    existing.setReason(vo.getReason());
                    existing.setUpdateTime(new Date());
                    if (createUserId != null) {
                        existing.setUpdateBy(createUserId.intValue());
                    }
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

        // 逐条更新有效记录
        if (CollUtil.isNotEmpty(updateList)) {
            for (SpecialListDo updateDo : updateList) {
                baseMapper.updateById(updateDo);
            }
            log.info("特殊名单批量编辑完成，成功更新 {} 条记录", updateList.size());
        }

        return resultList;
    }

}
