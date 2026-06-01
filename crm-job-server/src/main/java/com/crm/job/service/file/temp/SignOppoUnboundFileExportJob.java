package com.crm.job.service.file.temp;

import cn.hutool.core.util.ObjectUtil;
import com.crm.system.domain.dto.SignOppoFileExportDTO;
import com.crm.common.utils.BeanCopyUtils;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.topp.SignOppoFileExportVO;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.zbzq.ISalesupBakTblBidSuboppoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SignOppoUnboundFileExportJob {
    @Resource
    private IToppOpportunityService toppOpportunityService;
    @Resource
    private ISysOssService iSysOssService;
    @Resource
    private ISalesupBakTblBidSuboppoService salesupBakTblBidSuboppoService;

    @XxlJob("signOppoUnboundFileExportJob")
    public void execute() {
        log.info("开始执行废弃商机信息导出");
        XxlJobHelper.log("开始执行废弃商机信息导出");
        try {
            List<SignOppoFileExportDTO> list = toppOpportunityService.selectOpportunityUnbound();
            List<String> oppoCodeList = list.stream().map(SignOppoFileExportDTO::getOppCode).collect(Collectors.toList());
            List<String> signedOppoCodes = salesupBakTblBidSuboppoService.selectSignedOppoData(oppoCodeList);
            oppoCodeList.removeAll(signedOppoCodes);
            List<SignOppoFileExportDTO> zdList = list.stream()
                .filter(opportunity -> oppoCodeList.contains(opportunity.getOppCode()))
                .collect(Collectors.toList());


            // 空数据判断：避免空指针，且不生成空文件
            if (ObjectUtil.isEmpty(zdList)) {
                String emptyMsg = "未查询到商机数据，无需导出Excel";
                log.info(emptyMsg);
                XxlJobHelper.log(emptyMsg);
                return;
            }
            List<SignOppoFileExportVO> voList = BeanCopyUtils.copyList(zdList, SignOppoFileExportVO.class);
            File tempFile = File.createTempFile("SJ_SIGN_VALID_" + System.currentTimeMillis(), ".xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(voList, "废弃商机信息", SignOppoFileExportVO.class, fos);
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
