package com.crm.job.service.failureSysUser;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.crm.common.utils.DateUtils;
import com.crm.system.domain.dao.SysUserOperateLog;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.SysUserOperateLogMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;


/**
 * 失效超时未登录账号定时任务
 *
 * @author crm
 */
@Slf4j
@Component
public class FailureSysUserSyncJob {

    /**
     * 默认超时未登录天数
     */
    private static final int FAILURE_DAYS = 90;
    /**
     * XXL传递超时未登录天数
     */
    private static final String FAILURE_DAYS_NAME = "failureDays";
    /**
     * 批量处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 500;
    /**
     * 最小超时天数
     */
    private static final Integer MIN_FAILURE_DAYS = 1;
    /**
     * 操作人员
     */
    private static final Integer OPERATOR = 2;
    /**
     * 操作类型
     */
    private static final String OPERATE_TYPE_MOD = "del";
    /**
     * 失效用户类型
     */
    private static final String IS_VALID_FAILURE = "0";
    /**
     * 用户超时备注信息
     */
    private static final String USER_FAILURE_REMARK = "长期不登录账号清理";

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private SysUserOperateLogMapper sysUserOperateLogMapper;

    /**
     * 失效超时未登录账号定时任务
     * 在xxl-job调度中心配置JobHandler为：failureSysUserSyncJobHandler
     * 支持传入参数格式：
     * 1. 仅超时未登录天数：failureDays: - 指定超时未登录天数 - 例：90
     * 参数样例：{"failureDays": "90"}
     */
    @XxlJob("failureSysUserSyncJobHandler")
    public void execute() {
        log.info("开始执行失效超时未登录账号定时任务");
        XxlJobHelper.log("开始执行失效超时未登录账号定时任务");

        // 任务参数
        int failureDays = FAILURE_DAYS; // 默认超时未登录天数

        // 获取定时任务传入参数
        String jobStr = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(jobStr)) {
            JSONObject json = JSONUtil.parseObj(jobStr);

            // 指定超时未登录天数
            Integer failureDaysInt = json.getInt(FAILURE_DAYS_NAME);
            if (ObjectUtil.isNotNull(failureDaysInt) && failureDaysInt >= MIN_FAILURE_DAYS) {
                failureDays = failureDaysInt;
                log.info("使用指定的超时未登录天数: {}", failureDaysInt);
            }
        }
        log.info("数据参数：1.超时未登录天数：{}", failureDays);
        XxlJobHelper.log("数据参数：1.超时未登录天数：{}", failureDays);


