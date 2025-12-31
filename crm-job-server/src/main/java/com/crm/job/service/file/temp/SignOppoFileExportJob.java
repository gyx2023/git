package com.crm.job.service.file.temp;

import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.topp.SignOppoFileExportVO;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


@Slf4j
@Component
public class SignOppoFileExportJob {
    @Resource
    private IToppOpportunityService service;
    @Resource
    private ISysOssService iSysOssService;

    @XxlJob("signOppoFileExportJob")
    public void execute() {
        log.info("开始执行商机文件导出");
        XxlJobHelper.log("开始执行商机文件导出");

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
            File tempFile = File.createTempFile("SJ2025_" + System.currentTimeMillis(), ".xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(voList, "商机信息", SignOppoFileExportVO.class, fos);
            }

            SysOssVo upload = iSysOssService.upload(tempFile);
            tempFile.deleteOnExit();
            log.info("商机信息导出成功，文件地址：{}", upload.getUrl());
        } catch (Exception e) {
            // 5. 异常处理：记录日志+标记XXL-Job任务失败
            String errorMsg = "商机信息导出失败：" + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log(errorMsg);
            XxlJobHelper.handleFail(errorMsg); // 关键：标记任务执行失败
        }
    }
}
