package com.crm.system.mapper.encrypt;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.encrypt.SysFieldEncryptConfigDo;
import com.crm.system.domain.vo.encrypt.SysFieldEncryptConfigVo;
import org.apache.ibatis.annotations.Mapper;
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
