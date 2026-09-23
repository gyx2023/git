package com.crm.job.service.warning.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.domain.dao.FtpTcUserCustWarningDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.IFtpTcUserCustWarningService;
import com.crm.system.service.IMaintenanceDetailSyncService;
import com.crm.system.service.warning.WarningRecord;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 终端合约到期预警查询策略
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob 中 warning_type='000033' 分支（客户级派单）。
 * 数据取客户级预警表 ftp_tc_user_cust_warning，按 oa_id_dev 解析真实用户、创建
 * single_approval 流程、写 maintenance_detail + maintenance_detail_new（order_category='C'）。
 * </p>
 * <p>
 * ===== 完整链路 =====
 * WarningJob → WarningStartService.startByType("terminal_contract_warn")
 *   → TerminalContractWarningStrategy.query(targetDate)
 *     → listByWarningTypeAndCreateDate('000033') 查 ftp_tc_user_cust_warning + 按 oa_id_dev 查 sys_user
 *     → 转 List&lt;WarningRecord&gt;（已解析为真实 userId）
 *   → 逐条 createAndStartOrder(record)
 *     → Flowable 启动 single_approval 流程
 *     → 创建 topp_order 工单
 * </p>
 *
 * @author crm
 */
@Component
public class TerminalContractWarningStrategy extends AbstractWarningQueryStrategy {

    /** 终端合约到期的预警类型编码（ftp_tc_user_cust_warning.warning_type） */
    private static final String WARNING_TYPE = "000033";

    @Resource
    private IFtpTcUserCustWarningService ftpTcUserCustWarningService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private TcommCustomerMapper tcommCustomerMapper;

    @Resource
    private FtpCustBaseInfoMapper ftpCustBaseInfoMapper;

    @Resource
    private IMaintenanceDetailSyncService maintenanceDetailSyncService;

    @Override
    public String getOrderType() {
        return "terminal_contract_warn";
    }

