//package com.crm.job.service.file.groupContract;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.crm.system.domain.GrpContractInfo;
//import com.crm.system.domain.ZqztGrpContractInfo;
//import com.crm.system.mapper.GrpContractInfoMapper;
//import com.crm.system.mapper.ZqztGrpContractInfoMapper;
//import com.crm.system.service.IZqztGrpContractInfoService;
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * 集团合同信息表导入定时任务<br>
// * 从Oracle库的grp_contract_info表查询数据，然后添加到MySQL主数据源的zqzt_grp_contract_info表
// *
// * @author 万超凡
// */
//@Slf4j
//@Component
//public class ZqztGrpContractInfoImportJob {
//
//    @Resource
//    private IZqztGrpContractInfoService zqztGrpContractInfoService;
//
//    @Resource
//    private GrpContractInfoMapper grpContractInfoMapper;
//
//    @Resource
//    private ZqztGrpContractInfoMapper zqztGrpContractInfoMapper;
//
//    /**
//     * 集团合同信息导入任务<br>
//     * 在xxl-job调度中心配置JobHandler为：zqztGrpContractInfoImportJob
//     */
//    @XxlJob("zqztGrpContractInfoImportJob")
//    public void execute() {
//        log.info("开始执行集团合同信息导入任务");
//
//        try {
//            // 1. 清空目标表数据
//            XxlJobHelper.log("清空目标表zqzt_grp_contract_info数据");
//            zqztGrpContractInfoService.importAllClear(null);
//
//            // 2. 分页查询grp_contract_info表的数据并处理
//            int pageSize = 1000;
//            long currentPage = 0;
//            int totalProcessed = 0;
//            boolean hasMoreData = true;
//
//            while (hasMoreData) {
//
//                Page<GrpContractInfo> page = new Page<>(currentPage + 1, pageSize);
//                Page<GrpContractInfo> resultPage = grpContractInfoMapper.selectPage(page, Wrappers.emptyWrapper());
//
//                List<GrpContractInfo> oracleDataList = resultPage.getRecords();
//
//                if (oracleDataList == null || oracleDataList.isEmpty()) {
//                    break;
//                }
//
//                // 转换数据列表
//                List<ZqztGrpContractInfo> targetDataList = convertToTargetDataList(oracleDataList);
//
//                // 批量插入数据
//                if (!targetDataList.isEmpty()) {
//                    XxlJobHelper.log("开始批量插入第" + (currentPage + 1) + "页数据，数量：" + targetDataList.size());
//                    int successCount = zqztGrpContractInfoService.importAll(targetDataList);
//                    totalProcessed += successCount;
//                    XxlJobHelper.log("第" + (currentPage + 1) + "页数据处理完成，成功导入: " + successCount + " 条");
//                }
//
//                currentPage++;
//
//                // 检查是否还有更多数据
//                if (oracleDataList.size() < pageSize) {
//                    hasMoreData = false;
//                }
//            }
//
//            XxlJobHelper.log("数据同步完成，总计处理: " + totalProcessed + " 条");
//            log.info("数据同步完成，总计处理: {} 条", totalProcessed);
//
//            String successMsg = String.format("集团合同信息导入任务执行成功，导入记录数：%d", totalProcessed);
//            XxlJobHelper.handleSuccess(successMsg);
//
//        } catch (Exception e) {
//            String errorMsg = "集团合同信息导入任务执行失败: " + e.getMessage();
//            log.error(errorMsg, e);
//            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
//            XxlJobHelper.handleFail(errorMsg);
//        }
//    }
//
//    /**
//     * 将Oracle数据转换为目标数据列表，并应用业务逻辑
//     */
//    private List<ZqztGrpContractInfo> convertToTargetDataList(List<GrpContractInfo> oracleDataList) {
//        return oracleDataList.stream()
//            .map(this::convertSingleRecord)
//            .collect(Collectors.toList());
//    }
//
//    /**
//     * 转换单条记录
//     */
//    private ZqztGrpContractInfo convertSingleRecord(GrpContractInfo oracleData) {
//        ZqztGrpContractInfo targetData = new ZqztGrpContractInfo();
//
//        // 普通字段映射
//        BeanUtils.copyProperties(oracleData, targetData);
//
//        // 特殊字段映射
//        targetData.setErpContractId(oracleData.getString2());
//        targetData.setInuseContractId(oracleData.getString15());
//        targetData.setContractProperty(oracleData.getString1());
//
//        return targetData;
//    }
//}
