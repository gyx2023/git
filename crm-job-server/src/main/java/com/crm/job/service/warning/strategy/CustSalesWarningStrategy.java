package com.crm.job.service.warning.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.SysUsersMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销预警查询策略
 * <p>
 * 从 ftp_broadband_market_cust 表查询指定账期且未生成订单的客户记录，
 * 按发展人编码（serv_manager_id）解析客户经理，转换为 WarningRecord 后由
 * WarningStartService 统一启动 single_approval 流程并创建工单。
 * <p>
 * 预警类型标识：cust_sales
 *
 * @author crm
 */
@Component
public class CustSalesWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Override
    public String getOrderType() {
        return "cust_sales";
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
            // 按发展人编码查询客户经理，为空则回写 order_id=0 标记跳过
            if (StringUtils.isBlank(cust.getServManagerId())) {
                log.warn("【{}】发展人编码为空，回写 order_id=0 跳过, custId={}",
                        getOrderTypeName(), cust.getId());
                ftpBroadbandMarketCustMapper.updateCustOrderId(0, Long.valueOf(cust.getId()));
                continue;
            }
            SysUsers user = sysUsersMapper.selectUserByChannelHqCode(cust.getServManagerId());
            if (user == null || user.getId() == null) {
                log.warn("【{}】根据发展人编码 {} 查询客户经理为空，回写 order_id=0 跳过, custId={}",
                        getOrderTypeName(), cust.getServManagerId(), cust.getId());
                ftpBroadbandMarketCustMapper.updateCustOrderId(0, Long.valueOf(cust.getId()));
                continue;
            }

            WarningRecord record = new WarningRecord();
            record.setOrderType(getOrderType());
            record.setOrderName(cust.getGrpName() + "宽带专项营销");
            record.setCustName(cust.getGrpName());
            record.setCustManagerId(user.getId());
            // 创建人 + 组织信息（WarningStartService 据此设 creator/orgId）
            record.setSysUser(user);

            // 扩展字段：infoId 用于 topp_order.infoId + 建单去重
            record.setExtraVariable("infoId", Long.valueOf(cust.getId()));
            if (user.getOrgId() != null) {
                record.setExtraVariable("orgId", user.getOrgId());
            }
            record.setExtraVariable("nickName", user.getNameCh());
            record.setExtraVariable("phone", user.getRelationTel());
            record.setExtraVariable("_needOrderNumber", true);
            // 行业部（为空时 WarningStartService 兜底 "22"）
            if (StringUtils.isNotBlank(cust.getGrpBusiDeptType())) {
                record.setExtraVariable("businessDep", cust.getGrpBusiDeptType());
            }
            record.setExtraVariable("servManagerId", cust.getServManagerId());
            record.setExtraVariable("marketingName", cust.getMarketingName());

            records.add(record);
        }
        return records;
    }

    /**
     * 建单成功后回写 order_id 至 ftp_broadband_market_cust，避免同账期重复建单。
     */
    @Override
    public void afterStart(WarningRecord record, ToppOrderBo orderBo) {
        try {
            Object infoId = record.getExtraVariables().get("infoId");
            if (orderBo != null && orderBo.getId() != null && infoId != null) {
                ftpBroadbandMarketCustMapper.updateCustOrderId(orderBo.getId().intValue(), (Long) infoId);
                log.info("【{}】订单已创建，回写 order_id={} 至客户记录 id={}",
                        getOrderTypeName(), orderBo.getId(), infoId);
            }
        } catch (Exception e) {
            log.error("【{}】回写 order_id 失败, infoId={}",
                    getOrderTypeName(), record.getExtraVariables().get("infoId"), e);
        }
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/custSales/toCustManagerDeal?orderId=" + orderId;
    }
}
