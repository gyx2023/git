package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.service.IWideExpireWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 宽带到期预警 Controller
 * <p>
 * 提供预警明细查询接口。流程操作（complete/reject/transfer/transferList/auditHandlers/track）
 * 已统一由 WideWarningController（/wideWarning）提供。
 * </p>
 * <p>
 * 与沃TV（WotvExpireWarningController）的差异：此处<b>不提供 /export</b>。
 * 宽带到期的用户清单导出已由 {@code /statistics/wxCustOrder/exportWideUserList} 提供
 * （PC 预警处理页在用，导出 VO 复用 {@code WideWarningUserListExportVo}），
 * 重复实现一份没有收益。若后续前端要统一走预警专属路径，再补 /export。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wideExpireWarning")
public class WideExpireWarningController extends BaseController {

    private final IWideExpireWarningDetailService detailService;

    /**
     * 分页查询工单关联的预警明细记录列表（用户清单，取自 wide_sys_auto_warning）
     */
    @GetMapping("/records")
    public TableDataInfo<Map<String, Object>> records(@RequestParam("orderId") Long orderId, PageQuery pageQuery) {
        return detailService.queryPageRecords(orderId, pageQuery);
    }
}
