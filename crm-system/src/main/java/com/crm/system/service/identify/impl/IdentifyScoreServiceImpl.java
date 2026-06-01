package com.crm.system.service.identify.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.WordTextExtractor;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.bo.identify.IdentifyScoreCustomerBo;
import com.crm.system.domain.identify.IdentifyScoreCustomerDo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.identify.IdentifyScoreCustomerVo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;
import com.crm.system.mapper.identify.IdentifyScoreMapper;
import com.crm.system.service.sys.ISysOssService;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.identify.IIdentifyScoreCustomerService;
import com.crm.system.service.identify.IIdentifyScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
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
    private IIdentifyScoreCustomerService iIdentifyScoreCustomerService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ScoreDataParser scoreDataParser;

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
        if (ObjectUtil.isNotEmpty(bo.getUploadTimeBegin())) {
            lqw.ge(IdentifyScoreDo::getUploadTime, bo.getUploadTimeBegin());
        }
        if (ObjectUtil.isNotEmpty(bo.getUploadTimeEnd())) {
            lqw.le(IdentifyScoreDo::getUploadTime, bo.getUploadTimeEnd());
        }
        Page<IdentifyScoreVo> result = identifyScoreMapper.selectVoPage(pageQuery.build(), lqw);
        Map<Long, String> userMap = sysUserService.listAll();
        result.getRecords().forEach(item -> {
            item.setUserName(userMap.get(item.getUploader()));
        });
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
        try {
            List<SysOssVo> list = ossService.listByIds(Arrays.asList(ossIds));
            Date now = DateUtil.date();
            for (SysOssVo sysOssVo : list) {
                createTask(sysOssVo,now);
            }
        } catch (Exception e) {
            log.error("保存失败", e);
            throw new RuntimeException("导入任务申请异常: " + e.getMessage());
        }
        return true;
    }

    private void createTask(SysOssVo sysOssVo,Date now) throws Exception {
        SysUser sysUser = sysUserService.selectUserById(LoginHelper.getUserId());
        log.info("开始三清单导入任务，文件URL: {}", sysOssVo.getUrl());
        try {
            // 3. 读取Word文件内容
            String wordContent = readWordFileContent(sysOssVo);
            if (ObjectUtil.isEmpty(wordContent)) {
                String errorMsg = "处理失败：Word文件内容为空，OSS ID: " + sysOssVo.getOssId();
                log.error(errorMsg);
            }

            // 4. 解析客户名称（委托解析器）
            String custName = scoreDataParser.extractCustName(wordContent);
            IdentifyScoreCustomerDo customerDo = iIdentifyScoreCustomerService.queryByCustName(custName, sysUser.getUserId());
            IdentifyScoreDo scoreDo = new IdentifyScoreDo();
            if(ObjectUtil.isEmpty(customerDo)){
                IdentifyScoreCustomerBo  customerBo = iIdentifyScoreCustomerService.initCustomerData(sysUser, sysOssVo);
                customerBo.setCustName(custName);
                iIdentifyScoreCustomerService.insertByBo(customerBo);
                scoreDo.setCustomerId(customerBo.getId());
            }else{
                scoreDo.setCustomerId(customerDo.getId());
                customerDo.setFileName(sysOssVo.getOriginalName());
                customerDo.setUpdateTime(new Date());
                IdentifyScoreCustomerBo customerBo = BeanUtil.toBean(customerDo, IdentifyScoreCustomerBo.class);
                iIdentifyScoreCustomerService.updateByBo(customerBo);
            }
            scoreDo.setUploadTime(now);
            scoreDo.setName(sysOssVo.getOriginalName());
            scoreDo.setFileText(wordContent);
            scoreDo.setScoreStatus("0");
            scoreDo.setUploader(LoginHelper.getUserId());
            scoreDo.setIsAgain("0");
            identifyScoreMapper.insert(scoreDo);
        } catch (Exception e) {
            log.error("更新评分失败状态异常", e);
        }
    }


    @Override
    public Boolean again(Long customerId) {
        IdentifyScoreCustomerVo customerVo = iIdentifyScoreCustomerService.queryById(customerId);
        IdentifyScoreDo scoreDo = identifyScoreMapper.selectList(new LambdaQueryWrapper<IdentifyScoreDo>()
            .eq(IdentifyScoreDo::getCustomerId, customerId)
            .eq(IdentifyScoreDo::getTotalScore, customerVo.getTotalScore())
            .eq(IdentifyScoreDo::getScoreStatus, "1")
            .eq(IdentifyScoreDo::getName, customerVo.getFileName())
            .orderByDesc(IdentifyScoreDo::getUploadTime)
        ).get(0);
        if(ObjectUtil.isNotEmpty(scoreDo)){
            IdentifyScoreDo newScoreDo = new IdentifyScoreDo();
            newScoreDo.setUploadTime(new Date());
            newScoreDo.setName(scoreDo.getName());
            newScoreDo.setCustomerId(customerId);
            newScoreDo.setFileText(scoreDo.getFileText());
            newScoreDo.setScoreStatus("0");
            newScoreDo.setUploader(LoginHelper.getUserId());
            newScoreDo.setIsAgain("1");
            identifyScoreMapper.insert(newScoreDo);
        }
        return true;
    }

    @Override
    public void updateById(IdentifyScoreDo scoreDo) {
        identifyScoreMapper.updateById(scoreDo);
    }

    // 原有方法保持不变，新增以下方法


    /**
     * 更新评分数据
     */
    @Override
    public void updateScoreData(IdentifyScoreCustomerDo customerDo, IdentifyScoreDo
        scoreDo, Map<String, Object> scoreResultMap,String status) {
        // 更新客户表
        if (ObjectUtil.equals("0", scoreDo.getIsAgain())) {
            customerDo.setContent(Objects.toString(scoreResultMap.get("content"), null));
            customerDo.setTotalScore(Objects.toString(scoreResultMap.get("totalScore"), null));
            customerDo.setUpdateTime(new Date());
            iIdentifyScoreCustomerService.updateByBo(BeanCopyUtils.copy(customerDo, IdentifyScoreCustomerBo.class));
        }
        // 更新评分表
        scoreDo.setContent(Objects.toString(scoreResultMap.get("content"), null));
        scoreDo.setTotalScore(Objects.toString(scoreResultMap.get("totalScore"), null));
        scoreDo.setScoreStatus(status); // 成功状态
        scoreDo.setCustomerId(customerDo.getId());
        scoreDo.setUpdateTime(new Date());
        updateByBo(BeanCopyUtils.copy(scoreDo, IdentifyScoreBo.class));
    }

    @Override
    public List<IdentifyScoreDo> queryWaitList() {
        return identifyScoreMapper.selectList
            (new LambdaQueryWrapper<IdentifyScoreDo>()
                .eq(IdentifyScoreDo::getScoreStatus, "0"));
    }

    /**
     * 读取Word文件内容
     */
    public String readWordFileContent(SysOssVo sysOss) throws Exception {
        InputStream fileContent = ossService.getFileContent(sysOss.getOssId());
        StringBuilder wordText = WordTextExtractor.readWordText(
            fileContent, sysOss.getFileName()
        );
        return ObjectUtil.isEmpty(wordText) ? "" : wordText.toString();
    }
}
