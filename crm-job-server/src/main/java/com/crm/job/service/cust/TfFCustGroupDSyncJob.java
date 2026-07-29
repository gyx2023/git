package com.crm.job.service.cust;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.base.AbstractFtpImportJob;
import com.crm.job.utils.GroupFileParser;
import com.crm.system.domain.dao.*;
import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.enums.CustStateEnum;
import com.crm.system.mapper.ZqGrpGroupInfoMapper;
import com.crm.system.mapper.ZqGrpGroupLogMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.service.TfFCustGroupDService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集团实体客户全量日数据同步定时任务
 * 从SFTP读取客户分组文件并导入到tf_f_cust_group_d表
 *
 * @author crm
 */
@Slf4j
@Component
public class TfFCustGroupDSyncJob extends AbstractFtpImportJob<TfFCustGroupDDo> {

    /**
     * 批量处理大小
     */
    private static final int DEFAULT_BATCH_SIZE = 500;
    /**
     * 集团级别标识
     */
    private static final String GRP_LEVEL_IDENTIFIER = "Y";
    /**
     * 生成集团编码的最大重试次数
     */
    private static final int GENERATE_GRP_CODE_MAX_ATTEMPTS = 10;
    /**
     * 集团编码长度
     */
    private static final int GRP_CODE_LENGTH = 15;
    private static final String LOG_UPDATE_TYPE = "auto";
    /**
     * 变更类型 - 变更前
     */
    private static final String BEFORE_LOG_DATA_TYPE = "1";
    /**
     * 变更类型 - 变更前
     */
    private static final String AFTER_LOG_DATA_TYPE = "2";

    @Resource private GroupFileParser groupFileParser;
    @Resource private TfFCustGroupDService dwdDCusZqztCustGrouService;
    @Resource private SysAreaMapper sysAreaMapper;
    @Resource private ZqGrpGroupInfoMapper zqGrpGroupInfoMapper;
    @Resource private ZqGrpGroupLogMapper zqGrpGroupLogMapper;

    private final Map<String, String> cityMap = new HashMap<>();
    private final Map<String, String> custStateMap = new HashMap<>();
    private volatile boolean cityListInitialized;

    @Override protected String getJobName() { return "tfFCustGroupDSyncJobHandler"; }
    @Override protected String getConfigType() { return "jf_common_ftp"; }
    @Override protected String getFileDateType() { return "D"; }
    @Override protected String getSeparator() { return "\\x1b"; }
    @Override protected int getBatchSize() { return DEFAULT_BATCH_SIZE; }

    @Override
    protected String getFileName(String dateStr) {
        return "/DWD_D_CUS_ZQZT_CUST_GROUP_" + dateStr + ".txt";
    }

    @Override protected TfFCustGroupDDo parseRow(String[] cols) { return null; }

    @Override
    protected TfFCustGroupDDo parseRowFromLine(String line) {
        return groupFileParser.parseLine(line, TfFCustGroupDDo.class, getSeparator());
    }

    @Override
    protected void preProcess(TfFCustGroupDDo entity) {
        ensureCityMapLoaded();
        if (entity.getEparchyCode() != null) {
            entity.setEparchyCode(cityMap.getOrDefault(entity.getEparchyCode(), entity.getEparchyCode()));
        }
        if (entity.getBCityCode() != null) {
            entity.setBCityCode(cityMap.getOrDefault(entity.getBCityCode(), entity.getBCityCode()));
        }
    }

    @Override
    protected int doInsert(List<TfFCustGroupDDo> batch) {
        return dwdDCusZqztCustGrouService.batchInsert(batch);
    }

    @Override
    protected void beforeImport() {
        dwdDCusZqztCustGrouService.truncateTable();
    }

    @Override
    protected void afterImport(int totalCount) {
        String createDate = currentDateStr.substring(0, 4) + "-" + currentDateStr.substring(4, 6) + "-" + currentDateStr.substring(6, 8);
        List<TfFCustGroupDDo> list = dwdDCusZqztCustGrouService.getTfFCustGroupDList(createDate);
        processGrpGroupInfo(list, createDate);
    }

