package com.crm.web.controller.system;

import com.crm.common.core.domain.R;
import com.crm.common.core.domain.dto.CustomerTagSyncDTO;
import com.crm.system.service.IZqztCustomerTagSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * RPA客户标签同步接口
 * 供RPA服务通过HTTP调用，同步集团客户标签信息
 *
 * @author crm
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/system/customerTag")
public class ZqztCustomerTagSyncController {

    private final IZqztCustomerTagSyncService customerTagSyncService;

    /**
     * RPA调用：同步集团客户标签信息
     */
    @PostMapping("/sync")
    public R<String> sync(@RequestBody CustomerTagSyncDTO request) {
        log.info("[客户标签同步] 接收RPA同步请求: hqGrpCode={}, bizCode={}",
            request.getHqGrpCode(), request.getBizCode());

        try {
            Boolean result = customerTagSyncService.syncCustomerTags(request);
            if (Boolean.TRUE.equals(result)) {
                return R.ok("success");
            } else {
                return R.fail("同步失败");
            }
        } catch (Exception e) {
            log.error("[客户标签同步] 同步异常: {}", e.getMessage(), e);
            return R.fail("同步异常: " + e.getMessage());
        }
    }
}
