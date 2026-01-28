package com.crm.web.controller.system;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.grp.TfFCustGroupMDo;
import com.crm.system.service.ICustMaintInfoService;
import com.crm.system.service.ITfFCustGroupMService;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custMaintInfo")
public class CustMaintInfoController extends BaseController {
    private final IZqztGrpContractOtherInfoService iZqztGrpContractOtherInfoService;

    @GetMapping("/selectCustMaintInfoList")
    public R<List<Map<String, Object>>> selectCustMaintInfoList(@RequestParam String hqGrpCode, @RequestParam String contractId){
        try {
            // 调用服务层实现的完整业务逻辑
            List<Map<String, Object>> uncontractedUsers = iZqztGrpContractOtherInfoService.selectUncontractedCustomersByHqGrpCode(hqGrpCode, contractId);
            return R.ok(uncontractedUsers);
        } catch (Exception e) {
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
