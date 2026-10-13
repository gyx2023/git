package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;
import java.util.Map;

/**
 * 用户红名单到期预警(000030) 明细 Service 接口
 *
 * @author crm
 */
public interface IHmdExpireWarningDetailService {

    /**
     * 查询工单关联的红名单预警明细记录列表
     *
     * @param orderId 工单ID
     * @return 明细记录列表
     */
    List<Map<String, Object>> queryRecords(Long orderId);

    /**
     * 分页查询工单关联的红名单预警明细记录
     *
     * @param orderId   工单ID
     * @param pageQuery 分页参数
     * @return 分页明细数据
     */
    TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery);

    /**
     * 查询明细第一页（处理页首屏使用；总数通过返回的 {@link TableDataInfo#getTotal()} 获取）
     *
     * @param orderId  工单ID
     * @param pageSize 页大小
     * @return 第一页明细数据 + 总条数
     */
    TableDataInfo<Map<String, Object>> queryFirstPageRecords(Long orderId, int pageSize);

    /**
     * 查询客户级预警表头信息（客户名称/编码/用户规模/逾期欠费合计/最早欠费账期）
     *
     * @param orderId 工单ID（经 topp_order.info_id 关联到客户级预警表）
     * @return 表头键值：grpName/grpCode/userScale/overdueFeeTotal/earliestArrearsCycle
     */
    Map<String, Object> queryHeader(Long orderId);
}
