package com.crm.adapter.admin.system.zqzt;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.annotation.Log;
import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.BusinessType;
import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import com.crm.system.domain.bo.ZqztCloudPasswordBo;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.vo.ZqztLedgerAreaVo;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.service.IZqztLedgerAreaService;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.IZqztOrderInfoObService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * 台账信息管理
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/ledger")
public class ZqztLedgerInfoController extends BaseController {

    private final IZqztLedgerInfoService ledgerInfoService;
    private final IZqztLedgerAreaService ledgerAreaService;
    private final IZqztOrderInfoObService orderInfoObService;

    /**
     * 分页查询台账
     */
    @SaCheckPermission("system:ledger:list")
    @GetMapping("/list")
    public TableDataInfo<ZqztLedgerInfoVo> list(ZqztLedgerInfoBo bo, PageQuery pageQuery) {
        return ledgerInfoService.queryPageList(bo, pageQuery);
    }

    /**
     * 台账详情（敏感字段脱敏）
     */
    @SaCheckPermission("system:ledger:list")
    @GetMapping("/{id}")
    public R<ZqztLedgerInfoVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(ledgerInfoService.queryById(id));
    }

    /**
     * 修改回显（不脱敏）
     */
    @SaCheckPermission("system:ledger:edit")
    @GetMapping("/edit/{id}")
    public R<ZqztLedgerInfoVo> getEditInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(ledgerInfoService.queryForEdit(id));
    }

    /**
     * 新增台账
     */
    @SaCheckPermission("system:ledger:add")
    @Log(title = "台账信息管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ZqztLedgerInfoBo bo) {
        try {
            return toAjax(ledgerInfoService.insertByBo(bo));
        } catch (Exception e) {
            return R.fail("新增失败，请稍后再试");
        }
    }

    /**
     * 修改台账
     */
    @SaCheckPermission("system:ledger:edit")
    @Log(title = "台账信息管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ZqztLedgerInfoBo bo) {
        return toAjax(ledgerInfoService.updateByBo(bo));
    }

    /**
     * 删除台账（逻辑删除 is_valid=0）
     */
    @SaCheckPermission("system:ledger:remove")
    @Log(title = "台账信息管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(ledgerInfoService.deleteWithValidByIds(Arrays.asList(ids)));
    }

    /**
     * 按服务大厅工单号自动带出 OB 源表企业信息
     */
    @SaCheckPermission("system:ledger:add")
    @GetMapping("/orderInfo/{orderNo}")
    public R<ZqztLedgerInfoVo> getOrderInfo(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderInfoObService.getOrderInfo(orderNo));
    }

    /**
     * 工单号查重（新增前校验，重复则不允许提交）
     */
    @SaCheckPermission("system:ledger:add")
    @GetMapping("/checkOrderNo/{orderNo}")
    public R<Boolean> checkOrderNo(@PathVariable("orderNo") String orderNo) {
        return R.ok(ledgerInfoService.existsByOrderNo(orderNo));
    }

    /**
     * 省市区县街道三级联动（parentCode 为空默认返回地市）
     */
    @SaCheckPermission("system:ledger:list")
    @GetMapping("/areaChildren")
    public R<List<ZqztLedgerAreaVo>> areaChildren(@RequestParam(value = "parentCode", required = false) String parentCode) {
        return R.ok(ledgerAreaService.queryChildren(parentCode));
    }

    /**
     * 发送云智眼账户改密短信验证码（发到当前登录用户手机）
     */
    @SaCheckPermission("system:ledger:edit")
    @PostMapping("/cloudAccount/sendSmsCode")
    public R<Void> sendCloudAccountSmsCode(@Validated @RequestBody ZqztCloudPasswordBo bo) {
        ledgerInfoService.sendCloudAccountSmsCode(bo.getLedgerId());
        return R.ok();
    }

    /**
     * 当前登录用户脱敏手机号（改密弹窗展示）
     */
    @SaCheckPermission("system:ledger:edit")
    @GetMapping("/cloudAccount/currentPhone")
    public R<String> currentPhone() {
        return R.ok("查询成功", ledgerInfoService.getCurrentUserMaskedPhone());
    }

    /**
     * 修改云智眼账户密码（短信验证码校验通过后）
     */
    @SaCheckPermission("system:ledger:edit")
    @Log(title = "台账信息管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/cloudAccount/updatePassword")
    public R<Void> updateCloudAccountPassword(@Validated @RequestBody ZqztCloudPasswordBo bo) {
        return toAjax(ledgerInfoService.updateCloudAccountPassword(bo.getLedgerId(), bo.getNewPassword(), bo.getSmsCode()));
    }
}
