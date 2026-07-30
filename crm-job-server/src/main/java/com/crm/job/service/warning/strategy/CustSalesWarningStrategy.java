package com.crm.job.service.warning.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销预警查询策略
 * <p>
 * 从 ftp_broadband_market_cust 表查询指定账期且未生成订单的客户记录，
 * 转换为 WarningRecord 后由 WarningStartService 统一启动 single_approval 流程并创建工单。
 * <p>
 * 预警类型标识：cust_sales
 *
 * @author crm
 */
@Component
public class CustSalesWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;

    @Override
    public String getOrderType() {
        return "custSales";
    }

    @Override
    protected String getOrderTypeName() {
        return "宽带专项营销";
    }

    @Override
    protected List<WarningRecord> doQuery(String yyyyMM) {
        LambdaQueryWrapper<FtpBroadbandMarketCust> qw = new LambdaQueryWrapper<>();
        qw.eq(FtpBroadbandMarketCust::getStatCycle, yyyyMM)
            .isNull(FtpBroadbandMarketCust::getOrderId);
        List<FtpBroadbandMarketCust> custList = ftpBroadbandMarketCustMapper.selectList(qw);

        List<WarningRecord> records = new ArrayList<>(custList.size());
        for (FtpBroadbandMarketCust cust : custList) {
            WarningRecord record = new WarningRecord();
            record.setOrderType(getOrderType());
            record.setOrderName(cust.getGrpName() + "宽带专项营销");
            record.setCustName(cust.getGrpName());

            Map<String, Object> extra = new HashMap<>();
            extra.put("servManagerId", cust.getServManagerId());
            extra.put("custId", cust.getCustGrpId());
            extra.put("marketingName", cust.getMarketingName());
            extra.put("_id", cust.getId());
            record.setExtraVariables(extra);

            records.add(record);
        }
        return records;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/custSales/toCustManagerDeal?orderId=" + orderId;
    }
}
