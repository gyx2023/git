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

    /**
     * 按目标用户主键集合查询"营销成功产品"可选列表（对齐老框架 getProductList(userIds)）。
     * <p>
     * 优先取所选用户的推荐产品 MKT_PRODUCT_ID_1/2，结果为空时回退 MKT_DISCNT_CODE_1/2，
     * "其它"（code=1）恒置于最后。
     * </p>
     *
     * @param userIds ftp_broadband_market_user.ID_ 集合（弹窗本次选中的用户）
     */
    java.util.List<java.util.Map<String, Object>> getProductListByUserIds(java.util.List<Integer> userIds);

    /**
     * 按工单查询"业务受理结果"明细（受理结果文件回传，对齐老框架 findCustSaleForResultPage）。
     *
     * @param orderId 工单ID
     */
    java.util.List<java.util.Map<String, Object>> getAcceptResultsByOrderId(Long orderId);
}
