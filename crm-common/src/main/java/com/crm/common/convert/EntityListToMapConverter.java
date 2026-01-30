package com.crm.common.convert;

import com.crm.common.core.domain.dto.GrpGroupImportDTO;
import com.crm.common.core.domain.dto.ThreeFieldDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityListToMapConverter {

    /**
     * 高性能转换：DataEntity列表 → Map(key=keyField, value=ThreeFieldDTO)
     * 核心优化点：
     * 1. 提前初始化Map容量，避免扩容
     * 2. 增强for循环，无迭代器/流的额外开销
     * 3. 直接getter取值，无类型转换
     * 4. DTO用final+构造器，减少对象开销
     *
     * @param entityList 数据库查询返回的实体类列表
     * @return 转换后的Map
     */
    public static Map<String, ThreeFieldDTO> convertToMap(List<GrpGroupImportDTO> entityList) {
        // 边界处理：空列表返回不可变空Map，避免NPE且减少内存
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 性能核心：初始化Map容量为 (列表大小 / 负载因子0.75) + 1，完全避免扩容
        int initialCapacity = (int) (entityList.size() / 0.75) + 1;
        Map<String, ThreeFieldDTO> resultMap = new HashMap<>(initialCapacity);

        // 增强for循环（JVM优化最充分，比stream/迭代器快）
        for (GrpGroupImportDTO entity : entityList) {
            // 跳过null实体（避免空指针，生产环境建议日志记录）
            if (entity == null) {
                continue;
            }

            // 1. 提取key（直接getter，无类型转换）
            String key = entity.getManagerid();
            // 跳过空key（避免key=null导致的HashMap异常，按需处理）
            if (key == null) {
                continue;
            }

            // 2. 封装DTO（构造器直接初始化，无setter开销）
            ThreeFieldDTO dto = new ThreeFieldDTO(
                entity.getEmpId(),
                entity.getOaUserName(),
                entity.getFullName()
            );

            // 3. 放入Map（无冗余操作）
            resultMap.put(key, dto);
        }

        return resultMap;
    }

    // 测试示例
}
