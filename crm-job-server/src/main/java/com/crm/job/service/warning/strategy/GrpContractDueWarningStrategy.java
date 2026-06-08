package com.crm.job.service.warning.strategy;

import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同到期预警查询策略
 * <p>
 * 对应旧系统 TcGrpContractDueWarningJob
 * 查询表：ftp_tc_grp_contract_due_warning
 * 预警类型标识：htdq_warn
 * <p>
 * ===== 完整链路 =====
 * ContractDueWarningJob → IWarningStartService.startByType("htdq_warn")
 *   → GrpContractDueWarningStrategy.query(targetDate)
 *     → GrpContractDueWarningMapper 查 ftp_tc_grp_contract_due_warning
 *     → 转 List<WarningRecord>
 *   → 逐条 createAndStartOrder(record)
 *     → Flowable 启动 single_approval 流程
 *     → 创建 topp_order 工单
 *     → 设置任务本地变量
 *
 * @author crm
 */
@Component
public class GrpContractDueWarningStrategy extends AbstractWarningQueryStrategy {

    /**
     * 合同到期预警 Mapper
     * 内联定义，与 Strategy 放在一起，便于查找和对应
     */
    @org.apache.ibatis.annotations.Mapper
    interface GrpContractDueWarningMapper {
        /**
         * 查询指定日期未处理的合同到期预警
         * JOIN maintenance_detail 做去重（已派单的记录不重复创建工单）
         */
        @Select({"SELECT f.id_, f.grp_code, f.grp_name, f.serv_manager_id, f.warning_level,"
                + " f.contract_end_date, f.create_date"
                + " FROM ftp_tc_grp_contract_due_warning f"
                + " LEFT JOIN maintenance_detail m ON f.id_ = m.detail_id AND m.order_type = 'htdq_warn'"
                + " WHERE m.status IS NULL"
                + " AND DATE_FORMAT(f.create_date, '%Y%m%d') = #{targetDate}"})
        List<ContractDueWarningEntity> selectDueList(@Param("targetDate") String targetDate);
    }

    /**
     * 合同到期预警实体
     * 仅包含启动流程所需的字段
     */
    @lombok.Data
    static class ContractDueWarningEntity {
        private Long id_;
        private String grpCode;
        private String grpName;
        private String servManagerId;
        private String warningLevel;
        // 扩展字段（流程变量用）
        private java.util.Date contractEndDate;
        private java.util.Date createDate;
    }

    @javax.annotation.Resource
    private GrpContractDueWarningMapper mapper;

    @Override
    public String getOrderType() {
        return "htdq_warn";
    }

    @Override
    protected String getOrderTypeName() {
        return "合同到期预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<ContractDueWarningEntity> list = mapper.selectDueList(targetDate);
        return list.stream().map(this::toWarningRecord).collect(Collectors.toList());
    }

    /**
     * 实体 → WarningRecord 映射
     * 各字段来源一目了然
     */
    private WarningRecord toWarningRecord(ContractDueWarningEntity entity) {
        WarningRecord record = new WarningRecord();
        record.setOrderType(getOrderType());
        record.setOrderName(getOrderTypeName());
        record.setCustId(parseLong(entity.getGrpCode()));
        record.setCustManagerId(parseLong(entity.getServManagerId()));
        record.setCustName(entity.getGrpName());
        record.setWarningLevel(entity.getWarningLevel());
        return record;
    }

    private Long parseLong(String value) {
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
