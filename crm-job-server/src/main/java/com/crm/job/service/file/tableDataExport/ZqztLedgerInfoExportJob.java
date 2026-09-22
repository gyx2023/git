package com.crm.job.service.file.tableDataExport;

import com.crm.common.utils.ExportToolsUtils;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.job.utils.export.TextFileExportContext;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * 台账数据主表导出定时任务。
 * 将 zqzt_ledger_info 全表、全字段导出为管道符分隔的 TXT 文件并上传 SFTP。
 */
@Slf4j
@Component
public class ZqztLedgerInfoExportJob extends AbstractTableDataExporter<ZqztLedgerInfo> {

    private static final long RANGE_BUCKET_PAGE_SIZE = 5000L;

    @Resource
    private ZqztLedgerInfoMapper zqztLedgerInfoMapper;

    @XxlJob("zqztLedgerInfoExportJobHandler")
    public void execute() {
        XxlJobHelper.log("开始执行台账数据主表导出任务");
        executeExport(null, null);
    }

    @Override
    protected long getTotalCount() {
        Long count = zqztLedgerInfoMapper.selectAllForExportCount();
        return count != null ? count : 0;
    }

    @Override
    protected Long getIdMin() {
        return zqztLedgerInfoMapper.selectAllForExportMinId();
    }

    @Override
    protected Long getIdMax() {
        return zqztLedgerInfoMapper.selectAllForExportMaxId();
    }

    @Override
    protected List<ZqztLedgerInfo> queryByRange(long startId, long endId) {
        return zqztLedgerInfoMapper.selectAllForExportByRange(startId, endId);
    }

    /**
     * 仅在本任务内兼容空表和单条记录，不改变公共导出基类及其他既有任务的行为。
     */
    @Override
    protected void writeBody(TextFileExportContext ctx, BufferedWriter writer) throws IOException {
        long totalCount = getTotalCount();
        if (totalCount <= 0) {
            log.warn("查询数据为空，表: {}", getFileNamePrefix());
            XxlJobHelper.log("查询数据为空");
            return;
        }

        Long minId = getIdMin();
        Long maxId = getIdMax();
        if (minId == null || maxId == null) {
            log.warn("ID范围为空，表: {}", getFileNamePrefix());
            XxlJobHelper.log("ID范围为空");
            return;
        }

        if (minId <= 0 || maxId < minId) {
            log.warn("ID范围无效，表: {}, minId={}, maxId={}", getFileNamePrefix(), minId, maxId);
            XxlJobHelper.log("ID范围无效: minId={}, maxId={}", minId, maxId);
            return;
        }

        long bucketCount = (totalCount + RANGE_BUCKET_PAGE_SIZE - 1) / RANGE_BUCKET_PAGE_SIZE;
        long rangeStep = (maxId - minId + 1) / bucketCount;
        if (rangeStep <= 0) {
            rangeStep = 1;
        }

        long totalRows = 0;
        for (long i = 0; i < bucketCount; i++) {
            long startId = minId + i * rangeStep;
            long endId = (i == bucketCount - 1) ? maxId + 1 : startId + rangeStep;
            List<ZqztLedgerInfo> records = queryByRange(startId, endId);
            if (records == null || records.isEmpty()) {
                continue;
            }

            for (ZqztLedgerInfo record : records) {
                Object[] columns = getColumns(record);
                for (int j = 0; j < columns.length; j++) {
                    if (j > 0) {
                        writer.write(SEPARATOR);
                    }
                    writer.write(ExportToolsUtils.formatField(columns[j]));
                }
                writer.newLine();
                totalRows++;
            }
            writer.flush();
        }

        ctx.setTotalRows(totalRows);
        log.info("数据写入完成，共 {} 行", totalRows);
        XxlJobHelper.log("数据写入完成，共 {} 行", totalRows);
    }

    @Override
    protected String getFileNamePrefix() {
        return "zqzt_ledger_info";
    }

    @Override
    protected String[] getHeaders() {
        return null;
    }

    @Override
    protected Object[] getColumns(ZqztLedgerInfo record) {
        return new Object[]{
                record.getId(), record.getOrderNo(), record.getSignDate(), record.getOrderCreateTime(),
                record.getCityId(), record.getDistrictId(), record.getStreetId(), record.getIndustryId(),
                record.getCompanyName(), record.getCreditCode(), record.getRegAddress(), record.getActualAddress(),
                record.getSignRoutes(), record.getSpecialScene(), record.getCompanyPrincipal(), record.getPrincipalPhone(),
                record.getSafetyOfficer1(), record.getSafetyOfficer1Phone(), record.getSafetyOfficer2(), record.getSafetyOfficer2Phone(),
                record.getConstructContact(), record.getConstructContactPhone(), record.getUnicomManager(), record.getUnicomManagerPhone(),
                record.getCameraIsNewInstall(), record.getCameraPurchaseType(), record.getCameraInstallDate(), record.getCameraAccount(),
                record.getCameraFinishDate(), record.getBroadbandIsReuse(), record.getBroadbandIsSporadic(),
                record.getBroadbandSporadicPlanDate(), record.getBroadbandSporadicActualDate(), record.getBroadbandConstructStatus(),
                record.getBroadbandSporadicOrderNo(), record.getBroadbandAccount(), record.getBroadbandFinishDate(),
                record.getCloudAccount(), record.getCloudPassword(), record.getCloudOrgId(), record.getCloudAccessKey(),
                record.getCloudVisionPackageId(), record.getInfoEntryStatus(), record.getFullProcessDate(), record.getStatus(),
                record.getRemark(), record.getCreateTime(), record.getUpdateTime(), record.getCreatedBy(), record.getUpdatedBy(),
                record.getIsValid()
        };
    }
}
