package com.crm.adapter.admin.system;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.topp.IToppOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 客户详情公共 Controller
 * <p>
 * 客户详情数据主要来自 ftp_cust_base_info，辅以 tcomm_customer 补充。
 * 供 CustomerWarningDetail 公共前端组件调用。
 * </p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/customerDetail")
public class CustomerDetailController extends BaseController {

    private final IToppOrderService orderService;
    private final TcommCustomerMapper tcommCustomerMapper;
    private final FtpCustBaseInfoMapper ftpCustBaseInfoMapper;

    /**
     * 按工单ID查询关联客户详情
     */
    @GetMapping("/byOrder")
    public R<Map<String, Object>> byOrder(@RequestParam("orderId") Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getCustId() == null) {
            return R.fail("工单不存在或无关联客户");
        }
        TcommCustomer cust = tcommCustomerMapper.selectById(order.getCustId());
        if (cust == null || StringUtils.isBlank(cust.getCustNo())) {
            return R.fail("客户信息不存在");
        }
        return R.ok(buildCustInfo(cust.getCustNo(), cust));
    }

    /**
     * 按集团编码查询客户详情
     */
    @GetMapping("/byGrpCode")
    public R<Map<String, Object>> byGrpCode(@RequestParam("grpCode") String grpCode) {
        if (StringUtils.isBlank(grpCode)) {
            return R.fail("grpCode 不能为空");
        }
        TcommCustomer cust = tcommCustomerMapper.selectOne(
                new LambdaQueryWrapper<TcommCustomer>().eq(TcommCustomer::getCustNo, grpCode).last("LIMIT 1"));
        return R.ok(buildCustInfo(grpCode, cust));
    }

    private Map<String, Object> buildCustInfo(String grpCode, TcommCustomer cust) {
        Map<String, Object> info = new LinkedHashMap<>();

        // 主要数据来源：ftp_cust_base_info
        FtpCustBaseInfoDo bi = ftpCustBaseInfoMapper.selectOne(
                new LambdaQueryWrapper<FtpCustBaseInfoDo>().eq(FtpCustBaseInfoDo::getGrpCode, grpCode).last("LIMIT 1"));

        if (bi != null) {
            // 集团基本信息
            safePut(info, "areaName", bi.getAreaName());
            safePut(info, "countyName", bi.getCountyName());
            safePut(info, "custName", bi.getGrpName());
            safePut(info, "grpCode", bi.getGrpCode());
            safePut(info, "hqCode", bi.getHqCustGrpId());
            safePut(info, "incomeCode", bi.getCustSvcNum());
            safePut(info, "isImpPrt", bi.getIsImpPrt());
            safePut(info, "certificateType", bi.getCertTypeName());
            safePut(info, "certificateNo", bi.getCertNum());
            safePut(info, "custStatus", bi.getGrpStatusName());
            safePut(info, "custLevel", bi.getGrpType());         // 老框架用 grp_type 不是 grp_type_name
            safePut(info, "expandAttr", bi.getGrpExpandTypeName());
            safePut(info, "deptName", bi.getGrpBusiDeptTypeName());
            safePut(info, "listExpandDept", bi.getExpandDeptName());
            // 客户联系人信息
            safePut(info, "contactPerson", bi.getOperatorName());  // 经办人 = operator_name
            safePut(info, "contactPhone", bi.getOperatorPhone());
            safePut(info, "contactName", bi.getContactMan());       // 联系人 = contact_man
            // 名单制信息
            safePut(info, "listAttr", bi.getProvListTypeName());
            safePut(info, "listCustCode", bi.getListCode());
            safePut(info, "listCustName", bi.getListName());
            safePut(info, "listManagerNo", bi.getListManagerId());
            safePut(info, "listManagerName", bi.getListManagerName());
            // 服务经理信息
            safePut(info, "managerNo", bi.getServManagerId());
            safePut(info, "managerName", bi.getServManagerName());
            safePut(info, "managerPhone", bi.getServManagerPhone());
            safePut(info, "accountManager", bi.getAccManagerName());
            safePut(info, "networkManager", bi.getNetManagerName());
            safePut(info, "productManager", bi.getProdManagerName());
            // 地址信息
            safePut(info, "custAddr", bi.getGrpAddr());
        }

        // 补充：TcommCustomer 的数据作为兜底
        if (cust != null) {
            if (info.get("custName") == null) safePut(info, "custName", cust.getCustName());
            if (info.get("grpCode") == null) safePut(info, "grpCode", cust.getCustNo());
            if (info.get("certificateType") == null) safePut(info, "certificateType", cust.getCertificateType());
            if (info.get("certificateNo") == null) safePut(info, "certificateNo", cust.getCertificateNo());
            if (info.get("custAddr") == null) safePut(info, "custAddr", cust.getCustAddr());
            if (info.get("custStatus") == null) safePut(info, "custStatus", cust.getCustState());
        }

        return info;
    }

    private void safePut(Map<String, Object> map, String key, Object val) {
        if (val != null && !"".equals(val.toString())) {
            map.put(key, val);
        }
    }

}
