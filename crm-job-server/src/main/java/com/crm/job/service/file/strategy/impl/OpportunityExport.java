package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.crm.common.core.domain.dto.OldOpportunityDTO;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.vo.topp.OldOpportunityVO;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.IDwdRepSysPersonDService;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商机信息导出策略实现类
 * 处理商机信息导出的具体业务逻辑
 *
 * @author crm
 * @date 2026-02-04
 */
@Slf4j
@Component
public class OpportunityExport extends AbstractExportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private ToppOpportunityMapper toppOpportunityMapper;

    @Resource
    private DwdRepSysPersonDMapper dwdRepSysPersonDMapper;

    @Resource
    private IDwdRepSysPersonDService dwdRepSysPersonDService;

    @Resource
    private IToppContractService contractService;

    @Resource
    private IToppContractEformService contractEformService;

    @Override
    public String getBusinessType() {
        return "opportunity_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            log.info("开始执行商机导出任务，任务ID: {}", task.getTaskId());

            // 解析请求参数
            Map<String, Object> requestParams = parseRequestParams(task);

            // 获取登录用户信息
            Long userId = task.getCreateUserId();
            if (userId == null) {
                task.setRemark("任务缺少创建用户信息");
                return false;
            }

            // 构建基础SQL查询
            StringBuffer sql = buildBaseSql(userId);

            // 添加查询条件
            appendAllQueryConditions(sql, requestParams);

            // 添加排序条件
            sql.append(" order by op.id_ desc");

            //打印 SQL
            log.info("查询的SQL语句: {}", sql.toString());

            // 创建临时文件
            String prefix = "opportunity_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");

            try {
                // 使用完整版流式导出方法
                boolean success = exportToExcelStreamFull(sql.toString(), tempFile);
                if (!success) {
                    task.setRemark("流式导出失败");
                    return false;
                }
            } catch (Exception e) {
                task.setRemark("流式导出异常");
                log.error("流式导出异常: " + e.getMessage());
                tempFile.delete();
                return false;
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }

            // 定义原始中文文件名
            String originalFileName = "商机信息导出_" + System.currentTimeMillis() + ".xlsx";

            // 保存到本地指定目录
            String localDownloadPath = saveToLocal(tempFile, originalFileName);

            // 上传文件到OSS
//            SysOssVo upload = iSysOssService.upload(tempFile, originalFileName);
//
//            // 验证上传是否成功
//            if (upload == null) {
//                task.setRemark("文件上传失败");
//                tempFile.delete();
//                return false;
//            }
//
//            // 删除临时文件
//            tempFile.delete();

            // 设置文件信息
            task.setFileName(originalFileName);
