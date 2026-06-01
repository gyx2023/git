package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.cust.ZqztCustMaintInfoVo;
import com.crm.system.service.cust.IZqztCustMaintInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/custMaintInfo")
public class CustMaintInfoController extends BaseController {
    private final IZqztCustMaintInfoService iZqztCustMaintInfoService;

    @GetMapping("/selectCustMaintInfoList")
    public TableDataInfo<ZqztCustMaintInfoVo> selectCustMaintInfoList(@RequestParam String hqGrpCode, @RequestParam String contractId, PageQuery pageQuery){
        TableDataInfo<ZqztCustMaintInfoVo> custMaintInfoList = iZqztCustMaintInfoService.selectUncontractedUsers(hqGrpCode, contractId, pageQuery);
        return custMaintInfoList;
    }
}
