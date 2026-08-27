package com.crm.job.service.warning;

import com.crm.common.core.domain.entity.SysUser;
import com.crm.system.domain.dao.SysUsers;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 预警记录 DTO
 * <p>
 * 各策略查询业务表后统一转为此对象，由 WarningStartService 统一处理流程启动。
 * 对应旧系统 BaseJob 中 FtpEntity 接口的数据契约角色（getGrpCode / getCustName / getServManagerId 等）。
 *
 * @author crm
 */
@Data
public class WarningRecord {

    /** 预警类型标识，与 topp_order.order_type 对应，如 "htdq_warn" */
    private String orderType;

    /** 工单名称，如 "合同到期预警" */
    private String orderName;

    /** 关联商机ID（可为 null） */
    private Long oppoId;

    /** 客户ID */
    private Long custId;

    /** 客户经理ID */
    private Long custManagerId;

    /** 客户名称 */
    private String custName;

    /** 预警等级 */
    private String warningLevel;

    private SysUsers sysUser;
    /** 地市区县 */
    private Long areaId;

    /** 扩展字段（如需传递额外的业务数据给流程变量或消息通知） */
    private Map<String, Object> extraVariables;

    /** 便捷设置单个扩展字段 */
    public void setExtraVariable(String key, Object value) {
        if (extraVariables == null) {
            extraVariables = new HashMap<>();
        }
        extraVariables.put(key, value);
    }
}
