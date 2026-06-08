package com.crm.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.dto.WorkOrderDTO;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.WxStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WxStrategyServiceImpl implements WxStrategyService {
    @Resource
    private SysCodeMapper sysCodeMapper;
    @Resource
    private ToppOrderMapper toppOrderMapper;

    /**
     * 获取微信策略配置
     */
    @Override
    public Map<String, JSONObject> queryStrategyInfo() {
        Map<String, JSONObject> codeMap = new HashMap<>(16);

        // 查询配置
        List<SysCode> sysCodeList = sysCodeMapper.selectList(new LambdaQueryWrapper<SysCode>()
            .eq(SysCode::getType, "process_key")
            .eq(SysCode::getSettingStr3, "maintenance_order"));

        if (sysCodeList == null || sysCodeList.isEmpty()) {
            log.warn("[策略配置] 未查询到 process_key=maintenance_order 相关配置");
            return codeMap;
        }

        // 遍历解析
        for (SysCode sysCode : sysCodeList) {
            try {
                String configJson = sysCode.getSettingStr2();
                log.info("[策略配置] 解析配置 code={}, config={}", sysCode.getCode(), configJson);

                // 空值判断
                if (configJson == null || configJson.isEmpty()) {
                    log.warn("[策略配置] code={} 的配置字符串为空", sysCode.getCode());
                    continue;
                }

                // 解析JSON
                JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(configJson);
                codeMap.put(sysCode.getCode(), jsonObject);

            } catch (Exception e) {
                log.error("[策略配置] 解析配置异常 code={}", sysCode.getCode(), e);
            }
        }
        return codeMap;
    }

    @Override
    public List<WorkOrderDTO> queryWorkOrder(String orderType, String ymd, Long lastId, Integer size) {
        return toppOrderMapper.queryWorkOrder(orderType, ymd, lastId, size);
    }

    @Override
    public String queryCustCount(String orderType, String ymd) {
        String date = ymd + " 23:59:59";
        return toppOrderMapper.getToppOrderCount(orderType, date).toString();
    }

    @Override
    public String queryOrderCount(String orderType, String ymd) {
        String date = ymd + " 23:59:59";
        return toppOrderMapper.countByOrderTypeAndDate(orderType, date).toString();
    }

    @Override
    public String queryClosedOrderCount(String orderType, String ymd) {
        // 1. 参数拼接日期
        String date = ymd + " 23:59:59";

        // 2. 调用 MyBatis 查询
        Integer count = toppOrderMapper.queryClosedOrderCount(orderType, date);
        return count.toString();
    }


    @Override
    public List<Long> listOrderIdByCursor(String orderType, String yyMMdd, Long lastId, int size) {
        return toppOrderMapper.listOrderIdByCursor(orderType, yyMMdd, lastId, size);
    }

    @Override
    public List<WorkOrderDTO> listWorkOrderByIds(String orderType, List<Long> idList) {
        return toppOrderMapper.listWorkOrderByIds(orderType, idList);
    }

}
