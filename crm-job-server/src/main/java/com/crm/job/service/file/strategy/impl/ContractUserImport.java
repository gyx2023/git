package com.crm.job.service.file.strategy.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.ContractUserTemplateResultVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.listener.client.ContractUserImportListener;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.service.ISysOssService;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 合同关联用户导入策略实现类
 * 处理合同关联用户数据导入的具体任务逻辑
 * 适配：1. Excel有表头（第一行“服务号码”）自动跳过 2. 无CellData依赖（兼容低版本EasyExcel）
 */
@Component
@Slf4j
public class ContractUserImport extends AbstractImportStrategy {

    @Resource
    private ISysOssService sysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Resource
    private ContractUserImportListener contractUserImportListener;

    @Override
    public String getBusinessType() {
        return "contract_user_import";
    }

    @Override
    protected boolean doImportTask(FileAsyncTaskDo task) throws Exception {
        // 根据FileAsyncTaskDO获取上传到OSS的文件URL
        String fileUrl = task.getFileUrl();
        log.info("开始处理合同关联用户导入任务，文件URL: {}", fileUrl);

        // 检查URL是否为空
        if (!StringUtils.hasText(fileUrl)) {
            log.error("文件URL为空");
            task.setRemark("处理失败：文件URL为空");
            return false;
        }

        // 先尝试直接精确匹配URL
        SysOss sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, fileUrl));

        // 如果没找到，并且URL包含查询参数，则尝试去除查询参数后再查找
        if (sysOss == null && StringUtils.hasText(fileUrl) && fileUrl.contains("?")) {
            String baseUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            log.info("完整URL未找到记录，尝试使用基础URL查询: {}", baseUrl);
            sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, baseUrl));
        }

        // 如果仍然没找到，记录所有存在的URL供调试
        if (sysOss == null) {
            List<SysOss> allOssRecords = sysOssMapper.selectList(new LambdaQueryWrapper<SysOss>().isNotNull(SysOss::getUrl));
            log.info("未找到对应的OSS文件记录，文件URL: {}，系统中存在的URL记录数量: {}", fileUrl, allOssRecords.size());
            for (SysOss oss : allOssRecords) {
                log.info("系统中存在的URL: {}", oss.getUrl());
            }
            task.setRemark("处理失败：未找到对应的OSS文件记录，文件URL: " + fileUrl);
            return false;
        }

        // 读取Excel文件，获取服务号码列表
        List<String> svcNumList = new ArrayList<>();
        // ========== 核心修改：无CellData依赖 + 跳过表头 + 流自动关闭 ==========
        try (InputStream fileContent = sysOssService.getFileContent(sysOss.getOssId())) {
            // 关键：改用Map<Integer, Object>泛型，完全避开CellData，适配低版本EasyExcel
            List<Map<Integer, Object>> excelDataList = EasyExcel.read(fileContent)
                .sheet() // 读取第一个sheet
                .headRowNumber(1) // 跳过第一行“服务号码”表头，从第二行读数据
                .doReadSync(); // 同步读取

            // 遍历每一行数据（从第二行开始）
            for (Map<Integer, Object> data : excelDataList) {
                // 跳过空行/无数据行
                if (data == null || data.isEmpty()) {
                    continue;
                }
                // 获取第一列（索引0）的原始值，无CellData封装
                Object svcNumObj = data.get(0);
                // 跳过第一列为空的行
                if (svcNumObj == null) {
                    continue;
                }
                // 转文本并去空格，保留原有非空判断逻辑
                String svcNum = svcNumObj.toString().trim();
                if (StringUtils.hasText(svcNum)) {
                    svcNumList.add(svcNum);
                }
            }
        } catch (Exception e) {
            // 捕获Excel读取/流操作异常，记录日志并设置任务失败信息
            log.error("读取合同关联用户Excel文件失败，OSSID：{}", sysOss.getOssId(), e);
            task.setRemark("处理失败：读取Excel文件异常，" + e.getMessage());
            return false;
        }

        // 获取任务参数中的合同ID和总部集团编码
        String contractId = "";
        String hqGrpCode = "";
        String requestParams = task.getRequestParams();
        if (StringUtils.hasText(requestParams)) {
            try {
                // 解析JSON格式的请求参数
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(requestParams);
                if (jsonObject != null) {
                    Object contractIdObj = jsonObject.get("contractId");
                    Object hqGrpCodeObj = jsonObject.get("hqGrpCode");
                    if (contractIdObj != null) {
                        contractId = contractIdObj.toString();
                    }
                    if (hqGrpCodeObj != null) {
                        hqGrpCode = hqGrpCodeObj.toString();
                    }
                }
            } catch (Exception e) {
                log.error("解析请求参数失败", e);
                task.setRemark("处理失败：解析请求参数失败");
                return false;
            }
        }

        if (!StringUtils.hasText(contractId)) {
            task.setRemark("处理失败：合同ID为空");
            return false;
        }

        if (!StringUtils.hasText(hqGrpCode)) {
            task.setRemark("处理失败：总部集团编码为空");
            return false;
        }

        // 处理数据并生成结果列表
        List<ContractUserTemplateResultVo> resultList = contractUserImportListener.processImportData(svcNumList, contractId, hqGrpCode);

        // 创建临时文件 - 使用英文文件名避免系统级中文文件名问题
        String prefix = "contract_user_import_" + System.currentTimeMillis();
        File tempFile = File.createTempFile(prefix, ".xlsx");

        // 定义原始中文文件名（用于用户下载时显示）
        String originalFileName = "合同关联用户导入结果_" + System.currentTimeMillis() + ".xlsx";

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            EasyExcel.write(fos, ContractUserTemplateResultVo.class).sheet("导入结果").doWrite(resultList);
        }

        // 验证文件是否创建成功
        if (!tempFile.exists() || tempFile.length() == 0) {
            task.setRemark("临时文件创建失败");
            return false;
        }

        // 上传结果文件到OSS
        SysOssVo upload = sysOssService.upload(tempFile, originalFileName);

        // 验证上传是否成功
        if (upload == null) {
            task.setRemark("文件上传失败");
            tempFile.delete();
            return false;
        }

        // 删除临时文件
        tempFile.delete();

        // 统计处理结果
        long totalRows = resultList.size();
        long successRows = resultList.stream().filter(item -> "成功".equals(item.getProcessResult())).count();
        long failRows = totalRows - successRows;

        // 设置任务结果信息
        task.setTotalRows(totalRows);
        task.setSuccessRows(successRows);
        task.setFailRows(failRows);
        task.setResultFileName("导入结果_" + task.getFileName());
        task.setResultFileUrl(upload.getUrl());
        task.setRemark("合同关联用户导入完成，成功: " + successRows + " 条，失败: " + failRows + " 条");

        return true; // 只要有成功记录就认为导入成功
    }
}
