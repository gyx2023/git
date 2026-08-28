package com.crm.system.service.warning.assign;

import com.crm.system.domain.dao.SysUsers;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 手动指派（批量转派未派发工单）建单上下文。
 * <p>
 * 与 crm-job-server 的 WarningRecord 数据契约对齐；
 * 策略负责从 maintenance_detail.detail_id 反查业务表并组装本对象，
 * 由 {@link WarningAssignService} 统一启动 Flowable 流程和创建 topp_order。
 * </p>
 *
 * @author crm
 */
@Data
public class WarningAssignContext {

    /** 预警类型，与 topp_order.order_type 对应，如 wotv_expire_warning */
    private String orderType;

    /** 工单名称 */
    private String orderName;

    /** 关联商机ID（可为 null） */
    private Long oppoId;

    /** 客户ID */
    private Long custId;

    /** 客户经理ID（被指派的处理人） */
    private Long custManagerId;

    /** 客户名称 */
    private String custName;

    /** 预警等级 */
    private String warningLevel;

    /** 指派处理人的用户信息（orgId、nameCh） */
    private SysUsers sysUser;

    /** 地市/区县 */
    private Long areaId;

    /** 扩展字段：grpCode/custGrpId/areaName/countyName/businessDep 等 */
    private Map<String, Object> extraVariables;

    public void setExtraVariable(String key, Object value) {
        if (extraVariables == null) {
            extraVariables = new HashMap<>();
        }
        extraVariables.put(key, value);
    }
}
