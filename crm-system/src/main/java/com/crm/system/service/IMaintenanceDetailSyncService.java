package com.crm.system.service;

import com.crm.system.domain.bo.topp.ToppOrderBo;

import java.util.Map;

/**
 * 维系预警明细写入 Service。
 * <p>
 * 复刻旧系统 TcUserCustWarningNewJob 中对 maintenance_detail / maintenance_detail_new 的写入，
 * 供新框架预警策略（如 KdAddrWarningStrategy）在建单成功 / 无客户经理时调用。
 *
 * @author crm
 */
public interface IMaintenanceDetailSyncService {

    /**
     * 建单成功后写入 maintenance_detail(status=1) + maintenance_detail_new。
     * 对应旧系统 TcUserCustWarningNewJob.java:367-389。
     *
     * @param orderBo 已建好的工单（提供 orderId、custName、areaId、infoId、orderType）
     * @param extra   WarningRecord.extraVariables，需含 grpCode/custGrpId/areaName/countyName/oaIdDev/oaNameDev
     */
    void writeSuccess(ToppOrderBo orderBo, Map<String, Object> extra);

    /**
     * 手动指派（未派发转派）成功后：把已有 maintenance_detail(status=0) 更新为 status=1，
     * 同时插入 maintenance_detail_new(status=1)。不同于 writeSuccess（全量新建），
     * 此方法针对手动指派场景复用原明细主键，避免产生重复明细。
     *
     * @param detailId  maintenance_detail.id_（status=0 的未派发明细）
     * @param orderBo   已建好的工单
     * @param extra     WarningAssignContext.extraVariables
     */
    void assignSuccess(Long detailId, ToppOrderBo orderBo, Map<String, Object> extra);

    /**
     * 无有效客户经理时写入 maintenance_detail(status=0)，不建单、不写 maintenance_detail_new。
     * 对应旧系统 TcUserCustWarningNewJob.java:325-339（manager 查询失败分支）。
     *
     * @param orderType 预警类型，如 kd_addr_warn
     * @param row       原始查询行（ftp_tc_user_cust_warning，键为 SQL 列名 id_/grp_code/cust_grp_id/grp_name/area_name/county_name/oa_id_dev/oa_name_dev）
     */
    void writeNoManager(String orderType, Map<String, Object> row);
}
