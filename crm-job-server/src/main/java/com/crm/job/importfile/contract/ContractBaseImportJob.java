package com.crm.job.importfile.contract;


import cn.hutool.extra.ssh.Sftp;
import com.crm.common.enums.DateTypeEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.domain.TaskParams;
import com.crm.job.base.AbstractImportJob;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.bo.ZqztDwdContractsBo;
import com.crm.system.mapper.ZqztDwdContractsMapper;
import com.jcraft.jsch.ChannelSftp;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.crm.system.utils.Constants.JF_COMMON_FTP;


/**
 * 合同文件拉取解析入库任务
 *
 * @author tjx
 * @date 2026-03-05
 */
@Slf4j
@Component
public class ContractBaseImportJob extends AbstractImportJob<ZqztDwdContractsBo, Integer> {

    // 文件分隔符
    private static final String FIELD_SEPARATOR = "\u001B";
    // 校验文件字段数量（导入时校验字段数量，字段数量不足认为脏数据，跳过该条）
    private static final int FIXED_FIELD_COUNT = 55;


    private static final String FILE_DATE_FORMAT = "yyyyMMdd";
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd";
    private static final String FIELD_EXTENSION = "txt";
    private static final int BATCH_SIZE = 1000;



    // 文件列表前缀 & 类型标识
    private static final List<FileConfig> FILE_CONFIGS = new ArrayList<FileConfig>() {{
        add(new FileConfig("DWD_CONTRACTS_D", "CU"));
        add(new FileConfig("DWD_CONTRACTS_JC_D", "JC"));
        add(new FileConfig("DWD_CONTRACTS_YSJ_D", "YSJ"));
    }};


    @Resource
    private ZqztDwdContractsMapper contractsMapper;

    @Resource
    private SftpUtil sftpUtil;

    @Override
    @XxlJob("contractBaseImport")
    public void execute() {
        super.execute();
    }

    @Override
    protected TaskParams parseTaskParams() {
        // 文件名前缀列表
        List<String> fileNameList = new ArrayList<>();
        for (FileConfig fileConfig : FILE_CONFIGS) {
            String fileNamePrefix = fileConfig.getFileNamePrefix();
            fileNameList.add(fileNamePrefix);
        }

        // 配置任务参数
        TaskParams params = new TaskParams();

        // 文件名前缀列表
        params.setFileNameList(fileNameList);
        // 文件名类型后缀
        params.setExtension(FIELD_EXTENSION);
        // sftp 配置类型
        params.setSftpConfigType(JF_COMMON_FTP);
        // 批量提交条数
        params.setBatchSize(BATCH_SIZE);
        // 文件账期类型
        params.setDateType(DateTypeEnum.DAY.getCode());

        return params;
    }

    @Override
    protected void clearTargetTable() {
        log.info("开始清空合同表 zqzt_dwd_contracts_d");
        for (FileConfig config : FILE_CONFIGS) {
            // 按类型删除数据（先不用truncate清表），避免三个文件不同时推，方便拆成三个任务。
            int cnt = contractsMapper.deleteByStorageType(config.getStorageType());
            log.info("清空 storage_type={} 数据 {} 条", config.getStorageType(), cnt);
        }
    }

    @Override
    protected void processFileInBatches(TaskParams params) {

        Date etl_date = DateUtils.getNowDate();

        List<String> fileNameList = params.getFileNameList();

        log.info("开始处理文件列表：{}", params.getFilePathList());

        FileConfigMapping mapping = new FileConfigMapping();

        // 遍历文件列表
        for (String fileNamePrefix : fileNameList) {

            // 拼接完整文件名
            StringBuffer filePath = new StringBuffer();
            filePath.append(params.getDataRootPath());
            filePath.append(params.getTargetDate());
            filePath.append(params.isHasPathDate() ? "/" : "");
            filePath.append(fileNamePrefix);
            filePath.append(params.isHasSuffixDate() ? "_" : "");
            filePath.append(params.getSuffixDate());
            filePath.append(StringUtils.isNotBlank(params.getExtension())  ? "." : "" );
            filePath.append(params.getExtension());

            FileConfig config = mapping.getConfigByPrefix(fileNamePrefix);

            log.info("开始处理文件：{}，对应存储类型：{}，文件日期：{}", filePath, config.getStorageType(), params.getSuffixDate());
            // 处理单个文件，解析入库
            processSingleFile(filePath.toString(), config, params.getBatchSize(), params.getFileEtlCycle(),etl_date);
        }
        log.info("所有文件处理完成，共处理 {} 个文件", fileNameList.size());
    }