    @XxlJob("tfFCustGroupDSyncJobHandler")
    public void start() { execute(); }

    // ==================== 以下为原有逻辑，保持不变 ====================

    private void ensureCityMapLoaded() {
        if (cityListInitialized) {
            return;
        }
        synchronized (this) {
            if (cityListInitialized) {
                return;
            }
            for (SysArea a : sysAreaMapper.selectList()) {
                if (a.getJtCode() != null) {
                    cityMap.put(a.getJtCode(), String.valueOf(a.getId()));
                }
            }
            custStateMap.put(CustStateEnum.CUST_STATE_0.getCode(), CustStateEnum.CUST_STATE_0.getLabel());
            custStateMap.put(CustStateEnum.CUST_STATE_1.getCode(), CustStateEnum.CUST_STATE_1.getLabel());
            custStateMap.put(CustStateEnum.CUST_STATE_2.getCode(), CustStateEnum.CUST_STATE_2.getLabel());
            custStateMap.put(CustStateEnum.CUST_STATE_3.getCode(), CustStateEnum.CUST_STATE_3.getLabel());
            custStateMap.put(CustStateEnum.CUST_STATE_4.getCode(), CustStateEnum.CUST_STATE_4.getLabel());
            cityListInitialized = true;
        }
    }

    private void processGrpGroupInfo(List<TfFCustGroupDDo> tfFCustGroupDList, String createDate) {
        log.info("存入政企集团客户信息开始");
        XxlJobHelper.log("存入政企集团客户信息开始");

        List<ZqGrpGroupInfo> groupInfoList = new ArrayList<>();
        int batchSize = DEFAULT_BATCH_SIZE, totalProcessed = 0, totalSuccess = 0, batchCount = 0, successBatchCount = 0, failBatchCount = 0;

        for (TfFCustGroupDDo custGroup : tfFCustGroupDList) {
            ZqGrpGroupInfo newGroup = new ZqGrpGroupInfo();
            newGroup.setGrpName(custGroup.getCustName());
            newGroup.setGrpCode(generateUniqueGrpCode());
            newGroup.setHqGrpCode(custGroup.getGroupId());
            newGroup.setCustId(custGroup.getProvinceCustId());
            newGroup.setGrpStatus(custStateMap.get(custGroup.getCustState()));
            newGroup.setAreaId(custGroup.getEparchyCode());
            newGroup.setCountyId(custGroup.getBCityCode());
            newGroup.setIndustryMajorKind(custGroup.getFirstCallingTypeCode());
            newGroup.setIndustrySubKind(custGroup.getSecondCallingTypeCode());
            newGroup.setIndustryDetailKind(custGroup.getThirdCallingTypeCode());
            newGroup.setGrpCustType(custGroup.getCustClassType());
            newGroup.setGrpLevel(CustStateEnum.CUST_STATE_2.getCode().equals(custGroup.getCustState()) ? GRP_LEVEL_IDENTIFIER : null);
            newGroup.setGrpCreator(custGroup.getCreateOaUserCode());
            if (StringUtils.isNotBlank(custGroup.getCreateDate())) {
                try { newGroup.setGrpCreateTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, custGroup.getCreateDate())); } catch (Exception ignored) {}
            }
            newGroup.setCreateTime(DateUtils.getNowDate());
            newGroup.setUpdateTime(DateUtils.getNowDate());
            groupInfoList.add(newGroup);
            totalProcessed++;

