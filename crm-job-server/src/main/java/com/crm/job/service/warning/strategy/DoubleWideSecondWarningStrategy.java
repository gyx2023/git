package com.crm.job.service.warning.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.mapper.warning.DoubleWideSecondWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.system.service.warning.WarningRecord;
import com.crm.system.domain.dao.MaintenanceDetailDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.MaintenanceDetailMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 双线二次预警查询策略
 * <p>
 * 对应旧系统 SecondWarningJob.doubleWideSecondJob
 * 查询表：ftp_tc_user_cust_warning（warning_type in ('C00007','C00008')）
 * 预警类型标识：double_wide_second
 * <p>
 * ===== 完整链路 =====
 * DoubleWideSecondWarningJob → WarningStartService.startByType("double_wide_second")
 *   → DoubleWideSecondWarningStrategy.query(targetDate)
 *     → Mapper SQL 查 ftp_tc_user_cust_warning + 按 oaId_dev 查 sys_user
 *     → 转 List&lt;WarningRecord&gt;（已解析为真实 userId）
 *   → 逐条 createAndStartOrder(record)
 *     → Flowable 启动 single_approval 流程
 *     → 创建 topp_order 工单（order_type = double_wide_second）
 *
 * @author crm
 */
@Component
public class DoubleWideSecondWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private DoubleWideSecondWarningMapper mapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private TcommCustomerMapper tcommCustomerMapper;

    @Resource
    private MaintenanceDetailMapper maintenanceDetailMapper;

    @Override
    public String getOrderType() {
        return "double_wide_second";
    }

    @Override
    protected String getOrderTypeName() {
        return "双线二次预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<Map<String, Object>> rows = mapper.selectSecondWarnList(targetDate);
        List<WarningRecord> records = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            // 单条源数据异常不能中断整批：跳过并记录，否则整次跑批 0 建单
            try {
                WarningRecord record = toWarningRecord(row);
                if (record != null) {
                    records.add(record);
                }
            } catch (Exception e) {
                log.error("【{}】转换预警记录失败, 跳过该条: id_={}", getOrderTypeName(), row.get("id_"), e);
            }
        }
        return records;
    }

    private WarningRecord toWarningRecord(Map<String, Object> row) {
        String oaIdDev = getString(row.get("oa_id_dev"));
        if (oaIdDev == null || oaIdDev.isEmpty()) {
            log.warn("【{}】oa_id_dev 为空, 跳过", getOrderTypeName());
            return null;
        }

        // 按 oaId 查 sys_user 表，获取真实 userId 及相关信息
        Map<String, Object> userRow = sysUsersMapper.selectUserByOaId(oaIdDev);
        if (userRow == null || userRow.isEmpty()) {
            log.warn("【{}】未找到对应客户经理, oaId: {}", getOrderTypeName(), oaIdDev);
            return null;
        }

        Long userId = toLong(userRow.get("user_id"));
        if (userId == null) {
            log.warn("【{}】客户经理ID为空, oaId: {}", getOrderTypeName(), oaIdDev);
            return null;
        }

        WarningRecord record = new WarningRecord();
        record.setOrderType(getOrderType());
        // order_name: yyyyMMdd_custName_双线二次预警
        String custName = getString(row.get("grp_name"));
        String dateStr = DateUtils.dateTimeNow(DateUtils.YYYYMMDD);
        record.setOrderName(dateStr + "_" + custName + "_" + getOrderTypeName());
        record.setCustName(custName);
        record.setCustManagerId(userId);

        String grpCode = getString(row.get("grp_code"));
        if (StringUtils.isNotBlank(grpCode)) {
            // 对齐旧系统 getByCustNo：cust_no 存在重复时取第一条，避免 selectOne 抛 TooManyResultsException
            List<TcommCustomer> customers = tcommCustomerMapper.selectList(
                    new LambdaQueryWrapper<TcommCustomer>().eq(TcommCustomer::getCustNo, grpCode));
            if (customers != null && !customers.isEmpty()) {
                record.setCustId(customers.get(0).getId());
            }
        }

        SysUsers sysUser = new SysUsers();
        sysUser.setId(userId);
        record.setSysUser(sysUser);

        record.setExtraVariable("infoId", toLong(row.get("id_")));
        record.setExtraVariable("orgId", toLong(userRow.get("dept_id")));
        record.setExtraVariable("nickName", getString(userRow.get("nick_name")));
        record.setExtraVariable("phone", getString(userRow.get("phonenumber")));
        record.setExtraVariable("_needOrderNumber", true);
        record.setExtraVariable("orderCategory", "C");

        Long infoId = toLong(row.get("id_"));
        if (infoId != null) {
            // 对齐旧系统 maintenanceDetailService.findByDetailId(id, ORDER_TYPE_DOUBLE_WIDE)：重复取第一条
            List<MaintenanceDetailDo> details = maintenanceDetailMapper.selectList(
                    new LambdaQueryWrapper<MaintenanceDetailDo>()
                            .eq(MaintenanceDetailDo::getDetailId, infoId)
                            .eq(MaintenanceDetailDo::getOrderType, "double_wide"));
            if (details != null && !details.isEmpty() && StringUtils.isNotBlank(details.get(0).getBusinessDep())) {
                record.setExtraVariable("businessDep", details.get(0).getBusinessDep());
            }
        }

        return record;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealByDoubleWideSecond?orderId=" + orderId;
    }

    @Override
    public boolean isDuplicateCheckEnabled() {
        // 双线二次预警不做建单去重：同一预警记录需随跑批多次建单（对齐旧系统 SecondWarningJob.doubleWideSecondJob 不查重）
        return false;
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
