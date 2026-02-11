package com.crm.web.controller.system;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.service.IZqztGrpContractOtherInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custMaintInfo")
public class CustMaintInfoController extends BaseController {
    private final IZqztGrpContractOtherInfoService iZqztGrpContractOtherInfoService;

    @GetMapping("/selectCustMaintInfoList")
    public R<List<ZqztGrpContractOtherInfoVo>> selectCustMaintInfoList(@RequestParam String hqGrpCode, @RequestParam String contractId){
        try {
            // 调用服务层实现的完整业务逻辑
            List<ZqztGrpContractOtherInfoVo> unContractedUsers = iZqztGrpContractOtherInfoService.selectUncontractedCustomersByHqGrpCode(hqGrpCode, contractId);
            return R.ok(unContractedUsers);
        } catch (Exception e) {
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
