package com.crm.job.service.sync;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.job.utils.FilePath;
import com.crm.system.domain.vo.topp.OpportunityReportVO;
import com.crm.system.service.sys.ISysOssService;
import com.crm.system.service.ReportService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 千项万亿报表导出csv文件
 * 每周六9点05分
 * @author pengjun
 */
@Component
@Slf4j
public class SyncExportReportJob {

    public static final String ATTACH_FILE_PATH = "zj_yypt/";

    private static final int PAGE_SIZE = 1000;

    @Resource
    private ISysOssService ossService;

    @Resource
    private ReportService reportService;

    @XxlJob("syncExportReportFile")
    public void syncExportReportFile() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("jobParam：{}", jobParam);
        log.info(">>>>>>>>>>>>> 开始执行导出报表");
        startDeal(jobParam);
        log.info(">>>>>>>>>>>>> 结束执行导出报表");
    }

    private void startDeal(String jobParam) {
        String fileName = jobParam + getYearMonthDay() + ".csv";
        String filePath = FilePath.rootPath;
        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean mkdirSuccess = dir.mkdirs();
            log.info("创建目录结果：{}，路径：{}", mkdirSuccess, filePath);
        }
        File csvFile = new File(filePath, fileName);
        String[] title = {"地市", "商机编号", "商机名称", "客户行业", "商机类型", "商机金额万", "cnt", "id_", "total"};

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(Files.newOutputStream(csvFile.toPath()), StandardCharsets.UTF_8))) {

            writer.write(arrayToCsvLine(title));
            writer.newLine();

            int currentPage = 1;
            long total = 0;

            while (true) {
                Page<OpportunityReportVO> page = new Page<>(currentPage, PAGE_SIZE);
                IPage<OpportunityReportVO> resultPage = reportService.getReportData(page);

                // ==============================================
                // 🔥 修复死循环：页数超过总页数就退出
                // ==============================================
                if (currentPage > resultPage.getPages()) {
                    break;
                }

                List<OpportunityReportVO> records = resultPage.getRecords();
                if (records == null || records.isEmpty()) {
                    break;
                }

                for (OpportunityReportVO vo : records) {
                    Object[] row = buildRow(vo);
                    writer.write(arrayToCsvLine(row));
                    writer.newLine();
                }

                total += records.size();
                currentPage++;
                log.info("已写入：{} 条", total);
                writer.flush();
            }

            log.info("报表生成完成，总条数：{}", total);
            XxlJobHelper.log("报表生成完成，总条数：{}", total);

            // 上传COS
            uploadToCos(csvFile);

        } catch (Exception e) {
            log.error("报表生成异常", e);
            XxlJobHelper.log("报表生成异常：" + e.getMessage());
        } finally {
            if (csvFile.exists()) {
                csvFile.delete();
            }
        }
    }

    private Object[] buildRow(OpportunityReportVO vo) {
        return new Object[]{
            ObjectUtil.isNotEmpty(vo.getCity()) ? vo.getCity() : "",
            ObjectUtil.isNotEmpty(vo.getOppCode()) ? vo.getOppCode() : "",
            ObjectUtil.isNotEmpty(vo.getOppName()) ? vo.getOppName() : "",
            ObjectUtil.isNotEmpty(vo.getCustomerIndustry()) ? vo.getCustomerIndustry() : "",
            ObjectUtil.isNotEmpty(vo.getOppType()) ? vo.getOppType() : "",
            vo.getOppMoney() == null ? "" : vo.getOppMoney(),
            vo.getCnt() == null ? "" : vo.getCnt(),
            vo.getId() == null ? "" : vo.getId(),
            vo.getTotal() == null ? "" : vo.getTotal()
        };
    }

    /**
     * 上传COS（自定义路径 zj_yypt/2026/06/05/xxx.csv）
     */
    private void uploadToCos(File csvFile) {
        try {
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String uploadPath = ATTACH_FILE_PATH + datePath;

            // 🔥 直接上传 File，不需要转 MultipartFile
            // 🔥 路径：uploadPath = zj_yypt/2026/06/05
            ossService.uploadFileByPath(csvFile, uploadPath);

            log.info("文件上传COS成功，路径：{}", uploadPath + "/" + csvFile.getName());
        } catch (Exception e) {
            log.error("文件上传COS失败", e);
        }
    }

    private String arrayToCsvLine(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            String value = (array[i] == null) ? "" : array[i].toString();
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            sb.append(value);
        }
        return sb.toString();
    }

    public static String getYearMonthDay() {
        SimpleDateFormat dateAllFormat = new SimpleDateFormat(Constants.YEAR_MONTH_DAY_PATTERN);
        return dateAllFormat.format(new Date());
    }
}
