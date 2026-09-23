package com.crm.system.service.impl;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.DoubleWideExpireWarningDetailMapper;
import com.crm.system.service.IDoubleWideExpireWarningDetailService;
import com.crm.system.service.IFtpTcUserCustWarningService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 双线到期预警明细 Service 实现
 * <p>
 * 通过 infoId → main_order_flow_no → 查询 wide_sys_auto_warning 获取明细。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoubleWideExpireWarningDetailServiceImpl implements IDoubleWideExpireWarningDetailService {

    private final DoubleWideExpireWarningDetailMapper detailMapper;
    private final IFtpTcUserCustWarningService ftpTcUserCustWarningService;
    private final IToppOrderService orderService;

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        log.info("【double_wide】查询明细: orderId={}, mainOrderFlowNo={}, 记录数={}",
                orderId, mainOrderFlowNo, rows.size());
        return rows;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return TableDataInfo.build();
        }
        Page<Map<String, Object>> page = pageQuery.build();
        IPage<Map<String, Object>> pageResult = detailMapper.selectByMainOrderFlowNoPage(page, mainOrderFlowNo);
        log.info("【double_wide】分页查询明细: orderId={}, mainOrderFlowNo={}, 总数={}",
                orderId, mainOrderFlowNo, pageResult.getTotal());

        TableDataInfo<Map<String, Object>> tableDataInfo =
                new TableDataInfo<>(pageResult.getRecords(), pageResult.getTotal());
        tableDataInfo.setCode(HttpStatus.HTTP_OK);
        tableDataInfo.setMsg("查询成功");
        return tableDataInfo;
    }

    /**
     * orderId → topp_order.info_id → ftp_tc_user_cust_warning.main_order_flow_no
     *
     * @return 主订单流水号；解析失败返回 null
     */
    private String resolveMainOrderFlowNo(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【double_wide】工单不存在或无关联记录, orderId={}", orderId);
            return null;
        }
        String mainOrderFlowNo = ftpTcUserCustWarningService.getMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【double_wide】未找到主订单流水号, infoId={}", order.getInfoId());
        }
        return mainOrderFlowNo;
    }
}
