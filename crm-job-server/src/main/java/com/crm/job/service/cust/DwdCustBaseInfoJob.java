package com.crm.job.service.cust;

import com.crm.job.base.AbstractFtpImportJob;
import com.crm.job.utils.GroupFileParser;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.service.cust.IFtpCustBaseInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 集团客户基础信息同步定时任务
 * 从SFTP读取客户基础信息文件并导入到ftp_cust_base_info表
 *
 * @author crm
 */
@Slf4j
@Component
public class DwdCustBaseInfoJob extends AbstractFtpImportJob<FtpCustBaseInfoDo> {

    @Resource private GroupFileParser groupFileParser;
    @Resource private IFtpCustBaseInfoService ftpCustBaseInfoService;

    @Override protected String getJobName() { return "dwdCustBaseInfoJobHandler"; }
    @Override protected String getConfigType() { return "jf_common_ftp"; }
    @Override protected String getFileDateType() { return "D"; }
    @Override protected String getSeparator() { return "\\x1b"; }

    @Override
    protected String getFileName(String dateStr) {
        return "DWD_CUST_BASE_INFO_D_" + dateStr + ".txt";
    }

    @Override
    protected FtpCustBaseInfoDo parseRow(String[] cols) { return null; }

    @Override
    protected FtpCustBaseInfoDo parseRowFromLine(String line) {
        return groupFileParser.parseLine(line, FtpCustBaseInfoDo.class, getSeparator());
    }

    @Override
    protected int doInsert(List<FtpCustBaseInfoDo> batch) {
        batch.forEach(e -> { if (e.getSrcTable() == null) {
            e.setSrcTable("NOLD");
        }
        });
        return ftpCustBaseInfoService.batchInsert(batch);
    }

    @Override
    protected void beforeImport() {
        ftpCustBaseInfoService.backUpFtpCustBaseInfo();
    }

    @XxlJob("dwdCustBaseInfoJobHandler")
    public void start() { execute(); }
}