            if (groupInfoList.size() >= batchSize) {
                batchCount++;
                try {
                    if (zqGrpGroupInfoMapper.insertBatch(groupInfoList)) {
                        successBatchCount++; totalSuccess += groupInfoList.size();
                        batchInsertGroupLogForNewRecords(groupInfoList);
                    } else { failBatchCount++; }
                } catch (Exception e) {
                    failBatchCount++; log.error("批次插入异常", e);
                }
                groupInfoList.clear();
            }
        }
        if (!groupInfoList.isEmpty()) {
            batchCount++;
            int size = groupInfoList.size();
            try {
                if (zqGrpGroupInfoMapper.insertBatch(groupInfoList)) {
                    successBatchCount++;
                    totalSuccess += size;
                    batchInsertGroupLogForNewRecords(groupInfoList);
                } else {
                    failBatchCount++;
                }
            } catch (Exception e) {
                failBatchCount++;
                log.error("最后批次插入异常", e);
            }
        }
        log.info("政企集团客户信息完成：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功导入数={}",
            totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess);
        XxlJobHelper.log("数据处理统计：处理总条数={}，总批次={}，成功批次={}，失败批次={}，成功导入数={}",
            totalProcessed, batchCount, successBatchCount, failBatchCount, totalSuccess);

        List<ZqGrpGroupInfo> updList = zqGrpGroupInfoMapper.getUpdQuery(createDate);
        if (updList != null && !updList.isEmpty()) {
            batchInsertGroupLogForUpdateBefore(updList);
            if (zqGrpGroupInfoMapper.updateBatchById(updList)) {
                log.info("更新集团级别与状态完成: {}", updList.size());
                batchInsertGroupLog(updList, AFTER_LOG_DATA_TYPE);
            }
        }
    }

    private void batchInsertGroupLogForNewRecords(List<ZqGrpGroupInfo> groupInfoList) {
        try {
            List<String> codes = groupInfoList.stream().map(ZqGrpGroupInfo::getHqGrpCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            if (codes.isEmpty()) {
                return;
            }
            List<ZqGrpGroupInfo> inserted = zqGrpGroupInfoMapper.selectList(new LambdaQueryWrapper<ZqGrpGroupInfo>().in(ZqGrpGroupInfo::getHqGrpCode, codes));
            if (!inserted.isEmpty()) {
                batchInsertGroupLog(inserted, AFTER_LOG_DATA_TYPE);
            }
        } catch (Exception e) { log.error("日志写入失败(新增)", e); }
    }

    private void batchInsertGroupLogForUpdateBefore(List<ZqGrpGroupInfo> updList) {
        try {
            List<Long> ids = updList.stream().map(ZqGrpGroupInfo::getGrpId).filter(Objects::nonNull).collect(Collectors.toList());
            if (ids.isEmpty()) {
                return;
            }
            List<ZqGrpGroupInfo> before = zqGrpGroupInfoMapper.selectBatchIds(ids);
            if (!before.isEmpty()) {
                batchInsertGroupLog(before, BEFORE_LOG_DATA_TYPE);
            }
        } catch (Exception e) { log.error("日志写入失败(更新前)", e); }
    }

    private void batchInsertGroupLog(List<ZqGrpGroupInfo> list, String dataType) {
        try {
            List<ZqGrpGroupLog> logs = new ArrayList<>(list.size());
            Date now = DateUtils.getNowDate();
            for (ZqGrpGroupInfo info : list) {
                ZqGrpGroupLog l = BeanUtil.toBean(info, ZqGrpGroupLog.class);
                l.setUpdateType(LOG_UPDATE_TYPE);
                l.setDataType(dataType);
                l.setCreateTime(now);
                l.setUpdateTime(now);
                logs.add(l);
            }
            zqGrpGroupLogMapper.insertBatch(logs);
        } catch (Exception e) { log.error("日志写入失败", e); }
    }

    private String generateUniqueGrpCode() {
        for (int i = 0; i < GENERATE_GRP_CODE_MAX_ATTEMPTS; i++) {
            String code = generateRandomGrpCode();
            if (zqGrpGroupInfoMapper.selectCount(new LambdaQueryWrapper<ZqGrpGroupInfo>().eq(ZqGrpGroupInfo::getGrpCode, code)) == 0) {
                return code;
            }
        }
        throw new RuntimeException("无法生成唯一的grpCode");
    }

    private String generateRandomGrpCode() {
        Random r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRP_CODE_LENGTH; i++) {
            sb.append(r.nextInt(GENERATE_GRP_CODE_MAX_ATTEMPTS));
        }
        return sb.toString();
    }
}
