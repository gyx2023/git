package com.crm.job.service.file.contractAndOpportunityPut;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crm.job.utils.SftpUtil;
import com.crm.job.utils.SftpWebUtil;
import com.crm.system.domain.ContractAndOpportunityPutConfig;
import com.crm.system.domain.vo.ContractIncomeExcelVo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.vo.ToppContractExcelVo;
import com.crm.system.domain.vo.topp.ToppOpportunityExcelVo;
import com.crm.system.mapper.ContractAndOpportunityPutConfigMapper;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.ISysCodeService;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.impl.SysCodeServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
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

    //完整的文件名+扩展名
    static String contractIncome;//合同收入
    static String contract;//合同
    static String opportunity;//商机

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

    @XxlJob("contractAndOpportunityPutJob")
    public void execute(){
        Map<String, String> param = sysParamService.findValueMapsByType("contract_and_opportunity_put_job");
        ftpUsername = param.get("ftp_username");  //用户名
        ftpPassword = param.get("ftp_password");  //密码
        ftpHost = param.get("ftp_host");  //ip地址
        ftpPort = param.get("ftp_port");  //端口号

        String currentDate = null;
        String jobParam = XxlJobHelper.getJobParam();
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            currentDate = jobParam;
        } else {
            currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        List<ContractAndOpportunityPutConfig> contractAndOpportunityPutConfigs = contractAndOpportunityPutConfigMapper.selectList();
        for (ContractAndOpportunityPutConfig contractAndOpportunityPutConfig : contractAndOpportunityPutConfigs){
            long l1 = System.currentTimeMillis();
            ftpDirectory = contractAndOpportunityPutConfig.getFtpDirectory();
            String areaEng = contractAndOpportunityPutConfig.getAreaEng();
            Integer areaId = contractAndOpportunityPutConfig.getAreaId();

            contractIncome = areaEng + "ContractIncome" + currentDate + ".xlsx";
            contract = areaEng + "Contract" + currentDate + ".xlsx";
            opportunity = areaEng + "Opportunity" + currentDate + ".xlsx";

            // 创建合同收入文件并上传
            createContractIncomeFile(areaId, areaEng, currentDate);
            long l2 = System.currentTimeMillis();
            log.info("{} 合同收入文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l2 - l1));

            // 创建合同统计数据文件并上传
            createContractStatisticsFile(areaId, areaEng, currentDate);
            long l3 = System.currentTimeMillis();
            log.info("{} 合同统计数据文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l3 - l2));

            // 创建商机数据文件并上传
            createOpportunityFile(areaId, areaEng, currentDate);
            long l4 = System.currentTimeMillis();
            log.info("{} 商机数据文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l4 - l3));
        }
    }

    /**
     * 创建合同收入数据Excel文件并上传到SFTP服务器
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     * @param currentDate 当前日期（yyyyMMdd格式）
     */
    private void createContractIncomeFile(Integer areaId, String areaEng, String currentDate) {
        log.info("开始执行合同收入数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            // 1. 查询数据
            List<ContractIncomeExcelVo> contractIncomeList = toppContractMapper.getContractIncomeList(areaId);

            // 2. 检查数据是否为空
            if (contractIncomeList == null || contractIncomeList.isEmpty()) {
                log.warn("合同收入数据为空，areaId: {}", areaId);
                return;
            }

            log.info("查询到合同收入数据 {} 条", contractIncomeList.size());

            // 3. 创建临时文件
            String fileName = areaEng + "ContractIncome" + currentDate + ".xlsx";
            tempFile = File.createTempFile("contract_income_", ".xlsx");

            // 4. 使用EasyExcel写入数据
            EasyExcel.write(tempFile, ContractIncomeExcelVo.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("合同收入数据统计")
                    .doWrite(contractIncomeList);

            // 5. 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("合同收入数据导出文件创建失败");
                return;
            }

            log.info("合同收入数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                    tempFile.getAbsolutePath(), tempFile.length());

            // 6. 上传到SFTP服务器
            uploadToSftp(tempFile, contractIncome,currentDate);

        } catch (Exception e) {
            log.error("合同收入数据导出任务执行失败,areaId: {}", areaId, e);
        } finally {
            // 7. 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    log.info("临时文件已删除: {}", tempFile.getAbsolutePath());
                } else {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 创建合同统计数据Excel文件并上传到SFTP服务器
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     * @param currentDate 当前日期（yyyyMMdd格式）
     */
    private void createContractStatisticsFile(Integer areaId, String areaEng, String currentDate) {
        log.info("开始执行合同统计数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            // 1. 计算时间参数
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

            // 2. 查询数据
            List<ToppContractExcelVo> contractList = toppContractMapper.exportContractList(
                    areaId, nowYear, lastYear, nowYearMonth, lastYearMonth);

            // 3. 检查数据是否为空
            if (contractList == null || contractList.isEmpty()) {
                log.warn("合同统计数据为空，areaId: {}", areaId);
                return;
            }

            log.info("查询到合同统计数据 {} 条", contractList.size());

            //进行数据格式转换
            translateAndFormatContractStatisticsData(contractList);

            // 4. 创建临时文件
            String fileName = areaEng + "Contract" + currentDate + ".xlsx";
            tempFile = File.createTempFile("contract_statistics_", ".xlsx");

            // 5. 使用EasyExcel写入数据
            EasyExcel.write(tempFile, ToppContractExcelVo.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("合同统计数据")
                    .doWrite(contractList);

            // 6. 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("合同统计数据导出文件创建失败");
                return;
            }

            log.info("合同统计数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                    tempFile.getAbsolutePath(), tempFile.length());

            // 7. 上传到SFTP服务器
            uploadToSftp(tempFile, contract,currentDate);

        } catch (Exception e) {
            log.error("合同统计数据导出任务执行失败,areaId: {}", areaId, e);
        } finally {
            // 8. 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    log.info("临时文件已删除: {}", tempFile.getAbsolutePath());
                } else {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 将合同统计数据进行转换和格式化
     */
    private void translateAndFormatContractStatisticsData(List<ToppContractExcelVo> contractList) {
        for (ToppContractExcelVo contract : contractList){
            //业务类型（主）
            String oppType = contract.getOppType();
            if (oppType != null){
                SysCodeVo sysCode1 = sysCodeService.findCode("opp_type", oppType);
                if (sysCode1 != null) {
                    contract.setOppType(sysCode1.getName());
                }else{
                    contract.setOppType("");
                }
            }else{
                contract.setOppType("");
            }

            //业务类型（全）
            String oppIncludeType = contract.getOppIncludeType();
            //截取掉首尾的括号
            if(oppIncludeType != null){
                oppIncludeType = oppIncludeType.substring(1).substring(0, oppIncludeType.length() - 2);
                //如果是多个
                if(oppIncludeType.contains(",")){
                    String[] oppIncludeTypeArr = oppIncludeType.split(",");
                    for(int i = 0; i < oppIncludeTypeArr.length; i++){
                        String oppIncludeType1 = oppIncludeTypeArr[i];
                        SysCodeVo sysCode2 = sysCodeService.findCode("opp_type", oppIncludeType1);
                        if(sysCode2 != null){
                            oppIncludeTypeArr[i] = sysCode2.getName();
                        }else{
                            oppIncludeTypeArr[i] = "";
                        }
                    }
                    contract.setOppIncludeType(String.join(" ", oppIncludeTypeArr));
                }else{
                    //单个逻辑
                    SysCodeVo sysCode2 = sysCodeService.findCode("opp_type", oppIncludeType);
                    if(sysCode2 != null){
                        contract.setOppIncludeType(sysCode2.getName());
                    }else{
                        contract.setOppIncludeType("");
                    }
                }
            }

            //一级行业
            String unicomType = contract.getUnicomType();
            if(unicomType != null){
                SysCodeVo sysCode3 = sysCodeService.findCode("unicom_type", unicomType);
                if(sysCode3 != null){
                    contract.setUnicomType(sysCode3.getName());
                }else{
                    contract.setUnicomType("");
                }
            }else{
                contract.setUnicomType("");
            }

            //二级行业
            String unicomType2 = contract.getUnicomType2();
            if(unicomType2 != null){
                SysCodeVo sysCode4 = sysCodeService.findCode("unicom_type_2", unicomType2);
                if(sysCode4 != null){
                    contract.setUnicomType2(sysCode4.getName());
                }else{
                    contract.setUnicomType2("");
                }
            }else{
                contract.setUnicomType2("");
            }

            //商机来源
            String oppSource = contract.getOppSource();
            if(oppSource != null){
                SysCodeVo sysCode5 = sysCodeService.findCode("clue_source", oppSource);
                if(sysCode5 != null){
                    contract.setOppSource(sysCode5.getName());
                }else{
                    contract.setOppSource("");
                }
            }else{
                contract.setOppSource("");
            }

            //合同种类
            String contractAddType = contract.getContractAddType();
            if(contractAddType != null){
                SysCodeVo sysCode6 = sysCodeService.findCode("contractAdd_type", contractAddType);
                if(sysCode6 != null){
                    contract.setContractAddType(sysCode6.getName());
                }else{
                    contract.setContractAddType("");
                }
            }else{
                contract.setContractAddType("");
            }

            //合同类型
            String contractType = contract.getContractType();
            if(contractType != null){
                SysCodeVo sysCode7 = sysCodeService.findCode("contract_type", contractType);
                if(sysCode7 != null){
                    contract.setContractType(sysCode7.getName());
                }else{
                    contract.setContractType("");
                }
            }else{
                contract.setContractType("");
            }

            //计收类型
            String incomeType = contract.getIncomeType();
            if (incomeType != null){
                SysCodeVo sysCode8 = sysCodeService.findCode("income_type", incomeType);
                if(sysCode8 != null){
                    contract.setIncomeType(sysCode8.getName());
                }else{
                    contract.setIncomeType("");
                }
            }else{
                contract.setIncomeType("");
            }

            //是否有效
            String isValid = contract.getIsValid();
            if (isValid == null){
                contract.setIsValid("暂未设置");
            }else{
                if("1".equals(isValid)){
                    contract.setIsValid("有效");
                }else{
                    contract.setIsValid("无效");
                }
            }

            //合同分类
            String contractCategory = contract.getContractCategory();
            if(contractCategory == null){
                contract.setContractCategory("");
            }else{
                if("1".equals(contractCategory)){
                    contract.setContractCategory("新增合同");
                } else if ("2".equals(contractCategory)) {
                    contract.setContractCategory("续约合同");
                }
            }
        }
    }

    /**
     * 创建商机查询数据Excel文件并上传到SFTP服务器
     *
     * @param areaId 区域ID
     * @param areaEng 区域英文标识
     * @param currentDate 当前日期（yyyyMMdd格式）
     */
    private void createOpportunityFile(Integer areaId, String areaEng, String currentDate) {
        log.info("开始执行商机数据导出任务，areaId: {}, areaEng: {}", areaId, areaEng);

        File tempFile = null;
        try {
            // 1. 查询数据
            List<ToppOpportunityExcelVo> opportunityList = toppOpportunityMapper.selectOpportunityExportList(areaId);

            // 2. 检查数据是否为空
            if (opportunityList == null || opportunityList.isEmpty()) {
                log.warn("商机数据为空，areaId: {}", areaId);
                return;
            }

            log.info("查询到商机数据 {} 条", opportunityList.size());

            // 3. 进行数据格式转换
            processingOpportunityData(opportunityList);

            // 4. 创建临时文件
            String fileName = areaEng + "Opportunity" + currentDate + ".xlsx";
            tempFile = File.createTempFile("opportunity_", ".xlsx");

            // 5. 使用EasyExcel写入数据
            EasyExcel.write(tempFile, ToppOpportunityExcelVo.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("商机数据")
                    .doWrite(opportunityList);

            // 6. 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                log.error("商机数据导出文件创建失败");
                return;
            }

            log.info("商机数据导出成功,临时文件路径: {}, 文件大小: {} bytes",
                    tempFile.getAbsolutePath(), tempFile.length());

            // 7. 上传到SFTP服务器
            uploadToSftp(tempFile, opportunity,currentDate);

        } catch (Exception e) {
            log.error("商机数据导出任务执行失败,areaId: {}", areaId, e);
        } finally {
            // 8. 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (deleted) {
                    log.info("临时文件已删除: {}", tempFile.getAbsolutePath());
                } else {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 对查询到的商机数据进行处理
     */
    private void processingOpportunityData(List<ToppOpportunityExcelVo> List){
        //业务类型（主）map
        Map<String,String> oppTypeMap = sysCodeService.getCodeMapByType("opp_type");
        //所属一级行业map
        Map<String,String> unicomTypeMap = sysCodeService.getCodeMapByType("unicom_type");
        //所属二级行业map
        Map<String,String> unicomType2Map = sysCodeService.getCodeMapByType("unicom_type_2");
        //将二级行业编码映射到一级行业编码的map
        Map<String,String> unicomType2Settingstr1Map = sysCodeService.getCodeSettingStr1MapByType("unicom_type_2");
        //商机来源map
        Map<String,String> clueSourceMap = sysCodeService.getCodeMapByType("clue_source");
        //是否招投标map
        Map<String,String> selectType1Map = sysCodeService.getCodeMapByType("select_type_1");
        //商机状态map
        Map<String,String> oppoStateMap = sysCodeService.getCodeMapByType("oppo_state");
        //投标结果map
        Map<String,String> bidResultMap = sysCodeService.getCodeMapByType("bid_result");
        //商机行业 BU打标map
        Map<String,String> bgTypeResultMap = sysCodeService.getCodeMapByType("bg_type_new");

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
                    StringBuilder sb = new StringBuilder();
                    for (String type : typeArray) {
                        String trimmedType = type.trim();
                        if (!trimmedType.isEmpty()) {
                            String typeName = oppTypeMap.get(trimmedType);
                            if (typeName != null) {
                                sb.append(typeName).append(" ");
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
                    case "1":
                        opportunity.setDevelopFlag("省直拓");
                        break;
                    case "2":
                        opportunity.setDevelopFlag("市直拓");
                        break;
                    case "3":
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

            //集团客户二级行业
            String departmentCode = opportunity.getDepartmentCode();
            opportunity.setCustomerUnicomType(departmentCode != null ? unicomType2Map.get(departmentCode) : "");
            //集团客户一级行业
            String customerUnicomType = unicomType2Settingstr1Map.get(departmentCode);
            opportunity.setDepartmentCode(customerUnicomType != null ? unicomTypeMap.get(customerUnicomType) : "");

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
     * 上传文件到SFTP服务器
     *
     * @param file 要上传的文件
     * @param fileName 文件名
     * @param dateSubDir 日期子目录（yyyyMMdd格式）
     */
    private void uploadToSftp(File file, String fileName, String dateSubDir) {
        SftpWebUtil sftpUtil = null;
        FileInputStream fis = null;
        try {
            // 创建SFTP连接
            sftpUtil = new SftpWebUtil(ftpUsername, ftpPassword, ftpHost, Integer.parseInt(ftpPort));
            sftpUtil.login();

            // 读取文件并上传，使用日期作为子目录
            fis = new FileInputStream(file);
            sftpUtil.upload(ftpDirectory, dateSubDir, fileName, fis);

            log.info("文件上传成功: {}", fileName);
        } catch (Exception e) {
            log.error("文件上传失败: {}", fileName, e);
        } finally {
            // 关闭输入流
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    log.warn("关闭输入流失败", e);
                }
            }
            // 断开SFTP连接
            if (sftpUtil != null) {
                try {
                    sftpUtil.logout();
                } catch (Exception e) {
                    log.warn("断开SFTP连接失败", e);
                }
            }
        }
    }
}
