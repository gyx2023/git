package com.crm.system.service.wx;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.mapper.sys.SysCodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 维系订单类型判断（对应老框架 getIsWxOrder: maintenance_order + wx_warning_second）
 * <p>
 * 供 PendingEventListener / PendingServiceImpl 等共用，避免各组件重复加载 sys_code 配置
 * </p>
 */
@Slf4j
@Component
public class WxProcessKeySupport {

    /** 维系类订单类型集合 */
    private final Set<String> wxProcessKeys = new HashSet<>();

    @Resource
    private SysCodeMapper sysCodeMapper;

    @PostConstruct
    public void init() {
        try {
            List<SysCode> codes = sysCodeMapper.selectList(
                    new LambdaQueryWrapper<SysCode>()
                            .eq(SysCode::getType, "process_key")
                            .in(SysCode::getSettingStr3,
                                    Arrays.asList("maintenance_order", "wx_warning_second")));
            for (SysCode code : codes) {
                wxProcessKeys.add(code.getCode());
            }
            log.info("维系订单类型加载完成，共{}个: {}", wxProcessKeys.size(), wxProcessKeys);
        } catch (Exception e) {
            log.warn("加载维系订单类型失败", e);
        }
    }

    /** 是否为维系预警流程 */
    public boolean isWxOrder(String processKey) {
        return processKey != null && wxProcessKeys.contains(processKey);
    }
}
