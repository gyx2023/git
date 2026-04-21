package com.crm.system.mapper.encrypt;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.encrypt.SysFieldEncryptConfigDo;
import com.crm.system.domain.vo.encrypt.SysFieldEncryptConfigVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 敏感字段加密配置Mapper接口
 *
 * @author tjx
 * @date 2026-04-17
 */
@Mapper
public interface SysFieldEncryptConfigMapper extends BaseMapperPlus<SysFieldEncryptConfigMapper, SysFieldEncryptConfigDo, SysFieldEncryptConfigVo> {

    /**
     * 获取状态为待执行(0)或失败(3)的表名列表（去重）
     *
     * @return 表名列表
     */
    List<String> selectDistinctTableNamesWithPendingStatus();

    /**
     * 查询多字段的批次数据（使用游标分页）
     *
     * @param tableName 表名
     * @param selectFields 查询字段（逗号分隔）
     * @param primaryKey 主键字段（用于 ORDER BY）
     * @param limit 限制条数
     * @param lastId 上一批次的最后一个主键值（用于游标分页，首次查询传 null）
     * @return 数据列表
     */
    @MapKey("id")
    List<Map<String, Object>> selectBatchDataForMultipleFieldsWithoutWhere(
        @Param("tableName") String tableName,
        @Param("selectFields") String selectFields,
        @Param("primaryKey") String primaryKey,
        @Param("limit") int limit,
        @Param("lastId") Object lastId
    );

    /**
     * 查询表的总数据量
     *
     * @param tableName 表名
     * @return 总数量
     */
    Long countTotalDataWithoutWhere(@Param("tableName") String tableName);

    /**
     * 校验定位字段的唯一性
     *
     * @param tableName 表名
     * @param primaryKey 主键字段
     * @return 重复数据示例
     */
    @MapKey("cnt")
    List<Map<String, Object>> validatePrimaryKeyUniqueness(
        @Param("tableName") String tableName,
        @Param("primaryKey") String primaryKey
    );

    /**
     * 动态单条更新加密字段
     *
     * @param tableName 表名
     * @param fieldName 待加密字段名
     * @param primaryKey 定位字段名（支持逗号分隔的多字段）
     * @param record 待更新记录，包含 "encryptedValue" 和定位字段的值
     */
    void updateEncryptedField(@Param("tableName") String tableName,
                              @Param("fieldName") String fieldName,
                              @Param("primaryKey") String primaryKey,
                              @Param("record") Map<String, Object> record);
}
