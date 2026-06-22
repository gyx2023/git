package com.crm.adapter.admin.system;

import com.crm.common.core.domain.R;
import com.crm.system.mapper.WxCustOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
