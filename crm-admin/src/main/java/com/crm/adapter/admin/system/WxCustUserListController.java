package com.crm.adapter.admin.system;

import com.crm.common.annotation.Log;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.system.service.IWxCustUserListService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 维系预警-客户级用户清单
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/statistics/wxCustUserList")
public class WxCustUserListController {

    private final IWxCustUserListService wxCustUserListService;

    /**
     * 查询维系预警-客户级用户清单
     */
    @Log(title = "微信客户列表", businessType = BusinessType.OTHER)
    @PostMapping("/list")
    public TableDataInfo<Map<String, Object>> list(@RequestBody Map<String, Object> params) {
        return wxCustUserListService.queryList(params);
    }
}
