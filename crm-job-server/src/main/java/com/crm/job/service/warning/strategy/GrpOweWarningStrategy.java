package com.crm.job.service.warning.strategy;

import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 欠费预警查询策略
 * <p>
 * 对应旧系统 TcGrpOweWarningJob
 * 查询表：ftp_tc_grp_owe_warning
 * 预警类型标识：qf_warn
 * <p>
 * ===== 完整链路 =====
 * OweWarningJob → IWarningStartService.startByType("qf_warn")
 *   → GrpOweWarningStrategy.query(targetDate)
 *     → GrpOweWarningMapper 查 ftp_tc_grp_owe_warning
 *     → 转 List<WarningRecord>
 *   → 逐条 createAndStartOrder(record)
 *     → Flowable 启动 single_approval 流程
 *     → 创建 topp_order 工单
 *     → 设置任务本地变量
 *
 * @author crm
 */
@Component
public class GrpOweWarningStrategy extends AbstractWarningQueryStrategy {

    /**
     * 欠费预警 Mapper
     */
    @org.apache.ibatis.annotations.Mapper
    interface GrpOweWarningMapper {
        /**
         * 查询指定日期未处理的欠费预警
         * 排除 WARNING_TYPE = 'C00009'
         */
        @Select({"SELECT f.id_, f.grp_code, f.grp_name, f.serv_manager_id,"
                + " f.warning_level, f.warning_type, f.create_date"
                + " FROM ftp_tc_grp_owe_warning f"
                + " LEFT JOIN maintenance_detail m ON f.id_ = m.detail_id AND m.order_type IN ('qf_warn')"
                + " WHERE m.status IS NULL"
                + " AND f.warning_type != 'C00009'"
                + " AND DATE_FORMAT(f.create_date, '%Y%m%d') = #{targetDate}"})
        List<OweWarningEntity> selectOweList(@Param("targetDate") String targetDate);
    }

    @lombok.Data
    static class OweWarningEntity {
        private Long id_;
        private String grpCode;
        private String grpName;
        private String servManagerId;
        private String warningLevel;
        private String warningType;
        private java.util.Date createDate;
    }

    @javax.annotation.Resource
    private GrpOweWarningMapper mapper;

    @Override
    public String getOrderType() {
        return "qf_warn";
    }

    @Override
    protected String getOrderTypeName() {
        return "欠费预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<OweWarningEntity> list = mapper.selectOweList(targetDate);
        return list.stream().map(this::toWarningRecord).collect(Collectors.toList());
    }

    private WarningRecord toWarningRecord(OweWarningEntity entity) {
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
