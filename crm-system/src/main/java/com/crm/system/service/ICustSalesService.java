package com.crm.system.service;

import com.crm.system.domain.bo.CustSalesResultBo;
import com.crm.system.domain.vo.CustSalesVo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 宽带专项营销（custSales）处理 Service
 *
 * @author crm
 */
public interface ICustSalesService {

    /**
     * 根据工单ID查询营销客户信息（topp_order.info_id → ftp_broadband_market_cust.ID_）
     *
     * @return 客户信息，工单或客户记录不存在时返回 null
     */
    CustSalesVo getCustInfoByOrderId(Long orderId);

    /**
     * 分页查询营销用户明细
     *
     * @param mktResult 营销结果过滤：null-全查 1-成功 0-失败
     * @return key: records（List）、total（int）、unfilledCount（int，未填写营销结果的用户数）
     */
    Map<String, Object> pageUsers(Long orderId, int pageNum, int pageSize, Integer mktResult);

    /**
     * 统计营销用户数
     *
     * @param mktResult 营销结果过滤，null 表示全部
     */
    int countUsers(Long orderId, Integer mktResult);

    /**
     * 统计未填写营销结果的用户数
     */
    int countNoResultUsers(Long orderId);

    /**
     * 按营销结果统计用户数
     *
     * @param result "1"-成功 "0"-失败
     */
    int countByResult(Long orderId, String result);

    /**
     * 批量保存用户营销结果
     *
     * @return 影响行数
     */
    int saveResultForUsers(CustSalesResultBo bo);

    /**
     * 查询营销产品字典（cust_sale_product，由 custSalesFileJob 重建，code_=1 为"其它"）
     *
     * @return key 为产品编码，value 为产品名称
     */
    LinkedHashMap<String, String> getProductOptions();
}