        try {
            int count = 0;
            int totalProcessed = 0; // 总处理记录数
            int totalSuccess = 0; // 总成功失效用户数
            int batchCount = 0; // 批次计数
            int successBatchCount = 0; // 成功批次计数
            int failBatchCount = 0; // 失败批次计数
            int logTotalProcessed = 0; // 日志总处理记录数
            int logTotalSuccess = 0; // 日志总成功失效用户数
            int batchSize = DEFAULT_BATCH_SIZE; // 每批处理数量
            List<SysUsers> sysUserList = new ArrayList<>();
            List<SysUserOperateLog> sysUserOperateLogList = new ArrayList<>();

            // 1. 获取超时未登录用户数据
            List<SysUsers> sysUsers = sysUsersMapper.selectFailureList(failureDays);

            // 2. 检查超时未登录用户是否存在
            if (CollectionUtils.isEmpty(sysUsers)) {
                String msg = "暂无超时未登录用户";
                log.info(msg);
                XxlJobHelper.handleSuccess(msg);
                return;
            }

            // 3.失效超时未登录账号
            for (SysUsers user : sysUsers) {
                Date nowDate = DateUtils.getNowDate();
                SysUserOperateLog sysUserOperateLog = getSysUserOperateLog(user, nowDate);

                user.setIsValid(IS_VALID_FAILURE);
                user.setUpdateTime(nowDate);
                user.setRemark(USER_FAILURE_REMARK);


                // 保存sysUser信息到List
                sysUserList.add(user);
                // 保存sysUserOperateLog信息到List
                sysUserOperateLogList.add(sysUserOperateLog);

                count++;
                totalProcessed++;
                logTotalProcessed++;

                // 每500条执行一次批量更新
                if (count % batchSize == 0) {
                    batchCount++;
                    log.info("第 {} 批次开始批量失效超时未登录用户信息，本次处理 {} 条", batchCount, batchSize);
                    XxlJobHelper.log("第 {} 批次开始批量失效超时未登录用户信息，本次处理 {} 条", batchCount, batchSize);

                    try {
                        boolean batchSuccess = sysUsersMapper.updateBatchById(sysUserList);
                        if (batchSuccess) {
                            successBatchCount++;
                            totalSuccess += batchSize; // 假设全部成功
                            log.info("批次处理完成，当前已处理 {} 条记录，成功失效用户 {} 条", totalProcessed, totalSuccess);
                            XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功失效用户 {} 条", totalProcessed, totalSuccess);

                            try {
                                logTotalSuccess += batchSize;
                                // 批量插入用户操作日志表
                                boolean insertBatch = sysUserOperateLogMapper.insertBatch(sysUserOperateLogList);
                                if (insertBatch) {
                                    log.info("批次处理完成，当前已处理 {} 条记录，成功插入失效用户日志 {} 条", logTotalProcessed, logTotalSuccess);
                                    XxlJobHelper.log("批次处理完成，当前已处理 {} 条记录，成功插入失效用户日志 {} 条", logTotalProcessed, logTotalSuccess);
                                } else {
                                    log.error("第 {} 批次批量插入日志失败，本次处理 {} 条", batchCount, logTotalSuccess);
                                    XxlJobHelper.log("第 {} 批次批量插入日志失败，本次处理 {} 条", batchCount, logTotalSuccess);
                                }
                            } catch (Exception e) {
                                // 批量插入日志失败
                                log.error("第 {} 批次批量插入日志异常，错误信息:{}", batchCount, e.getMessage(), e);
                                XxlJobHelper.log("第 {} 批次批量插入日志异常，错误信息:{}", batchCount, e.getMessage());
                            }

                        } else {
                            // 批量更新失败
                            failBatchCount++;
                            log.error("第 {} 批次批量更新失败，本次处理 {} 条", batchCount, batchSize);
                            XxlJobHelper.log("第 {} 批次批量更新失败，本次处理 {} 条", batchCount, batchSize);
                        }
                    } catch (Exception e) {
                        failBatchCount++;
                        log.error("第 {} 批次批量更新异常，错误信息:{}", batchCount, e.getMessage(), e);
                        XxlJobHelper.log("第 {} 批次批量更新异常，错误信息:{}", batchCount, e.getMessage());
                    }
                    sysUserList.clear(); // 清空list，准备下一批
                    sysUserOperateLogList.clear(); // 清空list，准备下一批
                }
            }

            // 处理剩余不足500条的数据
            if (!sysUserList.isEmpty()) {
                batchCount++;
                int lastBatchSize = sysUserList.size();
                int lastLogBatchSize = sysUserOperateLogList.size();
                log.info("第 {} 批次(最后批次)开始批量失效超时未登录用户信息，本次处理剩余 {} 条", batchCount, lastBatchSize);
                XxlJobHelper.log("第 {} 批次(最后批次)开始批量失效超时未登录用户信息，本次处理剩余 {} 条", batchCount, lastBatchSize);

                try {
                    boolean batchSuccess = sysUsersMapper.updateBatchById(sysUserList);
                    if (batchSuccess) {
                        successBatchCount++;
                        totalSuccess += lastBatchSize; // 假设全部成功
                        log.info("最后批次处理完成，本次处理 {} 条记录，成功失效用户日志 {} 条", lastBatchSize, lastBatchSize);
                        XxlJobHelper.log("最后批次处理完成，本次处理 {} 条记录，成功失效用户日志 {} 条", lastBatchSize, lastBatchSize);


                        try {
                            logTotalSuccess += lastLogBatchSize;
                            // 批量插入用户操作日志表
                            boolean insertBatch = sysUserOperateLogMapper.insertBatch(sysUserOperateLogList);
                            if (insertBatch) {
                                log.info("最后批次批量插入日志处理完成，当前已处理 {} 条记录，成功插入失效用户日志 {} 条", logTotalSuccess, logTotalSuccess);
                                XxlJobHelper.log("最后批次批量插入日志处理完成，当前已处理 {} 条记录，成功插入失效用户日志 {} 条", logTotalSuccess, logTotalSuccess);

                            } else {
                                log.error("最后批次批量插入失败，本次处理 {} 条", lastLogBatchSize);
                                XxlJobHelper.log("最后批次批量插入失败，本次处理 {} 条", lastLogBatchSize);
                            }
                        } catch (Exception e) {
                            // 批量插入日志失败
                            log.error("最后批次批量插入日志信息异常，错误信息:{}", e.getMessage(), e);
                            XxlJobHelper.log("最后批次批量插入日志异常，错误信息:{}", e.getMessage());
                        }

                    } else {
                        // 批量更新失败
                        failBatchCount++;
                        log.error("最后批次批量更新失败，本次处理 {} 条", lastBatchSize);
                        XxlJobHelper.log("最后批次批量更新失败，本次处理 {} 条", lastBatchSize);
                    }
                } catch (Exception e) {
                    failBatchCount++;
                    log.error("最后批次批量更新异常，错误信息:{}", e.getMessage(), e);
                    XxlJobHelper.log("最后批次批量更新异常，错误信息:{}", e.getMessage());
                }
            }

            // 4. 数据统计
            // 失效处理统计
            log.info("数据处理统计：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功失效用户数={}，成功插入失效用户日志数据={}",
                totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess, logTotalSuccess);
            XxlJobHelper.log("数据处理统计：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功失效用户数={}，成功插入失效用户日志数据={}",
                totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess, logTotalSuccess);


            // 统计总结
            String successMsg = String.format("失效超时未登录用户总计处理完成，累计处理 %d 条记录，成功失效用户 %d 条，成功插入失效用户日志 %d 条",
                totalProcessed, totalSuccess, logTotalSuccess);
            log.info(successMsg);
            XxlJobHelper.handleSuccess(successMsg);

        } catch (Exception e) {
            // 5. 处理异常
            String errorMsg = "失效超时未登录账号任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 加工失效用户日志记录信息
     *
     * @param user 失效用户
     * @param nowDate 更新时间
     * @return 失效用户日志
     */
    private SysUserOperateLog getSysUserOperateLog(SysUsers user, Date nowDate) {
        SysUserOperateLog sysUserOperateLog = new SysUserOperateLog();

        // 从SysUsers复制基本信息到SysUserOperateLog
        sysUserOperateLog.setName(user.getName());
        sysUserOperateLog.setNameCh(user.getNameCh());
        sysUserOperateLog.setRelationTel(user.getRelationTel());
        sysUserOperateLog.setType(user.getType());
        sysUserOperateLog.setOrgId(user.getOrgId());
        sysUserOperateLog.setPassword(user.getPassword());
        sysUserOperateLog.setPasswordSalt(user.getPasswordSalt());
        sysUserOperateLog.setFunctionList(user.getFunctionList());
        sysUserOperateLog.setRoleList(user.getRoleList());
        sysUserOperateLog.setRemark(user.getRemark());
        sysUserOperateLog.setCreator(user.getCreator());
        sysUserOperateLog.setCreateTime(user.getCreateTime());
        sysUserOperateLog.setModifier(user.getModifier());
        sysUserOperateLog.setModifyTime(user.getModifyTime());
        sysUserOperateLog.setIsValid(user.getIsValid());
        sysUserOperateLog.setWxUserFlag(user.getWxUserFlag());
        sysUserOperateLog.setWxPhone(user.getWxPhone());
        sysUserOperateLog.setWxNo(user.getWxNo());
        sysUserOperateLog.setWxEmail(user.getWxEmail());
        sysUserOperateLog.setWxFlag(user.getWxFlag());
        sysUserOperateLog.setStealth(user.getStealth());
        sysUserOperateLog.setCustomerNo(user.getCustomerNo());
        sysUserOperateLog.setOaAccount(user.getOaAccount());
        sysUserOperateLog.setEmail(user.getEmail());
        sysUserOperateLog.setFirstLogin(user.getFirstLogin());
        sysUserOperateLog.setUpdateTime(user.getUpdateTime());
        sysUserOperateLog.setIncludeType(user.getIncludeType());
        sysUserOperateLog.setUnicomType(user.getUnicomType());
        sysUserOperateLog.setUnicomType2(user.getUnicomType2());
        sysUserOperateLog.setData(user.getData());
        sysUserOperateLog.setToken(user.getToken());
        sysUserOperateLog.setMarkunits(user.getMarkunits());
        sysUserOperateLog.setDeveloperCodes(user.getDeveloperCodes());
        sysUserOperateLog.setCardNumber(user.getCardNumber());
        sysUserOperateLog.setStaffType(user.getStaffType());
        sysUserOperateLog.setUserRoleYx(user.getUserRoleYx());
        sysUserOperateLog.setZqbusinessFlag(user.getZqbusinessFlag());
        sysUserOperateLog.setInfoId(user.getId());

        // 操作信息
        sysUserOperateLog.setOperateTime(nowDate);  // 操作时间
        sysUserOperateLog.setOperator(OPERATOR);  // 当前操作人ID
        sysUserOperateLog.setOperateType(OPERATE_TYPE_MOD);  // 操作类型，根据实际情况修改

        return sysUserOperateLog;
    }

}