    @Override
    protected String getOrderTypeName() {
        return "终端合约到期预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<FtpTcUserCustWarningDo> rows =
                ftpTcUserCustWarningService.listByWarningTypeAndCreateDate(WARNING_TYPE, targetDate);
        List<WarningRecord> records = new ArrayList<>();

        for (FtpTcUserCustWarningDo row : rows) {
            WarningRecord record = toWarningRecord(row);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    private WarningRecord toWarningRecord(FtpTcUserCustWarningDo row) {
        String oaIdDev = getString(row.getOaIdDev());
        if (oaIdDev == null || oaIdDev.isEmpty()) {
            log.warn("【{}】oa_id_dev 为空, 写 status=0 维系明细并跳过建单", getOrderTypeName());
            writeNoManagerSafe(row);
            return null;
        }

        // 按 oaId 查用户，获取真实 userId 及相关信息
        Map<String, Object> userRow = sysUsersMapper.selectUserByOaId(oaIdDev);
        if (userRow == null || userRow.isEmpty()) {
            log.warn("【{}】未找到对应客户经理, oaId: {}, 写 status=0 维系明细并跳过建单", getOrderTypeName(), oaIdDev);
            writeNoManagerSafe(row);
            return null;
        }

        Long userId = toLong(userRow.get("user_id"));
        if (userId == null) {
            log.warn("【{}】客户经理ID为空, oaId: {}, 写 status=0 维系明细并跳过建单", getOrderTypeName(), oaIdDev);
            writeNoManagerSafe(row);
            return null;
        }

        WarningRecord record = new WarningRecord();
        record.setOrderType(getOrderType());
        record.setOrderName(DateUtils.dateTime() + "_" + (StringUtils.isNotBlank(getString(row.getGrpName())) ? getString(row.getGrpName()) + "_" : "") + getOrderTypeName());
        record.setCustName(getString(row.getGrpName()));
        record.setCustManagerId(userId);

        // 根据 grp_code 查客户，设置 custId（对齐旧系统 customerService.getByCustNo）
        String grpCode = getString(row.getGrpCode());
        if (StringUtils.isNotBlank(grpCode)) {
            TcommCustomer customer = tcommCustomerMapper.selectOne(
                    new LambdaQueryWrapper<TcommCustomer>().eq(TcommCustomer::getCustNo, grpCode).last("LIMIT 1"));
            if (customer != null) {
                record.setCustId(customer.getId());
            }
        }

        // 设置创建人信息
        SysUsers sysUser = new SysUsers();
        sysUser.setId(userId);
        sysUser.setOrgId(toLong(userRow.get("dept_id")));
        sysUser.setNameCh(getString(userRow.get("nick_name")));
        record.setSysUser(sysUser);

        // 扩展字段
        record.setExtraVariable("infoId", toLong(row.getId()));
        record.setExtraVariable("orgId", toLong(userRow.get("dept_id")));
        record.setExtraVariable("nickName", getString(userRow.get("nick_name")));
        record.setExtraVariable("phone", getString(userRow.get("phonenumber")));
        record.setExtraVariable("_needOrderNumber", true);
        record.setExtraVariable("orderCategory", "C");
        // 地市/区县信息
        record.setExtraVariable("areaName", getString(row.getAreaName()));
        record.setExtraVariable("countyName", getString(row.getCountyName()));
        // 维系明细写入所需字段（afterStart → writeSuccess）
        record.setExtraVariable("grpCode", grpCode);
        record.setExtraVariable("custGrpId", getString(row.getCustGrpId()));
        record.setExtraVariable("oaIdDev", oaIdDev);
        record.setExtraVariable("oaNameDev", getString(row.getOaNameDev()));

        // businessDep：对齐旧系统 setAreaAndOrgNew，用 custGrpId 查 ftp_cust_base_info.grp_busi_dept_type
        String custGrpId = getString(row.getCustGrpId());
        if (StringUtils.isNotBlank(custGrpId) && ftpCustBaseInfoMapper != null) {
            try {
                FtpCustBaseInfoDo custInfo = ftpCustBaseInfoMapper.selectOne(
                        new LambdaQueryWrapper<FtpCustBaseInfoDo>()
                                .eq(FtpCustBaseInfoDo::getCustGrpId, Integer.parseInt(custGrpId))
                                .last("LIMIT 1"));
                if (custInfo != null) {
                    String deptType = custInfo.getGrpBusiDeptType();
                    record.setExtraVariable("businessDep",
                            (StringUtils.isBlank(deptType) || "99".equals(deptType)) ? "22" : deptType);
                }
            } catch (Exception e) {
                log.debug("【{}】查 ftp_cust_base_info businessDep 失败: custGrpId={}", getOrderTypeName(), custGrpId);
            }
        }

        return record;
    }

    /**
     * 建单成功后写维系明细：maintenance_detail(status=1) + maintenance_detail_new。
     * 写入失败仅记录日志，不影响预警主流程。
     */
    @Override
    public void afterStart(WarningRecord record, ToppOrderBo orderBo) {
        try {
            maintenanceDetailSyncService.writeSuccess(orderBo, record.getExtraVariables());
        } catch (Exception e) {
            log.error("【{}】写维系明细(status=1)失败, orderId={}", getOrderTypeName(),
                    orderBo != null ? orderBo.getId() : null, e);
        }
    }

    /** 无有效客户经理时写 status=0 维系明细，失败不影响查询主流程 */
    private void writeNoManagerSafe(FtpTcUserCustWarningDo row) {
        try {
            maintenanceDetailSyncService.writeNoManager(getOrderType(), row);
        } catch (Exception e) {
            log.error("【{}】写维系明细(status=0)失败", getOrderTypeName(), e);
        }
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealByTerminalContract?orderId=" + orderId;
    }

    @Override
    public boolean isNotificationEnabled() {
        return true;
    }

    @Override
    public String getNotificationMessage(int count) {
        return "温馨提醒：您收到" + count + "个" + getOrderTypeName() + "工单，已生成待处理任务，请至创新头条-省分专区-运营平台。";
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try {
            if (v instanceof Number) return ((Number) v).longValue();
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
