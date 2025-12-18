package com.crm.job.service.oldsyssyn;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.enums.UserType;
import com.crm.system.domain.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.oldsyssyn.OldSysUserDo;
import com.crm.system.mapper.SysDeptMapper;
import com.crm.system.mapper.SysUserMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.mapper.oldsyssyn.OldSysUserMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户同步定时任务
 * 将sys_user表中的数据同步到zqzt_sys_user表中
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserSyncJob {

    @Resource
    private OldSysUserMapper oldSysUserMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private OldSysOrgMapper oldSysOrgMapper;

    @Resource
    private SysDeptMapper sysDeptMapper;

    /**
     * 系统用户同步任务
     * 在xxl-job调度中心配置JobHandler为：sysUserSyncJobHandler
     */
    @XxlJob("sysUserSyncJobHandler")
    public void syncSysUserToZqztSysUser() {
        XxlJobHelper.log("开始执行系统用户同步任务...");
        log.info("开始执行系统用户同步任务...");

        try {
            // 在同步前清空zqzt_sys_user表（使用TRUNCATE方式清空，更高效且重置自增ID）
            try {
                XxlJobHelper.log("开始清空zqzt_sys_user表...");
                log.info("开始清空zqzt_sys_user表...");
                // 使用自定义SQL执行TRUNCATE操作，高效清空表并重置自增ID
                sysUserMapper.executeTruncate();
                XxlJobHelper.log("zqzt_sys_user表TRUNCATE完成");
                log.info("zqzt_sys_user表TRUNCATE完成");
            } catch (Exception e) {
                XxlJobHelper.log("清空zqzt_sys_user表失败：{}", e.getMessage());
                log.error("清空zqzt_sys_user表失败：{}", e.getMessage(), e);
                // 设置任务执行失败
                XxlJobHelper.handleFail("清空zqzt_sys_user表失败：" + e.getMessage());
                return; // 表清空失败，终止后续同步操作
            }

            // 查询所有sys_user数据
            List<OldSysUserDo> oldSysUserList = oldSysUserMapper.selectList(null);

            XxlJobHelper.log("查询到{}条sys_user数据", oldSysUserList.size());
            log.info("查询到{}条sys_user数据", oldSysUserList.size());

            int successCount = 0;
            int skipCount = 0;
            int batchSize = 50; // 批处理大小
            List<SysUser> batchInsertList = new ArrayList<>();

            for (OldSysUserDo oldUser : oldSysUserList) {
                try {
                    // 跳过无效用户
                    if (oldUser.getValid() != null && !oldUser.getValid()) {
                        XxlJobHelper.log("用户{}无效，跳过同步", oldUser.getName());
                        log.debug("用户{}无效，跳过同步", oldUser.getName());
                        continue;
                    }

                    // 由于已使用TRUNCATE清空表，不需要再检查是否存在
                    // 创建新用户对象
                    SysUser newUser = buildSysUserFromOldUser(oldUser);
                    batchInsertList.add(newUser);
                    successCount++;

                    // 当积累到batchSize条记录时，执行批量插入
                    if (batchInsertList.size() >= batchSize) {
                        sysUserMapper.insertBatch(batchInsertList);
                        XxlJobHelper.log("批量插入{}条用户数据成功", batchInsertList.size());
                        log.info("批量插入{}条用户数据成功", batchInsertList.size());
                        batchInsertList.clear();
                    }

                } catch (Exception e) {
                    XxlJobHelper.log("同步用户{}失败：{}", oldUser.getName(), e.getMessage());
                    log.error("同步用户{}失败：{}", oldUser.getName(), e.getMessage());
                    // 从批处理列表中移除失败的记录
                    if (!batchInsertList.isEmpty()) {
                        batchInsertList.remove(batchInsertList.size() - 1);
                        successCount--;
                    }
                }
            }

            // 处理最后一批不足batchSize的数据
            if (!batchInsertList.isEmpty()) {
                sysUserMapper.insertBatch(batchInsertList);
                XxlJobHelper.log("批量插入最后{}条用户数据成功", batchInsertList.size());
                log.info("批量插入最后{}条用户数据成功", batchInsertList.size());
            }

            XxlJobHelper.log("系统用户同步任务完成，成功同步{}条数据", successCount);
            log.info("系统用户同步任务完成，成功同步{}条数据", successCount);


        } catch (Exception e) {
            XxlJobHelper.log("系统用户同步任务执行失败：{}", e.getMessage());
            log.error("系统用户同步任务执行失败：{}", e.getMessage(), e);
            // 设置任务执行失败
            XxlJobHelper.handleFail("系统用户同步任务执行失败：" + e.getMessage());
        }

            //开始执行同步组织信息
            syncSysDept();
    }

    /**
     * 同步组织信息
     * 将sys_orgd数据同步到zqzt_sys_dept表
     */
    private void syncSysDept() {
        XxlJobHelper.log("开始执行部门同步任务...");
        log.info("开始执行部门同步任务...");

        try {
            // 在同步前清空zqzt_sys_dept表（使用TRUNCATE方式清空，更高效且重置自增ID）
            try {
                XxlJobHelper.log("开始清空zqzt_sys_dept表...");
                log.info("开始清空zqzt_sys_dept表...");
                // 使用自定义SQL执行TRUNCATE操作，高效清空表并重置自增ID
                sysDeptMapper.executeTruncate();
                XxlJobHelper.log("zqzt_sys_dept表TRUNCATE完成");
                log.info("zqzt_sys_dept表TRUNCATE完成");
            } catch (Exception e) {
                XxlJobHelper.log("清空zqzt_sys_dept表失败：{}", e.getMessage());
                log.error("清空zqzt_sys_dept表失败：{}", e.getMessage(), e);
                // 部门表清空失败，记录错误但继续执行（因为这是在子方法中）
                return;
            }

            // 查询所有sys_orgd数据
            List<OldSysOrgDo> oldSysOrgList = oldSysOrgMapper.selectList(null);

            XxlJobHelper.log("查询到{}条sys_orgd数据", oldSysOrgList.size());
            log.info("查询到{}条sys_orgd数据", oldSysOrgList.size());

            int successCount = 0;
            int batchSize = 50; // 批处理大小
            List<SysDept> batchInsertList = new ArrayList<>();

            for (OldSysOrgDo oldOrg : oldSysOrgList) {
                try {
                    // 由于已使用TRUNCATE清空表，不需要再检查是否存在
                    // 创建新部门对象
                    SysDept newDept = buildSysDeptFromOldOrg(oldOrg);
                    batchInsertList.add(newDept);
                    successCount++;

                    // 当积累到batchSize条记录时，执行批量插入
                    if (batchInsertList.size() >= batchSize) {
                        sysDeptMapper.insertBatch(batchInsertList);
                        XxlJobHelper.log("批量插入{}条部门数据成功", batchInsertList.size());
                        log.info("批量插入{}条部门数据成功", batchInsertList.size());
                        batchInsertList.clear();
                    }

                } catch (Exception e) {
                    XxlJobHelper.log("同步部门{}失败：{}", oldOrg.getId(), e.getMessage());
                    log.error("同步部门{}失败：{}", oldOrg.getId(), e.getMessage());
                    // 从批处理列表中移除失败的记录
                    if (!batchInsertList.isEmpty()) {
                        batchInsertList.remove(batchInsertList.size() - 1);
                        successCount--;
                    }
                }
            }

            // 处理最后一批不足batchSize的数据
            if (!batchInsertList.isEmpty()) {
                sysDeptMapper.insertBatch(batchInsertList);
                XxlJobHelper.log("批量插入最后{}条部门数据成功", batchInsertList.size());
                log.info("批量插入最后{}条部门数据成功", batchInsertList.size());
            }
            XxlJobHelper.log("部门同步任务完成，成功同步{}条数据", successCount);
            log.info("部门同步任务完成，成功同步{}条数据", successCount);

        } catch (Exception e) {
            XxlJobHelper.log("部门同步任务执行失败：{}", e.getMessage());
            log.error("部门同步任务执行失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 将OldSysOrgDO对象转换为SysDept对象
     *
     * @param oldOrg 旧系统组织对象
     * @return 新系统部门对象
     */
    private SysDept buildSysDeptFromOldOrg(OldSysOrgDo oldOrg) {
        SysDept newDept = new SysDept();

        // 基础信息映射
        newDept.setDeptId(Long.valueOf(oldOrg.getId()));
        newDept.setParentId(oldOrg.getParent()==null?null:Long.valueOf(oldOrg.getParent())); // 默认根节点
        newDept.setDeptName(oldOrg.getName());
        newDept.setStatus("0"); // 默认启用状态
        newDept.setDelFlag("0"); // 默认未删除
        newDept.setAreaCode(String.valueOf(oldOrg.getAreaId()));

        // 审计信息
        newDept.setCreateBy("system_sync");
        newDept.setUpdateBy("system_sync");

        return newDept;
    }

    /**
     * 将OldSysUserDO对象转换为SysUser对象
     *
     * @param oldUser 旧系统用户对象
     * @return 新系统用户对象
     */
    private SysUser buildSysUserFromOldUser(OldSysUserDo oldUser) {
        SysUser newUser = new SysUser();

        // 基础信息映射
        newUser.setUserId(Long.valueOf(oldUser.getId()));
        newUser.setUserName(oldUser.getOaAccount());
        newUser.setNickName(oldUser.getNameCh());
        newUser.setPassword(oldUser.getPassword());
        newUser.setEmail(oldUser.getEmail());
        newUser.setPhonenumber(oldUser.getRelationTel());
        newUser.setDeptId(Long.valueOf(oldUser.getOrgId()));

        // 系统默认设置
        newUser.setUserType(UserType.SYS_USER.getUserType()); // 设置为系统用户类型
        newUser.setStatus("0");          // 默认启用状态
        newUser.setDelFlag("0");       // 默认未删除

        // 审计信息
        newUser.setCreateBy("system_sync");
        newUser.setUpdateBy("system_sync");

        return newUser;
    }
}
