package com.crm.job.service.file.temp;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.system.domain.vo.topp.SignOppoFileExportVO;
import com.crm.system.service.topp.ToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;


@Slf4j
@Component
public class SignOppoFileExportJob {
    @Resource
    private ToppOpportunityService service;

    @XxlJob("signOppoFileExportJob")
    public void execute() {
        log.info("开始执行商机文件导出");
        XxlJobHelper.log("开始执行商机文件导出");

        // 1. 定义文件路径和名称（建议用时间戳避免覆盖）
        String filePath = "C:\\test";
        String fileName = "投标清单文件_" + System.currentTimeMillis() + ".xlsx";
        File targetFile = new File(filePath, fileName);

        try {
            // 2. 查询数据并转换
            List<SignOppoFileExportDTO> list = service.selectToppOpportunity();
            // 空数据判断：避免空指针，且不生成空文件
            if (list == null || list.isEmpty()) {
                String emptyMsg = "未查询到商机数据，无需导出Excel";
                log.info(emptyMsg);
                XxlJobHelper.log(emptyMsg);
                return;
            }
            List<SignOppoFileExportVO> voList = BeanCopyUtils.copyList(list, SignOppoFileExportVO.class);

            // 3. 自动创建目录（核心修复：解决目录不存在报错）
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists()) {
                boolean mkdirs = parentDir.mkdirs(); // 多级目录创建
                if (!mkdirs) {
                    throw new RuntimeException("创建导出目录失败：" + parentDir.getAbsolutePath());
                }
                log.info("成功创建导出目录：{}", parentDir.getAbsolutePath());
            }

            // 4. 生成Excel文件（使用 Alibaba EasyExcel）
            try (OutputStream os = new FileOutputStream(targetFile);
                 ExcelWriter writer = EasyExcel.write(os, SignOppoFileExportVO.class).build()) {

                // 写入数据
                writer.write(voList, EasyExcel.writerSheet("投标清单").build());

                log.info("Excel导出成功，文件路径：{}", targetFile.getAbsolutePath());
                XxlJobHelper.log("Excel导出成功，文件路径：{}", targetFile.getAbsolutePath());

            } catch (Exception e) {
                throw new RuntimeException("Excel写入失败", e);
            }

        } catch (Exception e) {
            // 5. 异常处理：记录日志+标记XXL-Job任务失败
            String errorMsg = "导出投标清单文件失败：" + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log(errorMsg);
            XxlJobHelper.handleFail(errorMsg); // 关键：标记任务执行失败
        }
    }
}
