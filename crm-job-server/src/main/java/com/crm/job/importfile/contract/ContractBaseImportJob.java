package com.crm.job.importfile.contract;

import com.crm.common.enums.DateTypeEnum;
import com.crm.domain.TaskParams;
import com.crm.job.base.AbstractImportJob;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ContractBaseImportJob extends AbstractImportJob {

    @Override
    @XxlJob("contractBaseImport") // 独立的任务名称
    public void execute() {
        // 调用父类的通用逻辑
        super.execute();
    }

    @Override
    protected TaskParams parseTaskParams() {
        TaskParams params = new TaskParams();
        params.setSftpConfigType("contract");
        params.setDateType(DateTypeEnum.DAY.getCode());
        params.setBatchSize(1000);
        return params;
    }

    @Override
    protected void clearTargetTable() {

    }

    @Override
    protected void processFileInBatches(String filePath, int batchSize) {

    }

    @Override
    protected void syncToTargetTable() {

    }

}
