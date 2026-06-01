package com.crm.web.controller.servicehall;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.core.domain.R;
import com.crm.service.mq.ygcf.RpaFormMappingCacheService;
import com.crm.system.domain.rpa.ZqztRpaFormMappingDo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/capability/development/rpa/form-mapping/cache")
@Slf4j
public class RpaFormMappingCacheController {

    @Resource
    private RpaFormMappingCacheService rpaFormMappingCacheService;

    /**
     * 刷新缓存
     */
    @PostMapping(value = "/refresh/{bizCode}")
    @ResponseBody
    @SaIgnore
    public R<Map<String, Object>> refreshByBizCode(@PathVariable String bizCode) {
        int count = "all".equalsIgnoreCase(bizCode)
            ? rpaFormMappingCacheService.refreshAll()
            : rpaFormMappingCacheService.refreshByBizCode(bizCode);

        String msg = "刷新RPA表单映射缓存，bizCode=" + bizCode + "，条数=" + count;
        Map<String, Object> map = new HashMap<>();
        map.put("bizCode", bizCode);
        map.put("count", count);
        return R.ok(msg, map);
    }
}
