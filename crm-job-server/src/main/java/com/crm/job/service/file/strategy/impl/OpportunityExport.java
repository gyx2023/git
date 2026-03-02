package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.crm.common.core.domain.dto.OldOpportunityDTO;
import com.crm.common.core.domain.entity.SysDictData;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.vo.topp.OldOpportunityVO;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.*;
import com.crm.system.service.topp.IToppContractEformService;
import com.crm.system.service.topp.IToppContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import com.crm.system.domain.vo.SysOssVo;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 商机信息导出策略实现类
 *
 * @author crm
 * @date 2026-02-10
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

    @Resource
    private ISysCodeService sysCodeService;

    @Resource
    private ISysDictTypeService sysDictTypeService;

    @Resource
    private ISysAreaService sysAreaService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    // 全局缓存
    private static final Map<String, Map<String, String>> SYS_CODE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<SysDictData>> DICT_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Long, SysAreaVo> AREA_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> ORG_NAME_CACHE = new ConcurrentHashMap<>();

    // 缓存初始化标志
    private volatile boolean cacheInitialized = false;
    private final Object cacheInitLock = new Object();

    // 创建固定线程池用于并行处理
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @PostConstruct
    public void init() {
        log.info("导出服务初始化完成");
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        clearGlobalCache();
        log.info("导出服务销毁完成");
    }

    @Override
    public String getBusinessType() {
        return "opportunity_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            log.info("开始执行商机导出任务，任务ID: {}", task.getTaskId());

            // 初始化全局缓存
            initializeGlobalCache();

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

            // 创建临时文件
            String prefix = "opportunity_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");

            try {
                boolean success = exportToExcelStreamOptimized(sql.toString(), tempFile, userId);
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

            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }

            // 定义原始中文文件名
            String originalFileName = "商机信息导出_" + System.currentTimeMillis() + ".xlsx";

            // 上传文件到OSS - 使用新方法直接设置原始文件名
            SysOssVo upload = iSysOssService.upload(tempFile, originalFileName);

            // 验证上传是否成功
            if (upload == null) {
                task.setRemark("文件上传失败");
                tempFile.delete();
                return false;
            }

            // 删除临时文件
            tempFile.delete();

            // 设置文件信息 - 使用原始中文文件名
            task.setFileName(originalFileName);
            task.setFileUrl(upload.getUrl());
            task.setRemark("商机信息导出成功");
            return true;

        } catch (Exception e) {
            log.error("商机信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 初始化全局缓存
     */
    private void initializeGlobalCache() {
        if (cacheInitialized) {
            return;
        }

        synchronized (cacheInitLock) {
            if (cacheInitialized) {
                return;
            }

            try {
                long startTime = System.currentTimeMillis();
                log.info("开始初始化全局缓存...");

                // 并行初始化各种静态数据缓存
                CompletableFuture<Void> sysCodeFuture = CompletableFuture.runAsync(this::initSysCodeCache, executorService);
                CompletableFuture<Void> dictTypeFuture = CompletableFuture.runAsync(this::initDictTypeCache, executorService);
                CompletableFuture<Void> areaFuture = CompletableFuture.runAsync(this::initAreaCache, executorService);

                // 等待所有缓存初始化完成
                CompletableFuture.allOf(sysCodeFuture, dictTypeFuture, areaFuture).join();

                cacheInitialized = true;
                long endTime = System.currentTimeMillis();
                log.info("全局缓存初始化完成，耗时: {}ms", (endTime - startTime));

            } catch (Exception e) {
                log.error("全局缓存初始化失败", e);
                throw new RuntimeException("全局缓存初始化失败", e);
            }
        }
    }

    /**
     * 初始化码表缓存
     */
    private void initSysCodeCache() {
        try {
            long startTime = System.currentTimeMillis();

            // 预定义需要缓存的码表类型
            String[] codeTypes = {
                "opp_type", "unicom_type", "clue_source",
                "oppo_state", "select_type_1", "main_product"
            };

            for (String type : codeTypes) {
                try {
                    SysCodeBo queryBo = new SysCodeBo();
                    queryBo.setType(type);

                    List<SysCodeVo> codeList = sysCodeService.queryList(queryBo);
                    Map<String, String> codeMap = new HashMap<>();

                    if (codeList != null && !codeList.isEmpty()) {
                        for (SysCodeVo sysCode : codeList) {
                            if (sysCode.getCode() != null && sysCode.getName() != null) {
                                codeMap.put(sysCode.getCode(), sysCode.getName());
                            }
                        }
                    }

                    SYS_CODE_CACHE.put(type, codeMap);
                    log.debug("码表缓存初始化完成: {}，条目数: {}", type, codeMap.size());

                } catch (Exception e) {
                    log.warn("初始化码表缓存失败，type: {}", type, e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("码表缓存初始化完成，耗时: {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.error("码表缓存初始化异常", e);
        }
    }

    /**
     * 初始化字典类型缓存
     */
    private void initDictTypeCache() {
        try {
            long startTime = System.currentTimeMillis();

            // 预定义需要缓存的字典类型
            String[] dictTypes = {"opp_growth_type"};

            for (String type : dictTypes) {
                try {
                    List<SysDictData> dictList = sysDictTypeService.selectDictDataByType(type);
                    DICT_TYPE_CACHE.put(type, dictList != null ? dictList : new ArrayList<>());
                    log.debug("字典类型缓存初始化完成: {}，条目数: {}", type,
                        dictList != null ? dictList.size() : 0);

                } catch (Exception e) {
                    log.warn("初始化字典类型缓存失败，type: {}", type, e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("字典类型缓存初始化完成，耗时: {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.error("字典类型缓存初始化异常", e);
        }
    }

    /**
     * 初始化地区缓存
     */
    private void initAreaCache() {
        try {
            long startTime = System.currentTimeMillis();

            // 查询所有地区信息
            List<SysAreaVo> allAreas = sysAreaService.queryList(null);

            if (allAreas != null && !allAreas.isEmpty()) {
                for (SysAreaVo area : allAreas) {
                    if (area.getId() != null) {
                        AREA_CACHE.put(area.getId(), area);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("地区缓存初始化完成，总条目数: {}，耗时: {}ms", AREA_CACHE.size(), (endTime - startTime));

        } catch (Exception e) {
            log.error("地区缓存初始化异常", e);
        }
    }

    /**
     * 清理全局缓存
     */
    private void clearGlobalCache() {
        SYS_CODE_CACHE.clear();
        DICT_TYPE_CACHE.clear();
        AREA_CACHE.clear();
        ORG_NAME_CACHE.clear();
        cacheInitialized = false;
        log.info("全局缓存已清理");
    }

    /**
     * 商机信息流式导出到Excel文件
     */
    private boolean exportToExcelStreamOptimized(String baseSql, File outputFile, Long userId) throws Exception {
        // 使用 EasyExcel 分批写入数据
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, OldOpportunityVO.class)
            .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20))
            .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("商机信息").build();

            int pageSize = 3000;
            Long lastMaxId = null;
            boolean hasMoreData = true;

            while (hasMoreData) {

                String paginatedSql = buildCursorPaginationSql(baseSql, lastMaxId, pageSize);

                // 查询当前批次数据
                List<OldOpportunityDTO> batchData = toppOpportunityMapper.selectOldOppoExportData(paginatedSql);

                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                log.info("处理第{}批次数据，数量: {}",
                    lastMaxId == null ? 1 : (int)(lastMaxId / pageSize) + 1, batchData.size());

                // 更新游标
                lastMaxId = batchData.stream()
                    .map(OldOpportunityDTO::getId)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(null);

                // 批量预加载动态数据
                BatchEnrichmentContext context = preloadDynamicDataOptimized(batchData);

                // 并行处理各项补充信息
                CompletableFuture<Void> areaFuture = CompletableFuture.runAsync(() ->
                    enrichAreaInfoOptimized(batchData, context), executorService);

                CompletableFuture<Void> contractFuture = CompletableFuture.runAsync(() ->
                    enrichContractAndEformInfoOptimized(batchData, context), executorService);

                CompletableFuture<Void> productFuture = CompletableFuture.runAsync(() ->
                    enrichMainProductInfoOptimized(batchData), executorService);

                CompletableFuture<Void> supportManagerFuture = CompletableFuture.runAsync(() ->
                    enrichSupportManagerInfoOptimized(batchData, context), executorService);

                // 等待所有并行任务完成
                CompletableFuture.allOf(areaFuture, contractFuture, productFuture, supportManagerFuture).join();

                // 进行翻译
                translateSysCodeFieldsOptimized(batchData);

                // 转换DTO为VO
                List<OldOpportunityVO> batchVoData = BeanCopyUtils.copyList(batchData, OldOpportunityVO.class);

                // 写入当前批次数据
                excelWriter.write(batchVoData, writeSheet);

                log.info("第{}批次数据处理完成", lastMaxId == null ? 1 : (int)(lastMaxId / pageSize));
            }
        }
        return true;
    }

    /**
     * 构建游标分页SQL
     */
    private String buildCursorPaginationSql(String baseSql, Long lastMaxId, int pageSize) {
        StringBuilder cursorSql = new StringBuilder(baseSql);

        if (lastMaxId != null) {
            // 添加游标条件
            if (baseSql.toLowerCase().contains("where")) {
                cursorSql.append(" AND op.id_ > ").append(lastMaxId);
            } else {
                cursorSql.append(" WHERE op.id_ > ").append(lastMaxId);
            }
        }

        // 添加排序和限制
        cursorSql.append(" ORDER BY op.id_ DESC LIMIT ").append(pageSize);
        return cursorSql.toString();
    }

    /**
     * 动态数据预加载
     */
    private BatchEnrichmentContext preloadDynamicDataOptimized(List<OldOpportunityDTO> batchData) {
        BatchEnrichmentContext context = new BatchEnrichmentContext();

        try {
            long startTime = System.currentTimeMillis();

            // 收集所有需要查询的ID
            Set<Long> opportunityIds = batchData.stream()
                .map(OldOpportunityDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Set<Long> custManagerIds = batchData.stream()
                .map(dto -> dto.getCustManagerId() != null ? Long.valueOf(dto.getCustManagerId()) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            // 并行预加载动态数据
            CompletableFuture<Void> contractFuture = CompletableFuture.runAsync(() -> {
                context.contractMap = queryContractsByOpportunityIdsOptimized(new ArrayList<>(opportunityIds));
            }, executorService);

            CompletableFuture<Void> eformFuture = CompletableFuture.runAsync(() -> {
                context.eformMap = queryEformsByOpportunityIdsOptimized(new ArrayList<>(opportunityIds));
            }, executorService);

            CompletableFuture<Void> orgFuture = CompletableFuture.runAsync(() -> {
                context.orgNameMap = queryCustManagerOrgNamesOptimized(new ArrayList<>(custManagerIds));
            }, executorService);

            CompletableFuture<Void> areaFuture = CompletableFuture.runAsync(() -> {
                context.areaInfoMap = queryCustManagerAreaInfoOptimized(new ArrayList<>(custManagerIds));
            }, executorService);

            CompletableFuture<Void> personAreaFuture = CompletableFuture.runAsync(() -> {
                context.personAreaMap = queryPersonAreaInfo(batchData);
            }, executorService);

            CompletableFuture<Void> supportManagerFuture = CompletableFuture.runAsync(() -> {
                context.supportManagerInfoMap = querySupportManagerInfoOptimized(new ArrayList<>(custManagerIds));
            }, executorService);

            // 等待所有预加载完成
            CompletableFuture.allOf(contractFuture, eformFuture, orgFuture, areaFuture, personAreaFuture, supportManagerFuture).join();

            long endTime = System.currentTimeMillis();
            log.info("动态数据预加载完成，耗时: {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.warn("动态数据预加载失败: {}", e.getMessage(), e);
        }

        return context;
    }

    /**
     * 地区信息补充
     */
    private void enrichAreaInfoOptimized(List<OldOpportunityDTO> batchData, BatchEnrichmentContext context) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            batchData.parallelStream().forEach(dto -> {
                if (dto.getCustManagerId() != null) {
                    Long managerId = Long.valueOf(dto.getCustManagerId());

                    // 填充组织机构信息（优先使用缓存）
                    String orgName = context.orgNameMap.get(managerId);
                    if (StringUtils.isNotEmpty(orgName)) {
                        dto.setCustManagerOrg(orgName);
                    }

                    // 填充地区信息（优先使用缓存）
                    SysAreaVo areaInfo = context.areaInfoMap.get(managerId);
                    if (areaInfo != null) {
                        if ("一级".equals(areaInfo.getType())) {
                            dto.setCustManagerCity("省本级");
                            dto.setCustManagerCounty("");
                        } else if ("二级".equals(areaInfo.getType())) {
                            dto.setCustManagerCity(areaInfo.getName());
                            dto.setCustManagerCounty("市本级");
                        } else if ("三级".equals(areaInfo.getType())) {
                            String parentAreaName = getCachedParentAreaName(areaInfo.getParent());
                            dto.setCustManagerCity(parentAreaName);
                            dto.setCustManagerCounty(areaInfo.getName());
                        }
                    }
                }

                // 填充客户经理地区信息
                String personKey = dto.getCustManagerName() + "_" + dto.getCustManagerTel();
                DwdRepSysPersonDVo personInfo = context.personAreaMap.get(personKey);
                if (personInfo != null) {
                    dto.setCustManagerCity(personInfo.getAreaName());
                    dto.setCustManagerCounty(personInfo.getCountyName());
                }

                // 复制给创建人
                dto.setCreatorName(dto.getCustManagerName());
                dto.setCreatorPhone(dto.getCustManagerTel());
                dto.setCreatorCity(dto.getCustManagerCity());
                dto.setCreatorCounty(dto.getCustManagerCounty());
                dto.setCreatorOrg(dto.getCustManagerOrg());
            });

            long endTime = System.currentTimeMillis();
            log.debug("地区信息补充完成，处理记录数: {}, 耗时: {}ms", batchData.size(), (endTime - startTime));

        } catch (Exception e) {
            log.warn("地区信息补充失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从缓存中获取父级地区名称
     */
    private String getCachedParentAreaName(Long parentId) {
        if (parentId == null) {
            return "";
        }

        SysAreaVo parentArea = AREA_CACHE.get(parentId);
        return parentArea != null ? parentArea.getName() : "";
    }

    /**
     * 合同和电子表单信息补充
     */
    private void enrichContractAndEformInfoOptimized(List<OldOpportunityDTO> batchData, BatchEnrichmentContext context) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // 处理合同和电子表单信息补充
            batchData.parallelStream().forEach(dto -> {
                Long oppId = dto.getId();

                // 填充合同信息
                List<ToppContractVo> contracts = context.contractMap.get(oppId);
                if (contracts != null && !contracts.isEmpty()) {
                    StringBuilder contractNames = new StringBuilder();
                    for (int i = 0; i < contracts.size(); i++) {
                        ToppContractVo contract = contracts.get(i);
                        if (i > 0) contractNames.append(",");
                        if (StringUtils.isNotEmpty(contract.getContractName())) {
                            contractNames.append(contract.getContractName());
                        }
                    }
                    dto.setContractNames(contractNames.toString());
                }

                // 填充电子表单信息
                List<ToppContractEformVo> eforms = context.eformMap.get(oppId);
                if (eforms != null && !eforms.isEmpty()) {
                    StringBuilder eformNames = new StringBuilder();
                    for (int i = 0; i < eforms.size(); i++) {
                        ToppContractEformVo eform = eforms.get(i);
                        if (i > 0) eformNames.append(",");
                        if (StringUtils.isNotEmpty(eform.getEformName())) {
                            eformNames.append(eform.getEformName());
                        }
                    }
                    dto.setEformNames(eformNames.toString());
                }
            });

            long endTime = System.currentTimeMillis();
            log.debug("合同信息补充完成，耗时: {}ms", (endTime - startTime));

        } catch (Exception e) {
            log.warn("合同信息补充失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 支撑经理信息补充
     */
    private void enrichSupportManagerInfoOptimized(List<OldOpportunityDTO> batchData, BatchEnrichmentContext context) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            batchData.parallelStream().forEach(dto -> {
                if (dto.getCustManagerId() != null) {
                    Long managerId = Long.valueOf(dto.getCustManagerId());

                    // 从缓存中获取支撑经理完整信息
                    Map<String, Object> supportManagerInfo = context.supportManagerInfoMap.get(managerId);
                    if (supportManagerInfo != null) {
                        // 填充支撑经理姓名
                        String managerName = (String) supportManagerInfo.get("managerName");
                        if (StringUtils.isNotEmpty(managerName)) {
                            dto.setDesignerName(managerName);
                        }

                        // 填充支撑经理电话
                        String managerPhone = (String) supportManagerInfo.get("managerPhone");
                        if (StringUtils.isNotEmpty(managerPhone)) {
                            dto.setDesignerPhone(managerPhone);
                        }

                        // 填充支撑经理归属部门
                        String orgName = (String) supportManagerInfo.get("orgName");
                        if (StringUtils.isNotEmpty(orgName)) {
                            dto.setDesignerOrg(orgName);
                        }

                        // 填充支撑经理地区信息
                        Object areaIdObj = supportManagerInfo.get("areaId");
                        String areaName = (String) supportManagerInfo.get("areaName");
                        String areaType = (String) supportManagerInfo.get("areaType");
                        Object parentAreaIdObj = supportManagerInfo.get("parentAreaId");

                        if (areaIdObj != null && StringUtils.isNotEmpty(areaName) && StringUtils.isNotEmpty(areaType)) {
                            Long areaId = convertToLong(areaIdObj);
                            Long parentAreaId = convertToLong(parentAreaIdObj);

                            // 根据地区类型设置地市和区县
                            if ("一级".equals(areaType)) {
                                dto.setDesignerCity("省本级");
                                dto.setDesignerCounty("");
                            } else if ("二级".equals(areaType)) {
                                dto.setDesignerCity(areaName);
                                dto.setDesignerCounty("市本级");
                            } else if ("三级".equals(areaType)) {
                                String parentAreaName = getCachedParentAreaName(parentAreaId);
                                dto.setDesignerCity(parentAreaName);
                                dto.setDesignerCounty(areaName);
                            }
                        }
                    }
                }
            });

            long endTime = System.currentTimeMillis();
            log.debug("支撑经理信息补充完成，处理记录数: {}, 耗时: {}ms", batchData.size(), (endTime - startTime));

        } catch (Exception e) {
            log.warn("支撑经理信息补充失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 主要产品信息补充
     */
    private void enrichMainProductInfoOptimized(List<OldOpportunityDTO> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        // 处理主要产品信息补充
        batchData.parallelStream().forEach(dto -> {
            String productCode = dto.getProductCode();
            String oppType = dto.getOppType();
            String sfProductName = dto.getSfProductName();

            if ("1".equals(productCode) || StringUtils.isEmpty(productCode)) {
                if (StringUtils.isNotEmpty(sfProductName)) {
                    dto.setMainProduct(sfProductName);
                }
                return;
            }

            // 根据业务类型确定settingStr1的值
            String settingStr1 = null;
            if ("opp_type_np".equals(oppType) || "opp_type_spy".equals(oppType)
                || "opp_type_yzy".equals(oppType) || "opp_type_yyy".equals(oppType)) {
                settingStr1 = "400301";
            } else if ("opp_type_zwyw".equals(oppType)) {
                settingStr1 = "400302";
            }

            // 查询main_product码表（使用全局缓存）
            if (StringUtils.isNotEmpty(settingStr1)) {
                try {
                    Map<String, String> mainProductMap = SYS_CODE_CACHE.get("main_product");
                    if (mainProductMap != null) {
                        String productName = mainProductMap.get(productCode);
                        if (StringUtils.isNotEmpty(productName)) {
                            dto.setMainProduct(productName);
                        }
                    }
                } catch (Exception e) {
                    log.warn("查询产品编码信息失败，productCode: {}, oppType: {}, error: {}",
                        productCode, oppType, e.getMessage());
                }
            }
        });

        log.debug("已完成主要产品信息补充，处理记录数: {}", batchData.size());
    }

    /**
     * 码表翻译
     */
    private void translateSysCodeFieldsOptimized(List<OldOpportunityDTO> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            return;
        }

        // 一次性获取所有需要的缓存Map到局部变量
        final Map<String, String> oppTypeMap = SYS_CODE_CACHE.get("opp_type");
        final Map<String, String> unicomTypeMap = SYS_CODE_CACHE.get("unicom_type");
        final Map<String, String> clueSourceMap = SYS_CODE_CACHE.get("clue_source");
        final Map<String, String> oppoStateMap = SYS_CODE_CACHE.get("oppo_state");
        final Map<String, String> selectType1Map = SYS_CODE_CACHE.get("select_type_1");

        // 字典类型缓存
        List<SysDictData> growthTypeDictList = DICT_TYPE_CACHE.get("opp_growth_type");
        final Map<String, String> oppGrowthTypeMap = new HashMap<>();
        if (growthTypeDictList != null && !growthTypeDictList.isEmpty()) {
            oppGrowthTypeMap.putAll(growthTypeDictList.stream()
                .collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getDictLabel)));
        }

        // isValid字段映射：1->有效, 0->无效（静态映射）
        Map<String, String> isValidMap = new HashMap<>();
        isValidMap.put("1", "有效");
        isValidMap.put("0", "无效");

        // 翻译处理
        batchData.parallelStream().forEach(dto -> {
            if (StringUtils.isNotEmpty(dto.getOppType()) && oppTypeMap != null) {
                dto.setOppType(oppTypeMap.getOrDefault(dto.getOppType(), dto.getOppType()));
            }

            if (StringUtils.isNotEmpty(dto.getUnicomType()) && unicomTypeMap != null) {
                dto.setUnicomType(unicomTypeMap.getOrDefault(dto.getUnicomType(), dto.getUnicomType()));
            }

            if (StringUtils.isNotEmpty(dto.getOppSource()) && clueSourceMap != null) {
                dto.setOppSource(clueSourceMap.getOrDefault(dto.getOppSource(), dto.getOppSource()));
            }

            if (StringUtils.isNotEmpty(dto.getOppoState()) && oppoStateMap != null) {
                dto.setOppoState(oppoStateMap.getOrDefault(dto.getOppoState(), dto.getOppoState()));
            }

            if (StringUtils.isNotEmpty(dto.getIsBidding()) && selectType1Map != null) {
                dto.setIsBidding(selectType1Map.getOrDefault(dto.getIsBidding(), dto.getIsBidding()));
            }

            if (StringUtils.isNotEmpty(dto.getIsValid())) {
                dto.setIsValid(isValidMap.getOrDefault(dto.getIsValid(), dto.getIsValid()));
            }

            if (StringUtils.isNotEmpty(dto.getOppGrowthType()) && oppGrowthTypeMap != null) {
                dto.setOppGrowthType(oppGrowthTypeMap.getOrDefault(dto.getOppGrowthType(), dto.getOppGrowthType()));
            }

            if (StringUtils.isNotEmpty(dto.getOppIncludeType()) && oppTypeMap != null) {
                String oppIncludeType = dto.getOppIncludeType();
                StringBuilder translatedResult = new StringBuilder();

                // 去除首尾逗号
                if (oppIncludeType.startsWith(",")) {
                    oppIncludeType = oppIncludeType.substring(1);
                }
                if (oppIncludeType.endsWith(",")) {
                    oppIncludeType = oppIncludeType.substring(0, oppIncludeType.length() - 1);
                }

                // 按逗号分割并逐个翻译
                if (oppIncludeType.contains(",")) {
                    // 多个业务类型
                    String[] typeCodes = oppIncludeType.split(",");
                    for (String typeCode : typeCodes) {
                        if (StringUtils.isNotEmpty(typeCode)) {
                            String translated = oppTypeMap.getOrDefault(typeCode.trim(), typeCode.trim());
                            if (translatedResult.length() > 0) {
                                translatedResult.append(" ");
                            }
                            translatedResult.append(translated);
                        }
                    }
                } else {
                    // 单个业务类型
                    String translated = oppTypeMap.getOrDefault(oppIncludeType, oppIncludeType);
                    translatedResult.append(translated);
                }

                dto.setOppIncludeType(translatedResult.toString());
            }
        });

        log.debug("码表翻译，处理记录数: {}", batchData.size());
    }

    /**
     * 解析请求参数
     */
    private Map<String, Object> parseRequestParams(FileAsyncTaskDo task) {
        if (task.getRequestParams() != null) {
            return JSONUtil.toBean(task.getRequestParams(), Map.class);
        }
        return new HashMap<>();
    }

    /**
     * 构建基础SQL查询语句
     */
    private StringBuffer buildBaseSql(Long userId) {
        StringBuffer sql = new StringBuffer();
        sql.append("select * from topp_opportunity op " +
            "join sys_user su on su.id_ = op.cust_manager_id " +
            "left join tcomm_customer customer on op.cust_id = customer.id_ where" +
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

    /**
     * 添加所有查询条件
     */
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
            // 处理空字符串情况
            if (oppoStates.trim().isEmpty()) {
                return;
            }

            if (oppoStates.contains(",")) {
                String[] states = oppoStates.split(",");
                sql.append(" and op.oppo_state in (");
                for (int i = 0; i < states.length; i++) {
                    if (i > 0) {
                        sql.append(",");
                    }
                    sql.append("'").append(states[i].trim()).append("'");
                }
                sql.append(")");
            } else {
                sql.append(" and op.oppo_state = '").append(oppoStates.trim()).append("'");
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
            sql.append(" and op.cust_manager_name like '%" + custManagerName + "%'");
        }

        // 7. 区域ID查询 - 通过客户经理的组织机构地区关联查询
        String areaId = getParamValue(params, "areaId");
        if (StringUtils.isNotEmpty(areaId)) {
            // 通过关联关系查询：opportunity.cust_manager_id → sys_user.org_id → sys_org.area_id
            if (areaId.length() == 4) {
                // 4位地区编码，使用模糊匹配
                sql.append(" and EXISTS (SELECT 1 FROM sys_user su2 " +
                    "JOIN sys_org o2 ON su2.org_id = o2.id_ " +
                    "WHERE su2.id_ = op.cust_manager_id " +
                    "AND o2.area_id LIKE '%" + areaId.trim() + "%')");
            } else {
                // 精确匹配
                sql.append(" and EXISTS (SELECT 1 FROM sys_user su2 " +
                    "JOIN sys_org o2 ON su2.org_id = o2.id_ " +
                    "WHERE su2.id_ = op.cust_manager_id " +
                    "AND o2.area_id = '" + areaId + "')");
            }
        }

        // 8. 开发标识查询 - 通过客户表关联查询
        String developFlag = getParamValue(params, "developFlag");
        if (StringUtils.isNotEmpty(developFlag)) {
            sql.append(" and customer.develop_flag = '" + developFlag + "'");
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
     * 查询人员地区信息
     */
    private Map<String, DwdRepSysPersonDVo> queryPersonAreaInfo(List<OldOpportunityDTO> batchData) {
        Map<String, DwdRepSysPersonDVo> personAreaMap = new HashMap<>();

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
                List<DwdRepSysPersonDVo> personAreaInfos = dwdRepSysPersonDService.selectAreaAndCounty(personInfos);

                // 构建姓名电话到地区信息的映射
                for (DwdRepSysPersonDVo personInfo : personAreaInfos) {
                    String key = personInfo.getFullName() + "_" + personInfo.getMobile();
                    personAreaMap.put(key, personInfo);
                }

                log.debug("查询到人员地区信息数量: {}", personAreaInfos.size());
            } catch (Exception e) {
                log.warn("查询人员地区信息失败: {}", e.getMessage(), e);
            }
        }

        return personAreaMap;
    }

    /**
     * 查询客户经理对应的组织机构名称
     */
    private Map<Long, String> queryCustManagerOrgNamesOptimized(List<Long> custManagerIds) {
        Map<Long, String> orgNameMap = new HashMap<>();

        if (custManagerIds == null || custManagerIds.isEmpty()) {
            return orgNameMap;
        }

        try {
            // 批量查询
            List<Map<String, Object>> batchResult = toppOpportunityMapper.selectCustManagerOrgInfoBatch(custManagerIds);

            if (batchResult != null && !batchResult.isEmpty()) {
                for (Map<String, Object> row : batchResult) {
                    Object userIdObj = row.get("userId");
                    String orgName = (String) row.get("orgName");

                    // 类型转换
                    Long userId = convertToLong(userIdObj);
                    if (userId != null && StringUtils.isNotEmpty(orgName)) {
                        orgNameMap.put(userId, orgName);
                    }
                }
            }

            log.debug("查询完成，查询到组织信息数量: {}", batchResult != null ? batchResult.size() : 0);

        } catch (Exception e) {
            log.warn("查询客户经理组织信息失败: {}", e.getMessage(), e);
        }

        return orgNameMap;
    }

    /**
     * 查询客户经理对应的组织机构地区信息
     */
    private Map<Long, SysAreaVo> queryCustManagerAreaInfoOptimized(List<Long> custManagerIds) {
        Map<Long, SysAreaVo> areaInfoMap = new HashMap<>();

        if (custManagerIds == null || custManagerIds.isEmpty()) {
            return areaInfoMap;
        }

        try {
            // 批量查询
            List<Map<String, Object>> batchResult = toppOpportunityMapper.selectCustManagerAreaInfoBatch(custManagerIds);

            if (batchResult != null && !batchResult.isEmpty()) {
                for (Map<String, Object> row : batchResult) {
                    Object userIdObj = row.get("userId");
                    Object areaIdObj = row.get("areaId");
                    String areaName = (String) row.get("areaName");
                    String areaType = (String) row.get("areaType");
                    Object parentAreaIdObj = row.get("parentAreaId");

                    // 类型转换
                    Long userId = convertToLong(userIdObj);
                    Long areaId = convertToLong(areaIdObj);
                    Long parentAreaId = convertToLong(parentAreaIdObj);

                    if (userId != null && areaId != null &&
                        StringUtils.isNotEmpty(areaName) && StringUtils.isNotEmpty(areaType)) {
                        SysAreaVo areaInfo = new SysAreaVo();
                        areaInfo.setId(areaId);
                        areaInfo.setName(areaName);
                        areaInfo.setType(areaType);
                        areaInfo.setParent(parentAreaId);
                        areaInfoMap.put(userId, areaInfo);
                    }
                }
            }

            log.debug("查询完成，查询到组织结构地区信息数量: {}", batchResult != null ? batchResult.size() : 0);

        } catch (Exception e) {
            log.warn("查询客户经理组织结构地区信息失败: {}", e.getMessage(), e);
        }

        return areaInfoMap;
    }

    /**
     * 类型转换
     */
    private Long convertToLong(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                log.warn("无法将字符串转换为Long类型: {}", obj);
                return null;
            }
        }
        return null;
    }

    /**
     * 查询合同信息
     */
    private Map<Long, List<ToppContractVo>> queryContractsByOpportunityIdsOptimized(List<Long> opportunityIds) {
        Map<Long, List<ToppContractVo>> contractMap = new HashMap<>();

        if (opportunityIds == null || opportunityIds.isEmpty()) {
            return contractMap;
        }

        try {
            // 批量查询
            List<ToppContractVo> allContracts = contractService.selectByOpportunityIds(opportunityIds);

            // 按商机ID分组
            contractMap = allContracts.stream()
                .collect(Collectors.groupingBy(ToppContractVo::getProjectId));

        } catch (Exception e) {
            log.warn("查询合同信息失败: {}", e.getMessage(), e);
        }

        return contractMap;
    }

    /**
     * 查询支撑经理完整信息
     */
    private Map<Long, Map<String, Object>> querySupportManagerInfoOptimized(List<Long> custManagerIds) {
        Map<Long, Map<String, Object>> supportManagerInfoMap = new HashMap<>();

        if (custManagerIds == null || custManagerIds.isEmpty()) {
            return supportManagerInfoMap;
        }

        try {
            // 批量查询支撑经理完整信息
            List<Map<String, Object>> batchResult = toppOpportunityMapper.selectSupportManagerInfoBatch(custManagerIds);

            if (batchResult != null && !batchResult.isEmpty()) {
                for (Map<String, Object> row : batchResult) {
                    Object userIdObj = row.get("userId");
                    Long userId = convertToLong(userIdObj);

                    if (userId != null) {
                        supportManagerInfoMap.put(userId, row);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("查询支撑经理信息失败: {}", e.getMessage(), e);
        }

        return supportManagerInfoMap;
    }

    /**
     * 查询电子表单信息
     */
    private Map<Long, List<ToppContractEformVo>> queryEformsByOpportunityIdsOptimized(List<Long> opportunityIds) {
        Map<Long, List<ToppContractEformVo>> eformMap = new HashMap<>();

        if (opportunityIds == null || opportunityIds.isEmpty()) {
            return eformMap;
        }

        try {
            // 批量查询
            List<ToppContractEformVo> allEforms = contractEformService.selectByOpportunityIds(opportunityIds);

            // 按商机ID分组
            eformMap = allEforms.stream()
                .collect(Collectors.groupingBy(ToppContractEformVo::getProjectId));

        } catch (Exception e) {
            log.warn("查询电子表单信息失败: {}", e.getMessage(), e);
        }

        return eformMap;
    }

    /**
     * 从参数Map中获取指定参数的值
     * 支持多选参数的处理
     */
    private String getParamValue(Map<String, Object> params, String paramName) {
        if (params != null && params.containsKey(paramName)) {
            Object valueObj = params.get(paramName);
            if (valueObj != null) {
                if (valueObj instanceof String[]) {
                    String[] values = (String[]) valueObj;
                    if (values.length > 0) {
                        // 对于多选参数，将数组元素用逗号连接
                        return String.join(",", values);
                    }
                } else if (valueObj instanceof String) {
                    return (String) valueObj;
                } else if (valueObj instanceof List) {
                    List<?> list = (List<?>) valueObj;
                    if (!list.isEmpty()) {
                        // 对于List类型，将元素用逗号连接
                        return list.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                    }
                }
            }
        }
        return null;
    }


    /**
     * 批量预加载上下文类
     */
    private static class BatchEnrichmentContext {
        Map<Long, List<ToppContractVo>> contractMap = new HashMap<>();
        Map<Long, List<ToppContractEformVo>> eformMap = new HashMap<>();
        Map<Long, String> orgNameMap = new HashMap<>();
        Map<Long, SysAreaVo> areaInfoMap = new HashMap<>();
        Map<String, DwdRepSysPersonDVo> personAreaMap = new HashMap<>();
        Map<Long, Map<String, Object>> supportManagerInfoMap = new HashMap<>();
    }
}
