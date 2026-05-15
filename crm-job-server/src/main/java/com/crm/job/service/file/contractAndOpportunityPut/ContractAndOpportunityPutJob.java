package com.crm.job.service.file.contractAndOpportunityPut;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.ContractAndOpportunityPutConfig;
import com.crm.system.domain.vo.ContractIncomeExcelVo;
import com.crm.system.mapper.ContractAndOpportunityPutConfigMapper;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.crm.system.service.ISysParamService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ContractAndOpportunityPutJob {
//    private static String ftpUsername;
//    private static String ftpPassword;
//    private static String ftpHost;
//    private static String ftpPort;
//    private static String ftpDirectory;
//
//    //完整的文件名+扩展名
//    static String contractIncome;//合同收入
//    static String contract;//合同
//    static String opportunity;//商机
//
//    @Resource
//    private ISysParamService sysParamService;
    @Resource
    private ContractAndOpportunityPutConfigMapper contractAndOpportunityPutConfigMapper;
    @Resource
    private ToppContractMapper toppContractMapper;
    @Resource
    private SftpUtil sftpUtil;

    @XxlJob("contractAndOpportunityPutJob")
    public void execute(){
//        Map<String, String> param = sysParamService.findValueMapsByType("contract_and_opportunity_put_job");
//        ftpUsername = param.get("ftp_username");  //用户名
//        ftpPassword = param.get("ftp_password");  //密码
//        ftpHost = param.get("ftp_host");  //ip地址
//        ftpPort = param.get("ftp_port");  //端口号

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
            //ftpDirectory = contractAndOpportunityPutConfig.getFtpDirectory();
            String areaEng = contractAndOpportunityPutConfig.getAreaEng();
            Integer areaId = contractAndOpportunityPutConfig.getAreaId();

//            contractIncome = areaEng + "ContractIncome" + currentDate + ".xls";
//            contract = areaEng + "Contract" + currentDate + ".xls";
//            opportunity = areaEng + "Opportunity" + currentDate + ".xls";

            // 创建合同收入文件并上传
            createContractIncomeFile(areaId, areaEng, currentDate);

            long l2 = System.currentTimeMillis();
            log.info("{} 合同收入文件生成耗时：{} ms", contractAndOpportunityPutConfig.getAreaName(), (l2 - l1));

            // TODO: 后续添加合同和商机文件的生成逻辑
            // createContractFile(areaId, areaEng, currentDate);
            // createOpportunityFile(areaId, areaEng, currentDate);
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

            log.info("合同收入数据导出成功，临时文件路径: {}, 文件大小: {} bytes",
                    tempFile.getAbsolutePath(), tempFile.length());

            // 6. 上传到SFTP服务器
            String configType = "contract_and_opportunity_put_job"; // SFTP配置类型
            String remoteFileName = fileName; // 远程文件名
            String uploadDate = currentDate; // 日期文件夹

            log.info("开始上传文件到SFTP服务器，配置类型: {}, 远程文件名: {}, 日期: {}",
                    configType, remoteFileName, uploadDate);

            sftpUtil.uploadFile(configType, tempFile.getAbsolutePath(), remoteFileName, uploadDate);

            log.info("文件上传成功: {}", remoteFileName);

        } catch (Exception e) {
            log.error("合同收入数据导出任务执行失败，areaId: {}", areaId, e);
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
}
