package com.crm.adapter.admin.system;

import com.crm.common.core.domain.R;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.WideWarningUserListExportVo;
import com.crm.system.mapper.WxCustOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/wxCustOrder")
public class WxCustOrderController {

    private final WxCustOrderMapper mapper;

    @GetMapping("/info")
    public R<Map<String, Object>> info(@RequestParam Integer orderId) {
        Map<String, Object> order = mapper.selectOrderInfo(orderId);
        return order == null ? R.fail("订单不存在") : R.ok(order);
    }

    @GetMapping("/processHistory")
    public R<List<Map<String, Object>>> processHistory(@RequestParam String piId) {
        return R.ok(mapper.selectProcessHistory(piId));
    }

    /**
     * 宽带到期预警详情（对应老框架 wideWarningInfo.jsp + infoByWIDE.jsp）
     * <p>
     * 返回：detail（wide_sys_auto_warning 单条详情字段）、
     * summary（ftp_tc_user_cust_warning 摘要：grpName/grpCode/userScale/warningType）、
     * userList（wide_sys_auto_warning 按 main_order_flow_no 的用户清单）
     * </p>
     */
    @GetMapping("/wideInfo")
    public R<Map<String, Object>> wideInfo(@RequestParam Integer orderId) {
        Map<String, Object> order = mapper.selectOrderInfo(orderId);
        if (order == null) {
            return R.fail("订单不存在");
        }
        Object infoId = order.get("infoId");
        if (infoId == null) {
            return R.fail("订单无预警记录ID");
        }
        Long id = Long.valueOf(infoId.toString());
        Map<String, Object> result = new HashMap<>();
        result.put("detail", mapper.selectWideSysAutoWarning(id));
        Map<String, Object> summary = mapper.selectFtpTcUserCustWarning(id);
        result.put("summary", summary);
        if (summary != null && summary.get("mainOrderFlowNo") != null) {
            result.put("userList", mapper.selectWideUserList(String.valueOf(summary.get("mainOrderFlowNo"))));
        } else {
            result.put("userList", Collections.emptyList());
        }
        return R.ok(result);
    }

    /**
     * 导出宽带到期预警用户清单（对应老框架 exportUserCustWarningList）
     */
    @PostMapping("/exportWideUserList")
    public void exportWideUserList(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        List<WideWarningUserListExportVo> list = new ArrayList<>();
        Map<String, Object> order = mapper.selectOrderInfo(orderId.intValue());
        if (order != null && order.get("infoId") != null) {
            Map<String, Object> summary = mapper.selectFtpTcUserCustWarning(Long.valueOf(order.get("infoId").toString()));
            if (summary != null && summary.get("mainOrderFlowNo") != null) {
                List<Map<String, Object>> records = mapper.selectWideUserList(String.valueOf(summary.get("mainOrderFlowNo")));
                for (Map<String, Object> r : records) {
                    WideWarningUserListExportVo vo = new WideWarningUserListExportVo();
                    vo.setSvcNum(str(r, "svcNum"));
                    vo.setCustName(str(r, "custName"));
                    vo.setContactMan(str(r, "contactMan"));
                    vo.setContactPhone(str(r, "contactPhone"));
                    vo.setDeveloperName(str(r, "developerName"));
                    vo.setSchemeName(str(r, "schemeName"));
                    vo.setLvlName(str(r, "lvlName"));
                    vo.setServiceName(str(r, "serviceName"));
                    vo.setConnType(str(r, "connType"));
                    vo.setAmountPerYear(str(r, "amountPerYear"));
                    vo.setEffTime(str(r, "effTime"));
                    vo.setExpTime(str(r, "expTime"));
                    vo.setRate(str(r, "rate"));
                    vo.setUserAddr(str(r, "userAddr"));
                    boolean renew = "1".equals(str(r, "exteFlag")) && str(r, "relationTypeName") == null;
                    boolean transfer = "1".equals(str(r, "exteFlag")) && str(r, "relationTypeName") != null;
                    vo.setExteFlag("1".equals(str(r, "exteFlag")) ? "是" : "否");
                    vo.setNewSchemeName(renew ? str(r, "newSchemeName") : "");
                    vo.setNewCreateTime(renew ? str(r, "newCreateTime") : "");
                    vo.setNewEffTime(renew ? str(r, "newEffTime") : "");
                    vo.setNewExpTime(renew ? str(r, "newExpTime") : "");
                    vo.setNewSchemePeriod(str(r, "newSchemePeriod"));
                    vo.setNewSchemeFee(str(r, "newSchemeFee"));
                    vo.setTransferFlag(transfer ? "是" : "否");
                    vo.setNewSchemeId(str(r, "newSchemeId"));
                    vo.setNewSchemeNameTransfer(transfer ? str(r, "newSchemeName") : "");
                    vo.setNewCreateTimeTransfer(transfer ? str(r, "newCreateTime") : "");
                    vo.setNewEffTimeTransfer(transfer ? str(r, "newEffTime") : "");
                    vo.setNewExpTimeTransfer(transfer ? str(r, "newExpTime") : "");
                    vo.setUserStatus(str(r, "userStatus"));
                    list.add(vo);
                }
            }
        }
        ExcelUtil.exportExcel(list, "宽带到期预警用户清单", WideWarningUserListExportVo.class, response);
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        return v == null ? null : v.toString();
    }
}
