package com.crm.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.utils.StringUtils;

import com.crm.system.domain.bo.cust.CustomerTagSyncDTO;
import com.crm.system.domain.dao.DwdRepSysPersonD;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.crm.system.service.IZqGrpGroupLogService;
import com.crm.system.service.IZqztCustomerTagSyncService;
import com.crm.system.service.sys.ISysDictDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * RPA客户标签同步Service实现
 *
 * @author crm
 */
@Slf4j
@Service
public class ZqztCustomerTagSyncServiceImpl implements IZqztCustomerTagSyncService {

    @Resource
    private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;

    @Resource
    private IZqGrpGroupInfoService zqGrpGroupInfoService;

    @Resource
    private IZqGrpGroupLogService zqGrpGroupLogService;

    @Resource
    private ISysDictDataService sysDictDataService;

    @Resource
    private DwdRepSysPersonDMapper dwdRepSysPersonDMapper;

    /**
     * 同步集团客户标签信息到zqqt_grp_group_info表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean syncCustomerTags(CustomerTagSyncDTO request) {
        String hqGrpCode = StringUtils.trimToNull(request.getHqGrpCode());
        if (StringUtils.isBlank(hqGrpCode)) {
            log.warn("[客户标签同步] 总部集团编码为空，跳过");
            return false;
        }

        // 1. 根据总部集团编码查询是否存在
        ZqGrpGroupInfo existing = zqGrpGroupInfoService.selectByHqGrpCode(hqGrpCode);
        if (existing == null) {
            log.error("[客户标签同步] 集团客户编码不存在: hqGrpCode={}", hqGrpCode);
            return false;
        }

        Long grpId = existing.getGrpId();

        // 2. 行业分类: 通过dict_label查dict_value
        String industryMappingLabel = StringUtils.trimToNull(request.getIndustryClassification());
        String industryMappingValue = null;
        if(StringUtils.isNotEmpty(industryMappingLabel)){
            String[] parts = industryMappingLabel.split("/");
            String lastPart = parts[parts.length - 1];
            industryMappingValue = resolveDictLabelToValue(lastPart,"rpa_busi_dept_type");
        }
        // 3. 直拓类型
        String expandTypeLabel = StringUtils.trimToNull(request.getDirectExpansionType());
        String expandTypeValue = resolveDictLabelToValue(expandTypeLabel,"rpa_expand_type");

        // 4. 发展人OA号: 查dwd_rep_sys_person_d
        String developerOa = StringUtils.trimToNull(request.getDeveloperOa());
        String custManagerEmpid = null;
        String custManagerName = null;
        if (StringUtils.isNotBlank(developerOa)) {
            DwdRepSysPersonD person = dwdRepSysPersonDMapper.selectOne(
                new LambdaQueryWrapper<DwdRepSysPersonD>()
                    .eq(DwdRepSysPersonD::getOaUserName, developerOa)
                    .last("LIMIT 1")
            );
            if (person != null) {
                custManagerEmpid = person.getEmpId();
                custManagerName = person.getFullName();
            } else {
                log.warn("[客户标签同步] 未找到发展人OA号对应的人员信息: oa={}", developerOa);
            }
        }
        //客户类型
        String customerTypeLabel = StringUtils.trimToNull(request.getCustomerType());
        String customerTypeValue = resolveDictLabelToValue(customerTypeLabel,"rpa_cust_type");
        try {
            // 5. 更新 zqqt_grp_group_info
            ZqGrpGroupInfo update = new ZqGrpGroupInfo();
            update.setGrpId(grpId);

            boolean hasUpdate = false;

            // 只有当解析成功时才更新对应字段
            if (StringUtils.isNotBlank(industryMappingValue)) {
                update.setIndustryDetailKind(industryMappingValue);
                hasUpdate = true;
            }
            if (StringUtils.isNotBlank(expandTypeValue)) {
                update.setGrpExpandType(expandTypeValue);
                hasUpdate = true;
            }
            if (StringUtils.isNotBlank(custManagerEmpid)) {
                update.setCustManagerEmpId(custManagerEmpid);
                hasUpdate = true;
            }
            if (StringUtils.isNotBlank(custManagerOauser(developerOa))) {
                update.setCustManagerOauser(custManagerOauser(developerOa));
                hasUpdate = true;
            }
            if (StringUtils.isNotBlank(custManagerName)) {
                update.setCustManagerName(custManagerName);
                hasUpdate = true;
            }
            if(StringUtils.isNotBlank(customerTypeValue)){
                update.setGrpCustType(customerTypeValue);
                hasUpdate = true;
            }

            if (hasUpdate) {
                update.setUpdateTime(new Date());
                int rows = zqGrpGroupInfoMapper.updateById(update);

                // 6. 记录变更日志(T2-集团客户经理变更)
                if (rows > 0) {
                    zqGrpGroupLogService.recordChangeLog(existing, "T1");
                }

                return rows > 0;
            } else {
                return true;
            }

        } catch (Exception e) {
            log.error("[客户标签同步] 更新失败: hqGrpCode={}, error={}", hqGrpCode, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 通过dict_label查询dict_value
     */
    private String resolveDictLabelToValue(String label,String dictType) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        try {
            SysDictData query = new SysDictData();
            query.setDictLabel(label);
            query.setDictType(dictType);
            query.setStatus("0");
            List<SysDictData> list = sysDictDataService.selectDictDataList(query);
            if (list != null && !list.isEmpty()) {
                String value = list.get(0).getDictValue();
                log.info("[客户标签同步] 字典项映射: label={} -> dict_value={}", label, value);
                return value;
            }
        } catch (Exception e) {
            log.error("[客户标签同步] 查询字典失败: label={}, error={}", label, e.getMessage());
        }
        log.warn("[客户标签同步] 字典表中未找到匹配的行业分类标签: {}", label);
        return null;
    }

    private String custManagerOauser(String oa) {
        return StrUtil.isBlank(oa) ? null : oa.toLowerCase();
    }
}
