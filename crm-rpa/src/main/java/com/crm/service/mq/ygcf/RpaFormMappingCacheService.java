package com.crm.service.mq.ygcf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.constant.CacheNames;
import com.crm.common.utils.redis.CacheUtils;
import com.crm.system.domain.rpa.ZqztRpaFormMappingDo;
import com.crm.system.mapper.rpa.ZqztRpaFormMappingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.impl.internal.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RpaFormMappingCacheService {

    @Autowired
    private ZqztRpaFormMappingMapper baseMapper;

    /**
     * 初始化RPA表单映射缓存数据
     */
    @PostConstruct
    public void initRefresh() {
        try {
            int i = refreshAll();
            log.info("预加载RPA表单映射缓存数据完成，共加载 {} 条记录", i);
        } catch (Exception e) {
            log.error("预加载RPA表单映射缓存数据失败", e);
        }
    }

    /**
     * 刷新RPA表单映射缓存数据
     *
     * @return 刷新条数
     */
    public int refreshAll() {
        ConcurrentHashMap<String, List<ZqztRpaFormMappingDo>> map = new ConcurrentHashMap<>();
        // 获取RPA表单映射数据
        baseMapper.selectList().forEach(item ->
            map.computeIfAbsent(item.getBizCode(),
                    k -> new CopyOnWriteArrayList<>())
                .add(item)
        );

        map.forEach((bizCode, list) ->
            CacheUtils.put(
                CacheNames.ZQZT_RPA_FORM_MAPPING,
                bizCode,
                list
            )
        );
        int total = map.values().stream().mapToInt(List::size).sum();
        log.info("刷新RPA表单映射缓存完成，总条数={}", total);
        return total;
    }

    /**
     * 流程编码刷新RPA表单映射缓存
     *
     * @param bizCode 流程编码
     * @return 刷新条数
     */
    public int refreshByBizCode(String bizCode) {
        CacheUtils.evict(CacheNames.ZQZT_RPA_FORM_MAPPING, bizCode);

        List<ZqztRpaFormMappingDo> list =
            baseMapper.selectList(
                new LambdaQueryWrapper<ZqztRpaFormMappingDo>()
                    .eq(ZqztRpaFormMappingDo::getBizCode, bizCode)
            );

        CacheUtils.put(
            CacheNames.ZQZT_RPA_FORM_MAPPING,
            bizCode,
            list.isEmpty() ? Collections.emptyList() : list
        );
        log.info("刷新RPA表单映射缓存，bizCode={}，条数={}", bizCode, list.size());
        return list.size();
    }

    /**
     * 获取缓存RPA表单映射数据
     *
     * @param bizCode 流程编码
     * @return RPA表单映射列表
     */
    public List<ZqztRpaFormMappingDo> getByBizCode(String bizCode) {
        Object obj = CacheUtils.get(CacheNames.ZQZT_RPA_FORM_MAPPING, bizCode);

        // 1. 缓存命中，且是 List
        if (obj instanceof List<?>) {
            List<ZqztRpaFormMappingDo> rawList = (List<ZqztRpaFormMappingDo>) obj;

            // 2. 防御性处理：只保留 ZqztRpaFormMappingDo 类型，其他忽略（或抛异常）
            List<ZqztRpaFormMappingDo> safeList = rawList.stream()
                .filter(ZqztRpaFormMappingDo.class::isInstance)
                .map(ZqztRpaFormMappingDo.class::cast)
                .collect(Collectors.toList()); // Java 8+ 用 Collectors.toList()

            // 3. 返回不可变列表，防止外部修改
            return Collections.unmodifiableList(safeList);
        }

        // 缓存未命中，查数据库
        try {
            List<ZqztRpaFormMappingDo> list = baseMapper.selectList(
                new LambdaQueryWrapper<ZqztRpaFormMappingDo>()
                    .eq(ZqztRpaFormMappingDo::getBizCode, bizCode)
            );

            // 4. 缓存空列表也存（避免重复查空）
            CacheUtils.put(
                CacheNames.ZQZT_RPA_FORM_MAPPING,
                bizCode,
                list.isEmpty() ? Collections.emptyList() : new ArrayList<>(list) // 防御性拷贝
            );

            // 5. 返回不可变列表
            return Collections.unmodifiableList(list);
        } catch (Exception e) {
            log.warn("查询zqzt_rpa_form_mapping表时发生错误，bizCode: {}, 错误: {}", bizCode, e.getMessage());
        }

        // 返回空不可变列表
        return Collections.emptyList();
    }

}
