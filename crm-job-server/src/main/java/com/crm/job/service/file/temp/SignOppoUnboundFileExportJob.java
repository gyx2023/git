package com.crm.job.service.file.temp;

import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.topp.SignOppoFileExportVO;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Slf4j
@Component
public class SignOppoUnboundFileExportJob {
    @Resource
    private IToppOpportunityService toppOpportunityService;
    @Resource
    private ISysOssService iSysOssService;

    @XxlJob("signOppoUnboundFileExportJob")
    public void execute() {
        log.info("开始执行商机文件导出");
        XxlJobHelper.log("开始执行商机文件导出");
        String param = XxlJobHelper.getJobParam();
        if(StringUtils.isBlank(param)){
            //默认取7天内数据
            param = "1";
        }
        // 计算查询时间范围
        LocalDate targetDate = LocalDate.now().minusDays(Integer.parseInt(param));
        // 构造指定日期的时间范围：00:00:00 到 23:59:59
        LocalDateTime startTime = LocalDateTime.of(targetDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(targetDate, LocalTime.MAX);

        log.info("查询时间范围：{} 到 {}", startTime, endTime);

        try {
            if(ObjectUtil.equals(param, "1")){
                List<ToppOpportunityVo> opportunityList = toppOpportunityService.queryOpportunityUnbound(startTime, endTime);
            }else{

            }


            // 2. 查询数据并转换
            List<SignOppoFileExportDTO> list = toppOpportunityService.selectToppOpportunity();
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
