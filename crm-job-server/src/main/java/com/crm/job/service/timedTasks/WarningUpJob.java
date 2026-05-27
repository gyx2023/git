package com.crm.job.service.timedTasks;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.crm.system.domain.DwdRepGwPstD;
import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.service.DwdRepGwPstDService;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 升级预警定时任务
 */
@Slf4j
@Component
public class WarningUpJob {

    @Resource
    private DwdRepGwPstDService dwdRepGwPstDService;

    @Resource
    private IDwdRepSysPersonDService dwdRepSysPersonDService;

    /**
     * 同步管理中台在职人员信息（person_new）
     * 升级预警推送时间，每月3号
     * 同步经分数据：
     * - DWD_REP_GW_PST_D（人员职位表）
     * - DWD_REP_SYS_PERSON_D（人员信息表）
     */
    @XxlJob(value = "warningUpJobDataPull")
    public void warningUpJobDataPull() {
        long startTime = System.currentTimeMillis();
        log.info("升级预警接口 数据同步 开始");
        XxlJobHelper.log("升级预警接口 数据同步 开始");

        try {
            // DWD_REP_GW_PST_D表：数据获取与入库
            syncDwdRepGwPstD();

            // DWD_REP_SYS_PERSON_D表：数据获取与入库
            syncDwdRepSysPersonD();

            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            log.info("升级预警接口 数据同步 完成,耗时:{}ms", costTime);
            XxlJobHelper.log("升级预警接口 数据同步 完成,耗时:{}ms", costTime);
        } catch (Exception e) {
            log.error("升级预警接口 数据同步 异常", e);
            XxlJobHelper.log("升级预警接口 数据同步 异常:{}", e.getMessage());
        }
    }

    /**
     * 同步DWD_REP_GW_PST_D表数据
     */
    private void syncDwdRepGwPstD() {
        log.info("开始同步DWD_REP_GW_PST_D表数据");
        XxlJobHelper.log("开始同步DWD_REP_GW_PST_D表数据");

        // 从IOB数据库查询数据
        List<Map<String, Object>> dwdRepGwPstMapList = dwdRepGwPstDService.getDwdRepGwPstListFromIob();

        if (CollUtil.isNotEmpty(dwdRepGwPstMapList)) {
            // 清空本地表数据
            dwdRepGwPstDService.truncate();
            log.info("已清空dwd_rep_gw_pst_d表数据");

            // 分批插入数据，每批100条
            int count = 0;
            List<DwdRepGwPstD> gwPstDList = new ArrayList<>();
            for (Map<String, Object> stringObjectMap : dwdRepGwPstMapList) {
                count++;
                DwdRepGwPstD gwPstDEntity = JSON.parseObject(JSON.toJSONString(stringObjectMap), DwdRepGwPstD.class);
                gwPstDList.add(gwPstDEntity);

                // 每100条插入一次
                if (count % 100 == 0) {
                    dwdRepGwPstDService.insertBatch(gwPstDList);
                    log.info("已插入{}条DWD_REP_GW_PST_D数据", count);
                    gwPstDList = new ArrayList<>();
                }
            }

            // 插入剩余数据
            if (CollUtil.isNotEmpty(gwPstDList)) {
                dwdRepGwPstDService.insertBatch(gwPstDList);
                log.info("已插入剩余{}条DWD_REP_GW_PST_D数据", gwPstDList.size());
            }

            log.info("DWD_REP_GW_PST_D表数据同步完成，共{}条", dwdRepGwPstMapList.size());
            XxlJobHelper.log("DWD_REP_GW_PST_D表数据同步完成，共{}条", dwdRepGwPstMapList.size());
        } else {
            log.info("DWD_REP_GW_PST_D表无数据需要同步");
            XxlJobHelper.log("DWD_REP_GW_PST_D表无数据需要同步");
        }
    }

    /**
     * 同步DWD_REP_SYS_PERSON_D表数据
     */
    private void syncDwdRepSysPersonD() {
        log.info("开始同步DWD_REP_SYS_PERSON_D表数据");
        XxlJobHelper.log("开始同步DWD_REP_SYS_PERSON_D表数据");

        // 查询总记录数
        int personCount = dwdRepSysPersonDService.getPersonCountFromIob();
        log.info("DWD_REP_SYS_PERSON_D表总记录数:{}", personCount);

        if (personCount > 10000) {
            // 清空本地表数据
            dwdRepSysPersonDService.truncate();
            log.info("已清空dwd_rep_sys_person_d表数据");

            // 计算循环次数，每次处理10000条
            int cycleNum;
            if (personCount % 10000 == 0) {
                cycleNum = personCount / 10000;
            } else {
                cycleNum = personCount / 10000 + 1;
            }

            log.info("需要分{}次处理，每次10000条", cycleNum);
            XxlJobHelper.log("需要分{}次处理，每次10000条", cycleNum);

            for (int i = 1; i <= cycleNum; i++) {
                log.info("开始处理第{}/{}批次", i, cycleNum);

                // 分页查询数据，每次10000条
                List<Map<String, Object>> dwdRepSysPersonMapList = dwdRepSysPersonDService.getDwdRepSysPersonListFromIob(i, 10000);

                if (CollUtil.isEmpty(dwdRepSysPersonMapList)) {
                    log.info("第{}批次无数据，跳过", i);
                    continue;
                }

                // 分批插入数据，每批100条
                int count = 0;
                List<DwdRepSysPersonD> personDList = new ArrayList<>();
                for (int j = 0; j < dwdRepSysPersonMapList.size(); j++) {
                    count++;
                    Map<String, Object> map = dwdRepSysPersonMapList.get(j);
                    DwdRepSysPersonD personDEntity = JSON.parseObject(JSON.toJSONString(map), DwdRepSysPersonD.class);
                    personDList.add(personDEntity);

                    // 每100条插入一次
                    if (count % 100 == 0) {
                        dwdRepSysPersonDService.insertBatch(personDList);
                        log.info("第{}批次已插入{}条数据", i, count);
                        personDList = new ArrayList<>();
                    }
                }

                // 插入剩余数据
                if (CollUtil.isNotEmpty(personDList)) {
                    dwdRepSysPersonDService.insertBatch(personDList);
                    log.info("第{}批次插入剩余{}条数据", i, personDList.size());
                }

                log.info("第{}批次处理完成，共{}条", i, dwdRepSysPersonMapList.size());
            }

            log.info("DWD_REP_SYS_PERSON_D表数据同步完成，共{}条", personCount);
            XxlJobHelper.log("DWD_REP_SYS_PERSON_D表数据同步完成，共{}条", personCount);
        } else {
            log.info("DWD_REP_SYS_PERSON_D表记录数小于等于10000，不需要同步");
            XxlJobHelper.log("DWD_REP_SYS_PERSON_D表记录数小于等于10000，不需要同步");
        }
    }
}
