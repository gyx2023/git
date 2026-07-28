package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.ZqztOperRecordBo;
import com.crm.system.domain.vo.ZqztOperRecordVo;
import com.crm.system.domain.dao.ZqztOperRecord;
import com.crm.system.mapper.ZqztOperRecordMapper;
import com.crm.system.service.IZqztOperRecordService;

import java.util.*;

/**
 * 系统操作记录Service业务层处理
 *
 * @author crm
 * @date 2026-04-02
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ZqztOperRecordServiceImpl implements IZqztOperRecordService {

    private final ZqztOperRecordMapper baseMapper;

    /**
     * 查询系统操作记录
     */
    @Override
    public ZqztOperRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询系统操作记录列表
     */
    @Override
    public TableDataInfo<ZqztOperRecordVo> queryPageList(ZqztOperRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztOperRecord> lqw = buildQueryWrapper(bo);
        Page<ZqztOperRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询系统操作记录列表
     */
    @Override
    public List<ZqztOperRecordVo> queryList(ZqztOperRecordBo bo) {
        LambdaQueryWrapper<ZqztOperRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztOperRecord> buildQueryWrapper(ZqztOperRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztOperRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getModuleCode()), ZqztOperRecord::getModuleCode, bo.getModuleCode());
        lqw.eq(StringUtils.isNotBlank(bo.getOperCode()), ZqztOperRecord::getOperCode, bo.getOperCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), ZqztOperRecord::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestParams()), ZqztOperRecord::getRequestParams, bo.getRequestParams());
        lqw.eq(StringUtils.isNotBlank(bo.getExtraInfo()), ZqztOperRecord::getExtraInfo, bo.getExtraInfo());
        lqw.eq(bo.getStatus() != null, ZqztOperRecord::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getErrorMsg()), ZqztOperRecord::getErrorMsg, bo.getErrorMsg());
        lqw.eq(bo.getOperUserId() != null, ZqztOperRecord::getOperUserId, bo.getOperUserId());
        lqw.like(StringUtils.isNotBlank(bo.getOperUserName()), ZqztOperRecord::getOperUserName, bo.getOperUserName());
        lqw.eq(bo.getOperTime() != null, ZqztOperRecord::getOperTime, bo.getOperTime());
        lqw.eq(StringUtils.isNotBlank(bo.getOperIp()), ZqztOperRecord::getOperIp, bo.getOperIp());
        return lqw;
    }

    /**
     * 新增系统操作记录
     */
    @Override
    public Boolean insertByBo(ZqztOperRecordBo bo) {
        ZqztOperRecord add = BeanUtil.toBean(bo, ZqztOperRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改系统操作记录
     */
    @Override
    public Boolean updateByBo(ZqztOperRecordBo bo) {
        ZqztOperRecord update = BeanUtil.toBean(bo, ZqztOperRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztOperRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除系统操作记录
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 根据集团信息新增系统操作记录
     *
     * @param groupInfo 集团信息对象
     * @param status 操作状态：0-成功，1-失败（可选，默认 0）
     * @param errorMsg 错误信息（可选，默认空字符串）
     * @return 新增结果
     */
    @Override
    public Boolean insertByGroupInfo(ZqGrpGroupInfo groupInfo, Integer status, String errorMsg) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        ZqztOperRecord record = new ZqztOperRecord();
        record.setModuleCode("group");
        record.setOperCode("group_info_desensitization");
        record.setBusinessId(String.valueOf(groupInfo.getGrpId()));
        // 将 GRP_ID 转为 JSON 格式存储到 request_params
        Map<String, Object> requestParamsMap = new HashMap<>();
        requestParamsMap.put("GRP_ID", groupInfo.getGrpId());
        record.setRequestParams(JSONUtil.toJsonStr(requestParamsMap));
        // 只有成功状态（0）才记录 extra_info
        if (status == null || status == 0) {
            // 将 GRP_ID、GRP_NAME、GRP_CODE、HQ_GRP_CODE 四个字段转为 JSON 格式存储到 extra_info
            Map<String, Object> extraInfoMap = new HashMap<>();
            extraInfoMap.put("GRP_ID", groupInfo.getGrpId());
            extraInfoMap.put("GRP_NAME", groupInfo.getGrpName());
            extraInfoMap.put("GRP_CODE", groupInfo.getGrpCode());
            extraInfoMap.put("HQ_GRP_CODE", groupInfo.getHqGrpCode());
            record.setExtraInfo(JSONUtil.toJsonStr(extraInfoMap));
        }
        // 设置状态和错误信息（如果为 null 则使用默认值）
        record.setStatus(status != null ? status : 0);
        record.setErrorMsg(errorMsg != null ? errorMsg : "");
        record.setOperUserId(loginUser.getUserId());
        record.setOperUserName(loginUser.getUsername());
        record.setOperTime(new Date());
        record.setOperIp(loginUser.getIpaddr());
        return baseMapper.insert(record) > 0;
    }

}