    @Override
    protected void syncToTargetTable() {
        log.info("数据直接入库，无需同步");
    }


    /**
     * 单文件处理
     */
    private void processSingleFile(String path, FileConfig config, int batchSize, Date fileDate,Date etl_date) {
        Sftp sftp = null;
        long total = 0;
        long success = 0;
        long fail = 0;
        List<ZqztDwdContractsBo> batchList = new ArrayList<>(batchSize);

        try {
            sftpUtil.setActiveConfigType(JF_COMMON_FTP);
            sftp = sftpUtil.getSftp();
            if (sftp == null) {
                log.error("获取SFTP连接失败，无法处理文件：{}", path);
                return;
            }

            ChannelSftp channel = sftp.getClient();
            // 批量入库
            try (InputStream is = channel.get(path);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    total++;
                    try {
                        ZqztDwdContractsBo bo = parseLine(line, total, config, fileDate,etl_date);
                        if (bo != null) {
                            batchList.add(bo);
                            success++;
                            if (batchList.size() >= batchSize) {
                                batchInsert(batchList);
                                batchList.clear();
                            }
                        } else {
                            fail++;
                        }
                    } catch (Exception e) {
                        fail++;
                        log.error("第{}行解析失败：{}", total, e.getMessage());
                    }
                }

                if (!batchList.isEmpty()) {
                    batchInsert(batchList);
                }

                log.info("文件{} 处理完成 总行数{} 成功{} 失败{}", path, total, success, fail);

            } catch (Exception e) {
                log.error("文件读取/解析异常：{}", path, e);
            }

        } catch (Exception e) {
            log.error("处理文件前置操作异常：{}", path, e);
        } finally {
            if (sftp != null) {
                try {
                    sftpUtil.close(sftp);
                    log.debug("SFTP连接已关闭，文件：{}", path);
                } catch (Exception e) {
                    log.error("关闭SFTP连接失败，文件：{}", path, e);
                }
            }
        }
    }

    private ZqztDwdContractsBo parseLine(String line, long lineNum, FileConfig config, Date fileDate, Date etl_date) {
        if (StringUtils.isEmpty(line)) {
            log.warn("第{}行数据为空，跳过", lineNum);
            return null;
        }

        String[] fields = line.split(FIELD_SEPARATOR);
        if (fields.length < FIXED_FIELD_COUNT) {
            log.warn("第{}行字段不足，需要{}实际{}", lineNum, FIXED_FIELD_COUNT, fields.length);
            return null;
        }

        ZqztDwdContractsBo bo = new ZqztDwdContractsBo();

        try {
            bo.setStorageType(config.getStorageType());
            bo.setStorageTime(etl_date);
            bo.setCustumerCode(get(fields, 0));
            bo.setAreaName(get(fields, 1));
            bo.setCountyCode(get(fields, 2));
            bo.setCountyName(get(fields, 3));
            bo.setContractName(get(fields, 4));
            bo.setArrairNo(get(fields, 5));
            bo.setContractNoLocal(get(fields, 6));
            bo.setDeptName(get(fields, 7));
            bo.setTakerName(get(fields, 8));
            bo.setTakerUserName(get(fields, 9));
            bo.setTakerPhone(get(fields, 10));
            bo.setArrairAmount(get(fields, 11));
            bo.setFinanceVatRate(get(fields, 12));
            bo.setFinanceUnVatAmount(get(fields, 13));
            bo.setRepositoryNameUnicom(get(fields, 14));
            bo.setRepositoryNameVendor(get(fields, 15));
            bo.setContractType(get(fields, 16));
            bo.setApprovalDate(get(fields, 17));
            bo.setEnterpertime(get(fields, 18));
            bo.setContractStartDate(get(fields, 19));
            bo.setContractEndDate(get(fields, 20));
            bo.setContractTimelimitName(get(fields, 21));
            bo.setContractStatusName(get(fields, 22));
            bo.setPerformDeptName(get(fields, 23));
            bo.setPerformer(get(fields, 24));
            bo.setPerformerUserName(get(fields, 25));
            bo.setPerformerPhone(get(fields, 26));
            bo.setMainPerformer(get(fields, 27));
            bo.setMainPerformerUserName(get(fields, 28));
            bo.setMainPerformerPhone(get(fields, 29));
            bo.setRecAmount(get(fields, 30));
            bo.setActRecAmount(get(fields, 31));
            bo.setProjectNum(get(fields, 32));
            bo.setProjectName(get(fields, 33));
            bo.setContractChangetypeName(get(fields, 34));
            bo.setEnterperformtime(get(fields, 35));
            bo.setHisContract(get(fields, 36));
            bo.setSignType(get(fields, 37));
            bo.setContractPaymentTypeName(get(fields, 38));
            bo.setUnitName(get(fields, 39));
            bo.setPayPeriod(get(fields, 40));
            bo.setIndustryProperty(get(fields, 41));
            bo.setCreatedBy(get(fields, 42));
            bo.setCreationDate(parseDate(get(fields, 43)));
            bo.setLastUpdatedBy(get(fields, 44));
            bo.setLastUpdateDate(parseDate(get(fields, 45)));
            bo.setEtlDataCycle(fileDate);
            bo.setEtlTime(etl_date);
            bo.setFixedAmount(get(fields, 46));
            bo.setOldArrairNo(get(fields, 47));
            bo.setFinalApprover(get(fields, 48));
            bo.setProcessdefname(get(fields, 49));
            bo.setOldSignDate(get(fields, 50));
            bo.setChoseTxt(get(fields,51));
            bo.setContractTypeCode("CU".equals(config.getStorageType()) ? get(fields,54) : get(fields,53));
            return bo;

        } catch (Exception e) {
            log.error("第{}行解析异常：{}", lineNum, e.getMessage());
            return null;

        }
    }

    private void batchInsert(List<ZqztDwdContractsBo> list) {
        if (list.isEmpty()) return;
        try {
            int insertCount = contractsMapper.batchInsert(list);
            log.info("批量入库{}条数据，成功{}条", list.size(), insertCount);
        } catch (Exception e) {
            log.error("批量入库失败，降级为单条插入：{}", e.getMessage());
            int success = 0;
            for (ZqztDwdContractsBo bo : list) {
                try {
                    contractsMapper.insert(bo);
                    success++;
                } catch (Exception ex) {
                    log.error("单条入库失败（合同编号：{}）：{}", bo.getArrairNo(), ex.getMessage());
                }
            }
            log.info("单条入库完成，成功{}条，失败{}条", success, list.size() - success);
        }
    }

    private String get(String[] arr, int i) {
        return i < arr.length ? arr[i].trim() : "";
    }

    private Date parseDate(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return s.length() == 8 ? DateUtils.dateTime(FILE_DATE_FORMAT,s) : DateUtils.dateTime(DB_DATE_FORMAT,s);
    }

    @Getter
    @AllArgsConstructor
    private static class FileConfig {
        private final String fileNamePrefix;
        private final String storageType;
    }

    private class FileConfigMapping {
        private final Map<String, FileConfig> configMap;

        public FileConfigMapping() {
            configMap = new HashMap<>();
            for (FileConfig config : FILE_CONFIGS) {
                configMap.put(config.getFileNamePrefix(), config);
            }
        }
        public FileConfig getConfigByPrefix(String prefix) {
            return configMap.get(prefix);
        }


    }
}
