package com.crm.adapter.api;

import com.crm.askdata.entity.AuthConfig;
import com.crm.askdata.service.AuthConfigService;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 外部系统认证配置管理 API
 * <p>
 * 提供外部系统认证配置的 CRUD 管理接口，供管理后台使用。
 * 认证配置用于工具调用时注入 HTTP 认证 Header。
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/askdata/auth-config")
public class AuthConfigController extends BaseController {

    private final AuthConfigService authConfigService;

    /**
     * 查询认证配置列表
     *
     * @param keyword   系统名称关键词（可选）
     * @param authType  认证方式（可选，NONE/SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT）
     */
    @GetMapping("/list")
    public R<List<AuthConfig>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "authType", required = false) String authType) {
        List<AuthConfig> list = authConfigService.queryList(keyword, authType);
        return R.ok(list);
    }

    /**
     * 获取所有启用的认证配置（下拉选择用）
     */
    @GetMapping("/enabled")
    public R<List<AuthConfig>> getEnabledConfigs() {
        return R.ok(authConfigService.getEnabledConfigs());
    }

    /**
     * 根据 ID 获取认证配置详情
     *
     * @param id 主键ID
     */
    @GetMapping("/{id}")
    public R<AuthConfig> getById(@PathVariable Long id) {
        AuthConfig config = authConfigService.getById(id);
        if (config == null) {
            return R.fail("认证配置不存在");
        }
        return R.ok(config);
    }

    /**
     * 新增认证配置
     */
    @Log(title = "外部系统认证配置", businessType = BusinessType.INSERT)
    @PostMapping
    public R<AuthConfig> add(@RequestBody AuthConfig config) {
        String operator = getUsername();
        AuthConfig saved = authConfigService.saveConfig(config, operator);
        return R.ok(saved);
    }

    /**
     * 修改认证配置
     */
    @Log(title = "外部系统认证配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<AuthConfig> edit(@RequestBody AuthConfig config) {
        String operator = getUsername();
        AuthConfig saved = authConfigService.saveConfig(config, operator);
        return R.ok(saved);
    }

    /**
     * 删除认证配置（逻辑删除）
     *
     * @param id 主键ID
     */
    @Log(title = "外部系统认证配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        String operator = getUsername();
        authConfigService.deleteConfig(id, operator);
        return R.ok();
    }

    /**
     * 清除认证配置缓存
     */
    @Log(title = "外部系统认证配置", businessType = BusinessType.CLEAN)
    @DeleteMapping("/cache/clear")
    public R<Void> clearCache() {
        authConfigService.clearAllCache();
        log.info("Auth config cache cleared by user: {}", getUsername());
        return R.ok("缓存已清空");
    }
}