//            task.setFileUrl(upload.getUrl());
            task.setFileUrl("file:///" + localDownloadPath); // 本地文件路径
            task.setRemark("商机信息导出成功，文件保存至: " + localDownloadPath);
            return true;

        } catch (Exception e) {
            log.error("商机信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 解析请求参数
     */
    private Map<String, Object> parseRequestParams(FileAsyncTaskDo task) {
        if (task.getRequestParams() != null) {
            return JSONUtil.toBean(task.getRequestParams(), Map.class);
        }
        return null;
    }

    /**
     * 构建基础SQL查询语句
     */
    private StringBuffer buildBaseSql(Long userId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from topp_opportunity op " +
            "join sys_user su on su.id_ = op.cust_manager_id where" +
            " (1=2 or (1=1 and (1=2 or op.opp_area_id like '33%%' ) and" +
            " op.unicom_type in('22','01','23','02','03','25','unicom_jr'," +
            "'04','26','unicom_ygs','05','06','28','07','29','08','unicom_zy'," +
            "'unicom_xy','30','10','13','15','16','17','18','19','unicom_dqy'," +
            "'unicom_sq','unicom_zlkh','21')) or (op.cust_manager_id = " + userId + " or " +
            "op.designer = " + userId + " or op.white_list like '%," + userId + ",%' or" +
            " exists (select 1 from topp_member m where m.project_id = op.id_ and m.member_id = " + userId
            + "))) ");
        return sql;
    }

    private void appendAllQueryConditions(StringBuffer sql, Map<String, Object> params) {
        // 1. 商机编码查询
        String oppCode = getParamValue(params, "oppCode");
        if (StringUtils.isNotEmpty(oppCode)) {
            sql.append(" and op.opp_code like '%" + oppCode + "%'");
        }

        // 2. 商机名称查询
        String oppName = getParamValue(params, "oppName");
        if (StringUtils.isNotEmpty(oppName)) {
            sql.append(" and op.opp_name like '%" + oppName + "%'");
        }

        // 3. 客户名称查询
        String custName = getParamValue(params, "custName");
        if (StringUtils.isNotEmpty(custName)) {
            sql.append(" and op.cust_name like '%" + custName + "%'");
        }

        // 4. 商机状态查询（支持多选）
        String oppoStates = getParamValue(params, "oppoStates");
        if (StringUtils.isNotEmpty(oppoStates)) {
            if (oppoStates.contains(",")) {
                String[] states = oppoStates.split(",");
                sql.append(" and ( 1=2");
                for (String state : states) {
                    sql.append(" or op.opp_states = '" + state.trim() + "' ");
                }
                sql.append(" ) ");
            } else {
                sql.append(" and op.opp_states = '" + oppoStates.trim() + "' ");
            }
        }

        // 5. 商机来源查询
        String oppSource = getParamValue(params, "oppSource");
        if (StringUtils.isNotEmpty(oppSource)) {
            sql.append(" and op.opp_source = '" + oppSource + "'");
        }

        // 6. 客户经理名称查询
        String custManagerName = getParamValue(params, "custManagerName");
        if (StringUtils.isNotEmpty(custManagerName)) {
            sql.append(" and op.name like '%" + custManagerName + "%'");
        }

        // 7. 区域ID查询
        String areaId = getParamValue(params, "areaId");
        if (StringUtils.isNotEmpty(areaId)) {
            sql.append(" and op.opp_area_id = '" + areaId + "'");
        }

        // 8. 开发标识查询
        String developFlag = getParamValue(params, "developFlag");
        if (StringUtils.isNotEmpty(developFlag)) {
            sql.append(" and op.develop_flag = '" + developFlag + "'");
        }

        // 9. 审核状态查询
        String auditStatus = getParamValue(params, "auditStatus");
        if (StringUtils.isNotEmpty(auditStatus)) {
            if ("01".equals(auditStatus)) {
                sql.append(" and op.audit_time is NULL ");
            } else if ("02".equals(auditStatus)) {
                sql.append(" and op.audit_time is not NULL ");
            }
        }

        // 10. 创建时间左边界查询
        String createTimeLeft = getParamValue(params, "createTimeLeft");
        if (StringUtils.isNotEmpty(createTimeLeft)) {
            sql.append(" and op.create_time >= '" + createTimeLeft + "'");
        }

        // 11. 创建时间右边界查询
        String createTimeRight = getParamValue(params, "createTimeRight");
        if (StringUtils.isNotEmpty(createTimeRight)) {
            sql.append(" and op.create_time <= '" + createTimeRight + "'");
        }

        // 12. 审核时间左边界查询
        String auditTimeLeft = getParamValue(params, "auditTimeLeft");
        if (StringUtils.isNotEmpty(auditTimeLeft)) {
            sql.append(" and op.audit_time >= '" + auditTimeLeft + "'");
        }

        // 13. 审核时间右边界查询
        String auditTimeRight = getParamValue(params, "auditTimeRight");
        if (StringUtils.isNotEmpty(auditTimeRight)) {
            sql.append(" and op.audit_time <= '" + auditTimeRight + "'");
        }

        // 14. 一级行业查询（支持多选）
        String type1 = getParamValue(params, "type1");
        sql.append(" and ( 1=2");
        if (StringUtils.isNotEmpty(type1)) {
            if (type1.contains(",")) {
                String[] strings = type1.split(",");
                sql.append(" or ( 1=2");
                for (String string : strings) {
                    sql.append(" or op.unicom_type ='" + string.trim() + "' ");
                }
                sql.append(" ) ");
            } else {
                sql.append(" or op.unicom_type ='" + type1.trim() + "' ");
            }
        } else {
            sql.append(" or 1=2 ");
        }

        if (sql.toString().endsWith("( 1=2 or 1=2 ")) {
            sql.append(" or 1=1 )");
        } else {
            sql.append(" or 1=2 )");
        }

        // 15. 商机类型处理（支持多选）
        String oppoTypes = getParamValue(params, "oppoTypes");
        String oppoTypesNew = getParamValue(params, "oppoTypesNew");
        if (StringUtils.isNotEmpty(oppoTypesNew) && StringUtils.isNotEmpty(oppoTypes)) {
            oppoTypes += "," + oppoTypesNew;
        } else if (StringUtils.isEmpty(oppoTypes) && StringUtils.isNotEmpty(oppoTypesNew)) {
            oppoTypes = oppoTypesNew;
        }

        if (StringUtils.isNotEmpty(oppoTypes)) {
            if (oppoTypes.contains(",")) {
                String[] types = oppoTypes.split(",");
                sql.append(" and ( 1=2");
                for (String type : types) {
                    sql.append(" or op.opp_type = '" + type + "' or op.opp_include_type like '%," + type + ",%' ");
                }
                sql.append(" ) ");
            } else {
                sql.append(" and (op.opp_type = '" + oppoTypes + "' or op.opp_include_type like '%," + oppoTypes + ",%')");
            }
        }

        // 16. BG类型查询
        String bgType = getParamValue(params, "bgType");
        if (StringUtils.isNotEmpty(bgType)) {
            sql.append(" and op.bg_type = '" + bgType + "'");
        }

        // 17. 预期合同金额最小值查询
        String expectContractMoneyMin = getParamValue(params, "expectContractMoneyMin");
        if (StringUtils.isNotEmpty(expectContractMoneyMin)) {
            sql.append(" and op.expect_contract_money >= " + Double.valueOf(expectContractMoneyMin));
        }

        // 18. 预期合同金额最大值查询
        String expectContractMoneyMax = getParamValue(params, "expectContractMoneyMax");
        if (StringUtils.isNotEmpty(expectContractMoneyMax)) {
            sql.append(" and op.expect_contract_money <= " + Double.valueOf(expectContractMoneyMax));
        }

        // 19. 是否有效查询
        String isValid = getParamValue(params, "isValid");
        if (StringUtils.isNotEmpty(isValid)) {
            sql.append(" and op.is_valid = " + isValid);
        }

        // 20. 是否招投标查询
        String isBidding = getParamValue(params, "isBidding");
        if (StringUtils.isNotEmpty(isBidding)) {
            sql.append(" and op.is_bidding = '" + isBidding + "'");
        }

        // 21. 投标状态查询
        String bidState = getParamValue(params, "bidState");
        if (StringUtils.isNotEmpty(bidState)) {
            sql.append(" and EXISTS (select 1 from topp_bid b where op.bid_id = b.id_ and result = '" + bidState + "') ");
        }

        // 22. 最低投标金额查询
        String moneyMin = getParamValue(params, "moneyMin");
        if (StringUtils.isNotEmpty(moneyMin)) {
            sql.append(" and EXISTS (select 1 from topp_bid b where op.bid_id = b.id_ and match_bid_money >= "
                + Double.valueOf(moneyMin) + ") ");
        }

        // 23. 最高投标金额查询
        String moneyMax = getParamValue(params, "moneyMax");
        if (StringUtils.isNotEmpty(moneyMax)) {
            sql.append(" and EXISTS (select 1 from topp_bid b where op.bid_id = b.id_ and match_bid_money <= "
                + Double.valueOf(moneyMax) + ") ");
        }

        // 24. 签约时间开始查询
        String signTimeStart = getParamValue(params, "signTimeStart");
        if (StringUtils.isNotEmpty(signTimeStart)) {
            sql.append(" and op.sign_time >= '" + signTimeStart + "'");
        }

        // 25. 签约时间结束查询
        String signTimeEnd = getParamValue(params, "signTimeEnd");
        if (StringUtils.isNotEmpty(signTimeEnd)) {
            sql.append(" and op.sign_time <= '" + signTimeEnd + "'");
        }

        // 26. 商机赢率范围查询
        String oppWinProbMin = getParamValue(params, "oppWinProbMin");
        String oppWinProbMax = getParamValue(params, "oppWinProbMax");
        if (StringUtils.isNotEmpty(oppWinProbMin) && StringUtils.isNotEmpty(oppWinProbMax)) {
            String oppWinProb = oppWinProbMin + "%-" + oppWinProbMax + "%";
            sql.append(" and op.opp_win_prob = '" + oppWinProb + "'");
        }

        // 27. 商机增长类型查询
        String oppGrowthType = getParamValue(params, "oppGrowthType");
        if (StringUtils.isNotEmpty(oppGrowthType)) {
            sql.append(" and op.opp_growth_type = '" + oppGrowthType + "'");
        }

        // 28. 预估总合同金额最小值查询
        String estimatTotalContratAmountMin = getParamValue(params, "estimatTotalContratAmountMin");
        if (StringUtils.isNotEmpty(estimatTotalContratAmountMin)) {
            sql.append(" and op.estimat_total_contrat_amount >= '" + estimatTotalContratAmountMin + "'");
        }

        // 29. 预估总合同金额最大值查询
        String estimatTotalContratAmountMax = getParamValue(params, "estimatTotalContratAmountMax");
        if (StringUtils.isNotEmpty(estimatTotalContratAmountMax)) {
            sql.append(" and op.estimat_total_contrat_amount <= '" + estimatTotalContratAmountMax + "'");
        }
    }

    /**
     * 完整商机信息流式导出到Excel文件
     * 使用OldOpportunityVO模板自动生成表头
     */
    private boolean exportToExcelStreamFull(String sql, File outputFile) throws Exception {
        // 使用 EasyExcel 分批写入数据，自动根据VO注解生成表头
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, OldOpportunityVO.class)
            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20)) // 固定列宽
            .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("商机信息").build();

            // 分批读取并写入数据
            int pageSize = 1000;
            int offset = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {
                // 构建分页SQL
                String paginatedSql = sql + " limit " + pageSize + " offset " + offset;

                // 查询当前批次数据
                List<OldOpportunityDTO> batchData = toppOpportunityMapper.selectOldOppoExportData(paginatedSql);

                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                // 补充客户经理和创建人地区信息
                enrichAreaInfo(batchData);

                // 补充合同和电子表单信息
                enrichContractAndEformInfo(batchData);

                // 转换DTO为VO
                List<OldOpportunityVO> batchVoData = BeanCopyUtils.copyList(batchData, OldOpportunityVO.class);

                // 写入当前批次数据
                excelWriter.write(batchVoData, writeSheet);
                offset += pageSize;
            }
        }
        return true;
    }

    /**
     * 保存文件到本地目录
     *
     * @param tempFile 临时文件
     * @param fileName 目标文件名
     * @return 保存的文件路径
     */
    private String saveToLocal(File tempFile, String fileName) throws Exception {
        // 配置本地下载
        String downloadDir = "C:\\Users\\FBODC\\Desktop";

        // 创建目录
        File downloadDirectory = new File(downloadDir);
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }

        // 目标文件路径
        String targetPath = downloadDir + File.separator + fileName;
        File targetFile = new File(targetPath);

        // 复制文件
        try (FileInputStream fis = new FileInputStream(tempFile);
             FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }

        log.info("文件已保存到本地: {}", targetPath);
        return targetPath;
    }

    /**
     * 补充商机客户经理和创建人地区信息
     * 查询并填充客户经理的地市和区县信息
     *
     * @param batchData 批量商机数据
     */
    private void enrichAreaInfo(List<OldOpportunityDTO> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        // 收集需要查询的人员信息
        List<Map<String, String>> personInfos = new ArrayList<>();
        Set<String> uniquePersons = new HashSet<>();

        for (OldOpportunityDTO dto : batchData) {
            if (StringUtils.isNotEmpty(dto.getCustManagerName()) &&
                StringUtils.isNotEmpty(dto.getCustManagerTel())) {
                String key = dto.getCustManagerName() + "_" + dto.getCustManagerTel();
                if (!uniquePersons.contains(key)) {
                    Map<String, String> personInfo = new HashMap<>();
                    personInfo.put("fullName", dto.getCustManagerName());
                    personInfo.put("mobile", dto.getCustManagerTel());
                    personInfos.add(personInfo);
                    uniquePersons.add(key);
                }
            }
        }

        // 查询客户经理地区信息
        if (!personInfos.isEmpty()) {
            try {
                log.debug("开始查询客户经理地区信息，需要查询人数: {}", personInfos.size());
                List<DwdRepSysPersonDVo> personAreaInfos = dwdRepSysPersonDService.selectAreaAndCounty(personInfos);

                log.debug("查询到的客户经理地区信息数量: {}", personAreaInfos != null ? personAreaInfos.size() : 0);

                // 构建姓名电话到地区信息的映射
                Map<String, DwdRepSysPersonDVo> personAreaMap = new HashMap<>();
                for (DwdRepSysPersonDVo personInfo : personAreaInfos) {
                    String key = personInfo.getFullName() + "_" + personInfo.getMobile();
                    personAreaMap.put(key, personInfo);
                }

                // 填充客户经理地区信息到DTO
                int filledCount = 0;
                for (OldOpportunityDTO dto : batchData) {
                    // 填充客户经理地区信息
                    String custManagerKey = dto.getCustManagerName() + "_" + dto.getCustManagerTel();
                    DwdRepSysPersonDVo custManagerInfo = personAreaMap.get(custManagerKey);
                    if (custManagerInfo != null) {
                        dto.setCustManagerCity(custManagerInfo.getAreaName());
                        dto.setCustManagerCounty(custManagerInfo.getCountyName());
                        filledCount++;
                    }
                }

                log.debug("成功填充客户经理地区信息的记录数: {}", filledCount);
            } catch (Exception e) {
                log.warn("查询客户经理地区信息失败: {}", e.getMessage(), e);
            }
        }

        // 将客户经理的信息复制给创建人
        for (OldOpportunityDTO dto : batchData) {
            dto.setCreatorName(dto.getCustManagerName());
            dto.setCreatorPhone(dto.getCustManagerTel());
            dto.setCreatorCity(dto.getCustManagerCity());
            dto.setCreatorCounty(dto.getCustManagerCounty());
        }

        log.debug("已完成客户经理信息查询，处理记录数: {}", batchData.size());
    }

    /**
     * 查询并填充合同信息和电子表单信息
     * @param batchData 批量商机数据
     */
    private void enrichContractAndEformInfo(List<OldOpportunityDTO> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        // 收集所有商机ID
        List<Long> opportunityIds = batchData.stream()
                .map(OldOpportunityDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (opportunityIds.isEmpty()) {
            return;
        }

        try {
            // 查询合同信息
            Map<Long, List<ToppContractVo>> contractMap = queryContractsByOpportunityIds(opportunityIds);

            // 查询电子表单信息
            Map<Long, List<ToppContractEformVo>> eformMap = queryEformsByOpportunityIds(opportunityIds);

            // 填充合同和电子表单信息到DTO
            for (OldOpportunityDTO dto : batchData) {
                Long oppId = dto.getId();

                // 填充合同信息
                List<ToppContractVo> contracts = contractMap.get(oppId);
                if (contracts != null && !contracts.isEmpty()) {
                    StringBuilder contractNames = new StringBuilder();
                    for (int i = 0; i < contracts.size(); i++) {
                        ToppContractVo contract = contracts.get(i);
                        if (i > 0) {
                            contractNames.append(",");
                        }
                        if (StringUtils.isNotEmpty(contract.getContractName())) {
                            contractNames.append(contract.getContractName());
                        }
                    }
                    dto.setContractNames(contractNames.toString());
                }

                // 填充电子表单信息
                List<ToppContractEformVo> eforms = eformMap.get(oppId);
                if (eforms != null && !eforms.isEmpty()) {
                    StringBuilder eformNames = new StringBuilder();
                    for (int i = 0; i < eforms.size(); i++) {
                        ToppContractEformVo eform = eforms.get(i);
                        if (i > 0) {
                            eformNames.append(",");
                        }
                        if (StringUtils.isNotEmpty(eform.getEformName())) {
                            eformNames.append(eform.getEformName());
                        }
                    }
                    dto.setEformNames(eformNames.toString());
                }
            }

            log.debug("已完成合同和电子表单信息查询，处理商机数量: {}", batchData.size());
        } catch (Exception e) {
            log.warn("查询合同和电子表单信息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据商机ID列表查询合同信息
     * @param opportunityIds 商机ID列表
     * @return 商机ID到合同列表的映射
     */
    private Map<Long, List<ToppContractVo>> queryContractsByOpportunityIds(List<Long> opportunityIds) {
        Map<Long, List<ToppContractVo>> contractMap = new HashMap<>();

        // 分批查询，避免一次性查询过多数据
        int batchSize = 100;
        for (int i = 0; i < opportunityIds.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, opportunityIds.size());
            List<Long> batchIds = opportunityIds.subList(i, endIndex);

            for (Long oppId : batchIds) {
                try {
                    ToppContractBo contractBo = new ToppContractBo();
                    contractBo.setProjectId(oppId);
                    contractBo.setIsValid("1"); // 只查询有效数据

                    List<ToppContractVo> contracts = contractService.queryList(contractBo);
                    if (contracts != null && !contracts.isEmpty()) {
                        contractMap.put(oppId, contracts);
                    }
                } catch (Exception e) {
                    log.warn("查询商机{}的合同信息失败: {}", oppId, e.getMessage());
                }
            }
        }

        return contractMap;
    }

    /**
     * 根据商机ID列表查询电子表单信息
     * @param opportunityIds 商机ID列表
     * @return 商机ID到电子表单列表的映射
     */
    private Map<Long, List<ToppContractEformVo>> queryEformsByOpportunityIds(List<Long> opportunityIds) {
        Map<Long, List<ToppContractEformVo>> eformMap = new HashMap<>();

        // 分批查询，避免一次性查询过多数据
        int batchSize = 100;
        for (int i = 0; i < opportunityIds.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, opportunityIds.size());
            List<Long> batchIds = opportunityIds.subList(i, endIndex);

            for (Long oppId : batchIds) {
                try {
                    ToppContractEformBo eformBo = new ToppContractEformBo();
                    eformBo.setProjectId(oppId);
                    eformBo.setIsValid(1); // 只查询有效数据

                    List<ToppContractEformVo> eforms = contractEformService.queryList(eformBo);
                    if (eforms != null && !eforms.isEmpty()) {
                        eformMap.put(oppId, eforms);
                    }
                } catch (Exception e) {
                    log.warn("查询商机{}的电子表单信息失败: {}", oppId, e.getMessage());
                }
            }
        }

        return eformMap;
    }

    /**
     * 从参数Map中获取指定参数的值
     *
     * @param params    参数Map
     * @param paramName 参数名
     * @return 参数值
     */
    private String getParamValue(Map<String, Object> params, String paramName) {
        if (params != null && params.containsKey(paramName)) {
            Object valueObj = params.get(paramName);
            if (valueObj != null) {
                if (valueObj instanceof String[]) {
                    String[] values = (String[]) valueObj;
                    if (values.length > 0) {
                        return values[0];
                    }
                } else if (valueObj instanceof String) {
                    return (String) valueObj;
                } else if (valueObj instanceof List) {
                    List<?> list = (List<?>) valueObj;
                    if (!list.isEmpty()) {
                        return String.valueOf(list.get(0));
                    }
                }
            }
        }
        return null;
    }
}
