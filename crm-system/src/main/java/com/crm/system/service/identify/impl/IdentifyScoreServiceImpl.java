package com.crm.system.service.identify.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.file.FileAsyncTaskBo;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;
import com.crm.system.mapper.identify.IdentifyScoreMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.file.IFileAsyncTaskService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import com.crm.system.service.identify.IIdentifyScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2026-02-02
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class IdentifyScoreServiceImpl implements IIdentifyScoreService {
    @Resource
    private IdentifyScoreMapper identifyScoreMapper;
    @Resource
    private ISysOssService ossService;
    @Resource
    private ISysDeptService iSysDeptService;
    @Resource
    private ISysAreaService iSysAreaService;
    @Resource
    private IIdentifyScoreCustomerService iIdentifyScoreCustomerService;

    @Resource
    private IFileAsyncTaskService iFileAsyncTaskService;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public IdentifyScoreVo queryById(Long id) {
        return identifyScoreMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<IdentifyScoreVo> queryPageList(IdentifyScoreBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IdentifyScoreDo> lqw = buildQueryWrapper(bo);
        Page<IdentifyScoreVo> result = identifyScoreMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<IdentifyScoreVo> queryList(IdentifyScoreBo bo) {
        LambdaQueryWrapper<IdentifyScoreDo> lqw = buildQueryWrapper(bo);
        return identifyScoreMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<IdentifyScoreDo> buildQueryWrapper(IdentifyScoreBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IdentifyScoreDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getCustomerId() != null, IdentifyScoreDo::getCustomerId, bo.getCustomerId());
        lqw.eq(StringUtils.isNotBlank(bo.getTotalScore()), IdentifyScoreDo::getTotalScore, bo.getTotalScore());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), IdentifyScoreDo::getContent, bo.getContent());
        lqw.eq(StringUtils.isNotBlank(bo.getName()), IdentifyScoreDo::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreStatus()), IdentifyScoreDo::getScoreStatus, bo.getScoreStatus());
        lqw.eq(bo.getUploadTime() != null, IdentifyScoreDo::getUploadTime, bo.getUploadTime());
        lqw.eq(bo.getUploader() != null, IdentifyScoreDo::getUploader, bo.getUploader());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(IdentifyScoreBo bo) {
        IdentifyScoreDo add = BeanUtil.toBean(bo, IdentifyScoreDo.class);
        validEntityBeforeSave(add);
        boolean flag = identifyScoreMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(IdentifyScoreBo bo) {
        IdentifyScoreDo update = BeanUtil.toBean(bo, IdentifyScoreDo.class);
        validEntityBeforeSave(update);
        return identifyScoreMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(IdentifyScoreDo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return identifyScoreMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public Boolean save(Long[] ossIds) {
        Long ds = null;
        Long xf = null;
        try {
            List<SysOssVo> list = ossService.listByIds(Arrays.asList(ossIds));
            Long deptId = LoginHelper.getDeptId();
            SysDept sysDept = iSysDeptService.selectDeptById(deptId);
            SysAreaVo sysAreaVo = iSysAreaService.queryById(Long.valueOf(sysDept.getAreaCode()));
            if (ObjectUtil.equals("二级", sysAreaVo.getType())) {
                ds = sysAreaVo.getId();
            } else {
                xf = iSysAreaService.queryById(sysAreaVo.getParent()).getId();
            }
            if (ObjectUtil.equals("三级", sysAreaVo.getType())) {
                xf = sysAreaVo.getId();
            }
            Date now = DateUtil.date();
            for (SysOssVo sysOssVo : list) {
                IdentifyScoreCustomerBo customerDo = new IdentifyScoreCustomerBo();
                customerDo.setFileName(sysOssVo.getFileName());
                customerDo.setFileUrlId(sysOssVo.getOssId());
                customerDo.setCreatorId(LoginHelper.getUserId());
                customerDo.setUpdaterId(LoginHelper.getUserId());
                customerDo.setAreaId(ds);
                customerDo.setCountyId(xf);
                iIdentifyScoreCustomerService.insertByBo(customerDo);
                // 创建异步任务
                FileAsyncTaskBo taskBo = new FileAsyncTaskBo();
                taskBo.setTaskType("import");
                taskBo.setFileName(sysOssVo.getFileName());

                taskBo.setFileUrl(sysOssVo.getUrl());
                taskBo.setBusinessType("identify_score_import");
                taskBo.setStatus(0L); // 0-待处理
                taskBo.setCreateUserId(LoginHelper.getUserId());

                // 生成任务ID：yyyymmdd+0001（4位序列号）格式
                String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                String sequence = iFileAsyncTaskService.getDailySequence();
                String taskIdStr = dateStr + sequence;
                Long taskId = Long.valueOf(taskIdStr);
                taskBo.setTaskId(taskId);
                taskBo.setTaskName("三清单文件导入");
                taskBo.setStartTime(LocalDateTime.now());
                iFileAsyncTaskService.insertByBo(taskBo);

                IdentifyScoreDo scoreDo = new IdentifyScoreDo();
                scoreDo.setCustomerId(customerDo.getId());
                scoreDo.setUploadTime(now);
                scoreDo.setName(sysOssVo.getFileName());
                scoreDo.setScoreStatus("0");
                scoreDo.setUploader(LoginHelper.getUserId());
                scoreDo.setTaskId(taskBo.getTaskId());
                identifyScoreMapper.insert(scoreDo);
            }
        } catch (Exception e) {
            log.error("保存失败", e);
            throw new RuntimeException("导入任务申请异常: " + e.getMessage());
        }
        return true;
    }

    @Override
    public IdentifyScoreDo queryByCustomerId(Long custmoerId,Long taskId) {
        return identifyScoreMapper.selectOne(new LambdaQueryWrapper<IdentifyScoreDo>()
            .eq(IdentifyScoreDo::getCustomerId, custmoerId)
            .eq(IdentifyScoreDo::getTaskId, taskId));
    }
}
