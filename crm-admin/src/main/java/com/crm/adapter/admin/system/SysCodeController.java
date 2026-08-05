package com.crm.adapter.admin.system;

import com.crm.common.core.domain.R;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.service.sys.ISysCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 统一编码 Controller
 * 提供字典选项加载接口，供前端动态渲染下拉/复选框
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sysCode")
public class SysCodeController {

    private final ISysCodeService sysCodeService;

    /**
     * 根据字典类型和可选过滤条件查询，返回 [{label, value}] 格式
     * 对标老系统 /sysCode/getCodeValueAndNameListByType
     *
     * @param type        字典类型（必填）
     * @param settingStr1 过滤条件（可选，三级分类级联时传入上级code）
     */
    @PostMapping({"/codeNameListByType", "/getCodeValueAndNameListByType"})
    public R<List<Map<String, String>>> codeNameListByType(@RequestParam String type,
                                                           @RequestParam(required = false) String settingStr1) {
        SysCodeBo bo = new SysCodeBo();
        bo.setType(type);
        if (StringUtils.isNotBlank(settingStr1)) {
            bo.setSettingStr1(settingStr1);
        }
        List<SysCodeVo> list = sysCodeService.queryList(bo);
        List<Map<String, String>> result = new ArrayList<>();
        for (SysCodeVo vo : list) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("label", vo.getName());
            item.put("value", vo.getCode());
            result.add(item);
        }
        return R.ok(result);
    }
}
