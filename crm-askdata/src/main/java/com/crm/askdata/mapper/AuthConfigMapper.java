package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.AuthConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 外部系统认证配置 Mapper
 *
 * @author crm
 */
@Mapper
public interface AuthConfigMapper extends BaseMapper<AuthConfig> {

    /**
     * 根据系统名称查询认证配置
     *
     * @param systemName 系统名称
     * @return 认证配置
     */
    @Select("SELECT * FROM askdata_auth_config WHERE system_name = #{systemName} AND del_flag = '0'")
    AuthConfig selectBySystemName(String systemName);

    /**
     * 查询所有启用的认证配置
     *
     * @return 认证配置列表
     */
    @Select("SELECT * FROM askdata_auth_config WHERE status = '0' AND del_flag = '0' ORDER BY system_name")
    List<AuthConfig> selectEnabledConfigs();

    /**
     * 检查系统名称是否已存在（排除自身）
     *
     * @param systemName 系统名称
     * @param excludeId  排除的ID
     * @return 匹配数量
     */
    @Select("SELECT COUNT(*) FROM askdata_auth_config WHERE system_name = #{systemName} AND id != #{excludeId} AND del_flag = '0'")
    int countBySystemNameExcludeId(String systemName, Long excludeId);
}
