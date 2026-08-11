package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.SysUsersMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * sys_user表数据导出定时任务
 * 将sys_user表中所有数据导出到文件，文件名为sys_users_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class SysUsersExport extends AbstractTableDataExporter<SysUsers> {

    @Resource
    private SysUsersMapper sysUsersMapper;

    @XxlJob("SysUsersExportHandler")
    public void execute() {
        String jobParam = XxlJobHelper.getJobParam();
        String configType = null;
        String dateStr = null;
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            String[] parts = jobParam.split(":");
            if (parts.length == 2) {
                configType = parts[0].isEmpty() ? null : parts[0];
                dateStr = parts[1].isEmpty() ? null : parts[1];
            } else if (parts.length == 1) {
                if (parts[0].matches("\\d{8}")) {
                    dateStr = parts[0];
                } else {
                    configType = parts[0];
                }
            }
        }
        XxlJobHelper.log("开始执行{}导出任务", getFileNamePrefix());
        executeExport(dateStr, configType);
    }

    @Override
    protected long getTotalCount() {
        return sysUsersMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<SysUsers> w = new LambdaQueryWrapper<SysUsers>()
                .select(SysUsers::getId).orderByAsc(SysUsers::getId).last("LIMIT 1");
        SysUsers r = sysUsersMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<SysUsers> w = new LambdaQueryWrapper<SysUsers>()
                .select(SysUsers::getId).orderByDesc(SysUsers::getId).last("LIMIT 1");
        SysUsers r = sysUsersMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<SysUsers> queryByRange(long startId, long endId) {
        return sysUsersMapper.selectList(new LambdaQueryWrapper<SysUsers>()
                .ge(SysUsers::getId, startId)
                .lt(SysUsers::getId, endId)
                .orderByAsc(SysUsers::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "sys_users";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "name", "nameCh", "relationTel", "type",
                "orgId", "password", "passwordSalt", "functionList", "roleList",
                "remark", "creator", "createTime", "modifier", "modifyTime",
                "isValid", "wxUserFlag", "wxPhone", "wxNo", "wxEmail",
                "wxFlag", "stealth", "customerNo", "oaAccount", "email",
                "firstLogin", "updataTime", "includeType", "unicomType", "unicomType2",
                "data", "token", "markunits", "developerCodes", "cardNumber",
                "staffType", "userRoleYx", "zqbusinessFlag", "recSmsBeginTime", "recSmsEndTime",
                "associatedDeveloper", "defaultDeveloper", "checkDeveloper"
        };
    }

    @Override
    protected Object[] getColumns(SysUsers r) {
        return new Object[]{
                r.getId(), r.getName(), r.getNameCh(), r.getRelationTel(), r.getType(),
                r.getOrgId(), r.getPassword(), r.getPasswordSalt(), r.getFunctionList(), r.getRoleList(),
                r.getRemark(), r.getCreator(), r.getCreateTime(), r.getModifier(), r.getModifyTime(),
                r.getIsValid(), r.getWxUserFlag(), r.getWxPhone(), r.getWxNo(), r.getWxEmail(),
                r.getWxFlag(), r.getStealth(), r.getCustomerNo(), r.getOaAccount(), r.getEmail(),
                r.getFirstLogin(), r.getUpdateTime(), r.getIncludeType(), r.getUnicomType(), r.getUnicomType2(),
                r.getData(), r.getToken(), r.getMarkunits(), r.getDeveloperCodes(), r.getCardNumber(),
                r.getStaffType(), r.getUserRoleYx(), r.getZqbusinessFlag(), r.getRecSmsBeginTime(), r.getRecSmsEndTime(),
                r.getAssociatedDeveloper(), r.getDefaultDeveloper(), r.getCheckDeveloper()
        };
    }

    @Override
    protected boolean isParamSupported() {
        return true;
    }

    @Override
    protected boolean isFileValidationNeeded() {
        return false;
    }
}
