package com.crm.job.service.file;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.dao.ContractAndOpportunityPutConfig;
import com.crm.system.domain.vo.ContractIncomeExcelVo;
import com.crm.system.domain.vo.ToppContractExcelVo;
import com.crm.system.domain.vo.topp.ToppOpportunityExcelVo;
import com.crm.system.mapper.ContractAndOpportunityPutConfigMapper;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.sys.ISysOperLogService;
import com.crm.system.service.sys.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class ContractAndOpportunityPutJob {
    private static String ftpUsername;
    private static String ftpPassword;
    private static String ftpHost;
    private static String ftpPort;
    private static String ftpDirectory;

    //文件的本地保存测试路径
    private static final String LOCAL_SAVE_PATH = "C:/Users/12139/Desktop";

    @Resource
    private ISysParamService sysParamService;
    @Resource
    private ISysCodeService sysCodeService;
    @Resource
    private ContractAndOpportunityPutConfigMapper contractAndOpportunityPutConfigMapper;
    @Resource
    private ToppContractMapper toppContractMapper;
    @Resource
    private ToppOpportunityMapper toppOpportunityMapper;

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ISysOperLogService sysOperLogService;

    @XxlJob("contractAndOpportunityPutJob")
    public void execute(){
        Map<String, String> param = sysParamService.findValueMapsByType("contract_and_opportunity_put_job");
        String jobParam = XxlJobHelper.getJobParam();
        String currentDate = (jobParam != null && !jobParam.trim().isEmpty())
            ? jobParam
            : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        sysOperLogService.tryExportCall("合同商机数据上传任务",
            "contract_and_opportunity_put_job", "currentDate=" + currentDate, () -> {
                ftpUsername = param.get("ftp_username");  //用户名
                ftpPassword = param.get("ftp_password");  //密码
                ftpHost = param.get("ftp_host");  //ip地址
                ftpPort = param.get("ftp_port");  //端口号

                List<ContractAndOpportunityPutConfig> contractAndOpportunityPutConfigs = contractAndOpportunityPutConfigMapper.selectList();

                // 一次性加载所有字典，避免每个地市重复加载
                Map<String, String> oppTypeMap = sysCodeService.getCodeMapByType("opp_type");
                Map<String, String> unicomTypeMap = sysCodeService.getCodeMapByType("unicom_type");
                Map<String, String> unicomType2Map = sysCodeService.getCodeMapByType("unicom_type_2");
                Map<String, String> unicomType2Settingstr1Map = sysCodeService.getCodeSettingStr1MapByType("unicom_type_2");
                Map<String, String> clueSourceMap = sysCodeService.getCodeMapByType("clue_source");
                Map<String, String> contractAddTypeMap = sysCodeService.getCodeMapByType("contractAdd_type");
                Map<String, String> contractTypeMap = sysCodeService.getCodeMapByType("contract_type");
                Map<String, String> incomeTypeMap = sysCodeService.getCodeMapByType("income_type");
                Map<String, String> selectType1Map = sysCodeService.getCodeMapByType("select_type_1");
                Map<String, String> oppoStateMap = sysCodeService.getCodeMapByType("oppo_state");
                Map<String, String> bidResultMap = sysCodeService.getCodeMapByType("bid_result");
                Map<String, String> bgTypeResultMap = sysCodeService.getCodeMapByType("bg_type_new");

                StringBuilder resultMsg = new StringBuilder();
                for (ContractAndOpportunityPutConfig contractAndOpportunityPutConfig : contractAndOpportunityPutConfigs){
                    long l1 = System.currentTimeMillis();
                    ftpDirectory = contractAndOpportunityPutConfig.getFtpDirectory();
                    String areaEng = contractAndOpportunityPutConfig.getAreaEng();
                    Integer areaId = contractAndOpportunityPutConfig.getAreaId();

                    String contractIncome = areaEng + "ContractIncome" + currentDate + ".xls";//合同收入
                    String contract = areaEng + "Contract" + currentDate + ".xls";//合同
                    String opportunity = areaEng + "Opportunity" + currentDate + ".xls";//商机

                    // 使用SftpUtil直连上传（每个文件独立连接，自动管理生命周期）
                    final String configType = "contract_and_opportunity_put_job";

                    // 创建并上传合同收入文件
                    File incomeFile = createContractIncomeFile(areaId, areaEng);
                    long l2 = System.currentTimeMillis();
                    log.info("{} 合同收入文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l2 - l1));
                    if (incomeFile != null) {
                        uploadSingleFileToSftp(configType, incomeFile, contractIncome, currentDate, ftpDirectory);
                        deleteTempFile(incomeFile);
                    }

                    // 创建并上传合同统计数据文件
                    File contractFile = createContractStatisticsFile(areaId, areaEng, oppTypeMap, unicomTypeMap,
                            unicomType2Map, clueSourceMap, contractAddTypeMap, contractTypeMap, incomeTypeMap);
                    long l3 = System.currentTimeMillis();
                    log.info("{} 合同统计数据文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l3 - l2));
                    if (contractFile != null) {
                        uploadSingleFileToSftp(configType, contractFile, contract, currentDate, ftpDirectory);
                        deleteTempFile(contractFile);
                    }

                    // 创建并上传商机数据文件
                    File opportunityFile = createOpportunityFile(areaId, areaEng, oppTypeMap, unicomTypeMap,
                            unicomType2Map, unicomType2Settingstr1Map, clueSourceMap, selectType1Map,
                            oppoStateMap, bidResultMap, bgTypeResultMap);
                    long l4 = System.currentTimeMillis();
                    log.info("{} 商机数据文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l4 - l3));
                    if (opportunityFile != null) {
                        uploadSingleFileToSftp(configType, opportunityFile, opportunity, currentDate, ftpDirectory);
                        deleteTempFile(opportunityFile);
                    }

                    log.info("{} 总耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l4 - l1));

                    log.info("{} 任务结束", contractAndOpportunityPutConfig.getAreaName());

                    resultMsg.append(contractAndOpportunityPutConfig.getAreaName())
                        .append(": 合同收入=").append(incomeFile != null ? "成功" : "失败")
                        .append(", 合同统计=").append(contractFile != null ? "成功" : "失败")
                        .append(", 商机=").append(opportunityFile != null ? "成功" : "失败")
                        .append("; ");
                }
                return resultMsg.toString();
            });
    }

    /**
     * 创建合同收入数据Excel文件
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     */
    private File createContractIncomeFile(Integer areaId, String areaEng) {
        log.info("开始执行合同收入数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("contract_income_", ".xls");

            // ✅ try-with-resources 自动关闭 ExcelWriter，无需手动 finish
            try (ExcelWriter excelWriter = EasyExcel.write(tempFile, ContractIncomeExcelVo.class)
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20))
                .inMemory(false)  // 不使用内存缓存，直接写磁盘
                .build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet("合同收入数据统计").build();

                int offset = 0;
                int pageSize = 1000;
                int totalCount = 0;
                List<ContractIncomeExcelVo> batch;
                do {
                    batch = toppContractMapper.getContractIncomeListPaged(areaId, offset, pageSize);
                    if (batch != null && !batch.isEmpty()) {
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        offset += pageSize;
                    }
                } while (batch != null && batch.size() == pageSize);

                log.info("合同收入数据导出完成，共 {} 条", totalCount);
            }
            // 离开try-with-resources代码块后，自动执行finish/close

            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("合同收入数据导出文件创建失败");
                return null;
            }

            log.info("合同收入数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                tempFile.getAbsolutePath(), tempFile.length());

            return tempFile;
        } catch (Exception e) {
            log.error("合同收入数据导出任务执行失败,areaId: {}", areaId, e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        }
    }

    /**
     * 创建合同统计数据Excel文件
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     */
    private File createContractStatisticsFile(Integer areaId, String areaEng,
                                              Map<String, String> oppTypeMap, Map<String, String> unicomTypeMap,
                                              Map<String, String> unicomType2Map, Map<String, String> clueSourceMap,
                                              Map<String, String> contractAddTypeMap, Map<String, String> contractTypeMap,
                                              Map<String, String> incomeTypeMap) {
        log.info("开始执行合同统计数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            Calendar cal = Calendar.getInstance();
            int nowYear = cal.get(Calendar.YEAR);
            cal.add(Calendar.YEAR, -1);
            int lastYear = cal.get(Calendar.YEAR);

            Calendar cal1 = Calendar.getInstance();
            int nowMonth = cal1.get(Calendar.MONTH) + 1;
            int lastMonth = cal1.get(Calendar.MONTH);
            String nowYearMonth = String.valueOf(nowYear) + (nowMonth < 10 ? "0" + nowMonth : nowMonth);
            String lastYearMonth = String.valueOf(nowYear) + (lastMonth < 10 ? "0" + lastMonth : lastMonth);

            log.info("时间参数 - nowYear: {}, lastYear: {}, nowYearMonth: {}, lastYearMonth: {}",
                nowYear, lastYear, nowYearMonth, lastYearMonth);

            tempFile = File.createTempFile("contract_statistics_", ".xls");

            // ✅ try-with-resources 自动关闭
            try (ExcelWriter excelWriter = EasyExcel.write(tempFile, ToppContractExcelVo.class)
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20))
                .inMemory(false)  // 不使用内存缓存，直接写磁盘
                .build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet("合同查询统计").build();

                int offset = 0;
                int pageSize = 1000;
                int totalCount = 0;
                List<ToppContractExcelVo> batch;
                do {
                    batch = toppContractMapper.exportContractListPaged(
                        areaId, nowYear, lastYear, nowYearMonth, lastYearMonth, offset, pageSize);
                    if (batch != null && !batch.isEmpty()) {
                        translateAndFormatContractStatisticsData(batch, oppTypeMap, unicomTypeMap,
                            unicomType2Map, clueSourceMap, contractAddTypeMap, contractTypeMap, incomeTypeMap);
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        offset += pageSize;
                    }
                } while (batch != null && batch.size() == pageSize);

                log.info("合同统计数据导出完成，共 {} 条", totalCount);
            }

            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("合同统计数据导出文件创建失败");
                return null;
            }

            log.info("合同统计数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                tempFile.getAbsolutePath(), tempFile.length());

            return tempFile;
        } catch (Exception e) {
            log.error("合同统计数据导出任务执行失败,areaId: {}", areaId, e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        }
    }

    /**
     * 将合同统计数据进行转换和格式化
     */
    private void translateAndFormatContractStatisticsData(List<ToppContractExcelVo> contractList,
            Map<String, String> oppTypeMap, Map<String, String> unicomTypeMap,
            Map<String, String> unicomType2Map, Map<String, String> clueSourceMap,
            Map<String, String> contractAddTypeMap, Map<String, String> contractTypeMap,
            Map<String, String> incomeTypeMap) {
        if (contractList == null || contractList.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder(128); // 方法开始时创建一次
        for (ToppContractExcelVo contract : contractList){
            // 1.业务类型（主）
            String oppType = contract.getOppType();
            contract.setOppType(oppType != null ? oppTypeMap.get(oppType) : "");

            // 2.业务类型（全）
            String oppIncludeType = contract.getOppIncludeType();
            if (oppIncludeType != null && oppIncludeType.length() > 2) {
                // 统一截取首尾括号
                String cleanedType = oppIncludeType.substring(1, oppIncludeType.length() - 1);
                if(cleanedType.contains(",")){
                    // 多个业务类型：分割后翻译
                    String[] oppIncludeTypeArr = cleanedType.split(",");
                    sb.setLength(0); // 复用，清空内容
                    for (String type : oppIncludeTypeArr) {
                        String trimType = type.trim();
                        String name = oppTypeMap.get(trimType);
                        if (name != null) {
                            sb.append(name).append(' ');  // char比String轻量
                        }
                    }
                    contract.setOppIncludeType(sb.toString().trim());
                }else{
                    // 单个业务类型
                    String trimType = cleanedType.trim();
                    contract.setOppIncludeType(oppTypeMap.getOrDefault(trimType, ""));
                }
            }else{
                contract.setOppIncludeType("");
            }

            // 3.一级行业
            String unicomType = contract.getUnicomType();
            contract.setUnicomType(unicomType != null ? unicomTypeMap.get(unicomType) : "");

            // 4.二级行业
            String unicomType2 = contract.getUnicomType2();
            contract.setUnicomType2(unicomType2 != null ? unicomType2Map.get(unicomType2) : "");

            // 5.商机来源
            String oppSource = contract.getOppSource();
            contract.setOppSource(oppSource != null ? clueSourceMap.get(oppSource) : "");

            // 6.合同种类
            String contractAddType = contract.getContractAddType();
            contract.setContractAddType(contractAddType != null ? contractAddTypeMap.get(contractAddType) : "");

            // 7.合同类型
            String contractType = contract.getContractType();
            contract.setContractType(contractType != null ? contractTypeMap.get(contractType) : "");

            // 8.计收类型
            String incomeType = contract.getIncomeType();
            contract.setIncomeType(incomeType != null ? incomeTypeMap.get(incomeType) : "");

            // 9.是否有效
            String isValid = contract.getIsValid();
            contract.setIsValid(isValid == null ? "暂未设置" : "1".equals(isValid) ? "有效" : "无效");

            // 10.合同分类
            String contractCategory = contract.getContractCategory();
            if(contractCategory == null){
                contract.setContractCategory("");
            }else{
                switch (contractCategory) {
                    case "1": contract.setContractCategory("新增合同"); break;
                    case "2": contract.setContractCategory("续约合同"); break;
                    default: contract.setContractCategory("");
                }
            }
        }
    }

    /**
     * 创建商机查询数据Excel文件
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     */
    private File createOpportunityFile(Integer areaId, String areaEng,
                                       Map<String, String> oppTypeMap, Map<String, String> unicomTypeMap,
                                       Map<String, String> unicomType2Map, Map<String, String> unicomType2Settingstr1Map,
                                       Map<String, String> clueSourceMap, Map<String, String> selectType1Map,
                                       Map<String, String> oppoStateMap, Map<String, String> bidResultMap,
                                       Map<String, String> bgTypeResultMap) {
        log.info("开始执行商机数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            tempFile = File.createTempFile("opportunity_", ".xls");

            // ✅ try-with-resources 自动关闭
            try (ExcelWriter excelWriter = EasyExcel.write(tempFile, ToppOpportunityExcelVo.class)
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20))
                .inMemory(false)  // 不使用内存缓存，直接写磁盘
                .build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet("商机查询统计").build();

                int offset = 0;
                int pageSize = 1000;
                int totalCount = 0;
                List<ToppOpportunityExcelVo> batch;
                do {
                    batch = toppOpportunityMapper.selectOpportunityExportListPaged(areaId, offset, pageSize);
                    if (batch != null && !batch.isEmpty()) {
                        processingOpportunityData(batch, oppTypeMap, unicomTypeMap, unicomType2Map,
                            unicomType2Settingstr1Map, clueSourceMap, selectType1Map,
                            oppoStateMap, bidResultMap, bgTypeResultMap);
                        excelWriter.write(batch, writeSheet);
                        totalCount += batch.size();
                        offset += pageSize;
                    }
                } while (batch != null && batch.size() == pageSize);

                log.info("商机数据导出完成，共 {} 条", totalCount);
            }

            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("商机数据导出文件创建失败");
                return null;
            }

            log.info("商机数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                tempFile.getAbsolutePath(), tempFile.length());

            return tempFile;
        } catch (Exception e) {
            log.error("商机数据导出任务执行失败,areaId: {}", areaId, e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        }
    }

    /**
     * 对查询到的商机数据进行处理
     */
    private void processingOpportunityData(List<ToppOpportunityExcelVo> List,
            Map<String, String> oppTypeMap, Map<String, String> unicomTypeMap,
            Map<String, String> unicomType2Map, Map<String, String> unicomType2Settingstr1Map,
            Map<String, String> clueSourceMap, Map<String, String> selectType1Map,
            Map<String, String> oppoStateMap, Map<String, String> bidResultMap,
            Map<String, String> bgTypeResultMap) {
        if (List == null || List.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder(128);
        for(ToppOpportunityExcelVo opportunity : List){
            //业务类型（主）
            String oppType = opportunity.getOppType();
            opportunity.setOppType(oppType != null ? oppTypeMap.get(oppType) : "");

            //业务类型（次）
            String oppIncludeType = opportunity.getOppIncludeType();
            if (oppIncludeType != null && !oppIncludeType.isEmpty()) {
                // 去掉首尾括号
                String cleanedType = oppIncludeType.substring(1, oppIncludeType.length() - 1);

                if (cleanedType.contains(",")) {
                    // 多个业务类型，用逗号分隔
                    String[] typeArray = cleanedType.split(",");
                    sb.setLength(0); // 复用，清空内容
                    for (String type : typeArray) {
                        String trimmedType = type.trim();
                        if (!trimmedType.isEmpty()) {
                            String typeName = oppTypeMap.get(trimmedType);
                            if (typeName != null) {
                                sb.append(typeName).append(' ');
                            }
                        }
                    }
                    opportunity.setOppIncludeType(sb.toString().trim());
                } else {
                    // 单个业务类型
                    String trimmedType = cleanedType.trim();
                    opportunity.setOppIncludeType(!trimmedType.isEmpty() ? oppTypeMap.get(trimmedType) : "");
                }
            } else {
                opportunity.setOppIncludeType("");
            }

            //所属一级行业
            String unicomType = opportunity.getUnicomType();
            opportunity.setUnicomType(unicomType != null ? unicomTypeMap.get(unicomType) : "");

            //所属二级行业
            String unicomType2 = opportunity.getUnicomType2();
            opportunity.setUnicomType2(unicomType2 != null ? unicomType2Map.get(unicomType2) : "");

            //客户拓展属性
            String developFlag = opportunity.getDevelopFlag();
            if (developFlag != null) {
                switch (developFlag) {
                    case "01":
                        opportunity.setDevelopFlag("省直拓");
                        break;
                    case "02":
                        opportunity.setDevelopFlag("市直拓");
                        break;
                    case "03":
                        opportunity.setDevelopFlag("县直拓");
                        break;
                    default:
                        opportunity.setDevelopFlag("");
                        break;
                }
            } else {
                opportunity.setDevelopFlag("");
            }

            //商机来源
            String oppSource = opportunity.getOppSource();
            opportunity.setOppSource(oppSource != null ? clueSourceMap.get(oppSource) : "");

            //是否招投标
            String isBidding = opportunity.getIsBidding();
            opportunity.setIsBidding(isBidding != null ? selectType1Map.get(isBidding) : "");

            //是否需要外部资源
            String isNeedExternalResources = opportunity.getIsNeedExternalResources();
            if(isNeedExternalResources != null){
                opportunity.setIsNeedExternalResources("1".equals(isNeedExternalResources) ? "是" : "否");
            }else{
                opportunity.setIsNeedExternalResources("");
            }

            //是否具有创新型
            String isHaveInnovate = opportunity.getIsHaveInnovate();
            if(isHaveInnovate != null){
                opportunity.setIsHaveInnovate("1".equals(isHaveInnovate) ? "是" : "否");
            }else{
                opportunity.setIsHaveInnovate("");
            }

            //集团客户一级行业: departmentCode → setting_str1 → unicomTypeMap
            String departmentCode = opportunity.getDepartmentCode();
            String settingStr1 = unicomType2Settingstr1Map.get(departmentCode);
            opportunity.setCustomerUnicomType(settingStr1 != null ? unicomTypeMap.get(settingStr1) : "");
            //集团客户二级行业: departmentCode → unicomType2Map
            opportunity.setDepartmentCode(departmentCode != null ? unicomType2Map.get(departmentCode) : "");

            //创建时间 - SQL中已使用DATE_FORMAT格式化为 yyyy/MM/dd 格式，无需额外处理

            //商机状态
            String oppoState = opportunity.getOppoState();
            opportunity.setOppoState(oppoState != null ? oppoStateMap.get(oppoState) : "");

            //投标状态
            String bidResult = opportunity.getBidResult();
            opportunity.setBidResult(bidResult != null ? bidResultMap.get(bidResult) : "");

            //

            //是否有效
            String isValid = opportunity.getIsValid();
            if (isValid != null){
                opportunity.setIsValid("1".equals(isValid) ? "有效" : "无效");
            }

            //商机行业 BU打标
            String bgType = opportunity.getBgType();
            opportunity.setBgType(bgType != null ? bgTypeResultMap.get(bgType) : "");
        }
    }

    /**
     * 保存文件到本地(本地测试用)
     *
     * @param file 源文件
     * @param fileName 文件名
     */
    private void saveToLocal(File file, String fileName) {
        try {
            Files.copy(file.toPath(), Paths.get(LOCAL_SAVE_PATH, fileName), StandardCopyOption.REPLACE_EXISTING);
            log.info("文件已保存到本地: {}/{}", LOCAL_SAVE_PATH, fileName);
        } catch (Exception e) {
            log.error("文件保存到本地失败: {}", fileName, e);
        }
    }

    /**
     * 上传单个文件到SFTP服务器（使用SftpUtil）
     *
     * @param configType 配置类型
     * @param file 要上传的文件
     * @param fileName 目标文件名
     * @param dateSubDir 日期子目录（yyyyMMdd格式）
     * @param ftpDir 远程基础目录（per-area）
     */
    private void uploadSingleFileToSftp(String configType, File file, String fileName, String dateSubDir, String ftpDir) {
        String remotePath = "/" + ftpDir + "/" + dateSubDir + "/" + fileName;
        sftpUtil.uploadFile(configType, file.getAbsolutePath(), remotePath);
        log.info("文件上传成功: {}", fileName);
    }

    /**
     * 删除临时文件，释放磁盘空间
     *
     * @param file 要删除的临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            if (file.delete()) {
                log.info("临时文件已删除: {}", file.getAbsolutePath());
            } else {
                log.warn("临时文件删除失败: {}", file.getAbsolutePath());
            }
        }
    }
}
