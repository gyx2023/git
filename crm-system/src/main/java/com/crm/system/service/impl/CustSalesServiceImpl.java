package com.crm.system.service.impl;

import com.crm.common.exception.ServiceException;
import com.crm.system.domain.bo.CustSalesResultBo;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.vo.CustSalesVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.FtpBroadbandMarketUserMapper;
import com.crm.system.service.ICustSalesService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销处理 Service 实现
 * <p>
 * 数据链路：topp_order.info_id = ftp_broadband_market_cust.ID_，
 * 再以 账期+集团客户+客户经理+营销类型 四要素查询 ftp_broadband_market_user。
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustSalesServiceImpl implements ICustSalesService {

    /** 营销产品字典类型 */
    private static final String PRODUCT_DICT_TYPE = "cust_sale_product";

    /** 营销产品字典中"其它"的编码 */
    private static final String PRODUCT_CODE_OTHER = "1";

    private final FtpBroadbandMarketCustMapper custMapper;
    private final FtpBroadbandMarketUserMapper userMapper;
    private final IToppOrderService orderService;
    private final ISysCodeService sysCodeService;

    @Override
    public CustSalesVo getCustInfoByOrderId(Long orderId) {
        FtpBroadbandMarketCust cust = resolveCust(orderId);
        if (cust == null) {
            return null;
        }
        CustSalesVo vo = new CustSalesVo();
        vo.setGrpName(cust.getGrpName());
        vo.setMarketingName(cust.getMarketingName());
        vo.setMarketingType(cust.getMarketingType());
        vo.setAreaName(cust.getAreaName());
        vo.setCountyName(cust.getCountyName());
        vo.setUserScale(cust.getUserScale());
        vo.setStatCycle(cust.getStatCycle());
        vo.setCustGrpId(cust.getCustGrpId());
        vo.setServManagerId(cust.getServManagerId());
        return vo;
    }

    /**
     * 由工单ID解析营销客户记录
     */
    private FtpBroadbandMarketCust resolveCust(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【custSales】工单不存在或无关联客户记录, orderId={}", orderId);
            return null;
        }
        FtpBroadbandMarketCust cust = custMapper.selectCustById(order.getInfoId().intValue());
        if (cust == null) {
            log.warn("【custSales】未找到营销客户记录, orderId={}, infoId={}", orderId, order.getInfoId());
        }
        return cust;
    }

    @Override
    public Map<String, Object> pageUsers(Long orderId, int pageNum, int pageSize, Integer mktResult) {
        Map<String, Object> result = new HashMap<>();
        FtpBroadbandMarketCust cust = resolveCust(orderId);
        if (cust == null) {
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("unfilledCount", 0);
            return result;
        }
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;

        List<Map<String, Object>> records = userMapper.selectUserPage(
                cust.getStatCycle(), cust.getCustGrpId(), cust.getServManagerId(),
                cust.getMarketingType(), mktResult, offset, safePageSize);
        int total = userMapper.countUsers(cust.getStatCycle(), cust.getCustGrpId(),
                cust.getServManagerId(), cust.getMarketingType(), mktResult);
        // 未填写营销结果数随列表一并返回，供页面提示实时刷新（不受 mktResult 过滤影响）
        int unfilledCount = userMapper.countNoResultUsers(cust.getStatCycle(), cust.getCustGrpId(),
                cust.getServManagerId(), cust.getMarketingType());

        log.info("【custSales】查询用户明细: orderId={}, 账期={}, 记录数={}, 总数={}, 未填写={}",
                orderId, cust.getStatCycle(), records.size(), total, unfilledCount);
        result.put("records", records);
        result.put("total", total);
        result.put("unfilledCount", unfilledCount);
        return result;
    }

    @Override
    public int countUsers(Long orderId, Integer mktResult) {
        FtpBroadbandMarketCust cust = resolveCust(orderId);
        if (cust == null) {
            return 0;
        }
        return userMapper.countUsers(cust.getStatCycle(), cust.getCustGrpId(),
                cust.getServManagerId(), cust.getMarketingType(), mktResult);
    }

    @Override
    public int countNoResultUsers(Long orderId) {
        FtpBroadbandMarketCust cust = resolveCust(orderId);
        if (cust == null) {
            return 0;
        }
        return userMapper.countNoResultUsers(cust.getStatCycle(), cust.getCustGrpId(),
                cust.getServManagerId(), cust.getMarketingType());
    }

    @Override
    public int countByResult(Long orderId, String result) {
        FtpBroadbandMarketCust cust = resolveCust(orderId);
        if (cust == null) {
            return 0;
        }
        return userMapper.countByResult(cust.getStatCycle(), cust.getCustGrpId(),
                cust.getServManagerId(), cust.getMarketingType(), result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveResultForUsers(CustSalesResultBo bo) {
        if (bo == null || bo.getUserIds() == null || bo.getUserIds().isEmpty()) {
            throw new ServiceException("请选择需要填写营销结果的用户");
        }
        if (bo.getResult() == null) {
            throw new ServiceException("请选择营销结果");
        }

        String orderProduct = null;
        String otherProductName = null;
        // 仅营销成功时保留产品信息，失败时一并清空
        if (Integer.valueOf(1).equals(bo.getResult())) {
            orderProduct = trimToNull(bo.getOrderProduct());
            if (orderProduct == null) {
                throw new ServiceException("营销成功时请选择营销成功产品");
            }
            if (PRODUCT_CODE_OTHER.equals(orderProduct)) {
                otherProductName = trimToNull(bo.getOtherProductName());
                if (otherProductName == null) {
                    throw new ServiceException("产品选择“其它”时请填写产品名称");
                }
            }
        }

        int rows = userMapper.updateResultByIds(bo.getUserIds(), bo.getResult(),
                orderProduct, otherProductName);
        log.info("【custSales】保存营销结果: 用户数={}, 结果={}, 产品={}, 影响行数={}",
                bo.getUserIds().size(), bo.getResult(), orderProduct, rows);
        return rows;
    }

    @Override
    public LinkedHashMap<String, String> getProductOptions() {
        LinkedHashMap<String, String> map;
        try {
            map = sysCodeService.getCodeMapByType(PRODUCT_DICT_TYPE);
        } catch (Exception e) {
            log.warn("【custSales】查询营销产品字典失败", e);
            return new LinkedHashMap<>();
        }
        if (map == null) {
            return new LinkedHashMap<>();
        }
        // 与老框架 getProductList 一致："其它"始终展示在最后
        String otherName = map.remove(PRODUCT_CODE_OTHER);
        if (otherName != null) {
            map.put(PRODUCT_CODE_OTHER, otherName);
        }
        return map;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
