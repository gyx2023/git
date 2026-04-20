package com.crm.system.service.encrypt;

import com.crm.system.domain.bo.encrypt.SysFieldEncryptConfigBo;
import com.crm.system.domain.vo.encrypt.SysFieldEncryptConfigVo;

import java.util.List;

/**
 * 敏感字段加密配置Service接口
 *
 * @author tjx
 * @date 2026-04-17
 */
public interface ISysFieldEncryptConfigService {

    /**
     * 查询敏感字段加密配置列表
     *
     * @param bo 查询条件
     * @return 配置列表
     */
    List<SysFieldEncryptConfigVo> queryList(SysFieldEncryptConfigBo bo);

    /**
     * 新增敏感字段加密配置
     *
     * @param bo 配置信息
     * @return 是否成功
     */
    Boolean insertByBo(SysFieldEncryptConfigBo bo);

    /**
     * 修改敏感字段加密配置
     *
     * @param bo 配置信息
     * @return 是否成功
     */
    Boolean updateByBo(SysFieldEncryptConfigBo bo);

    /**
     * 删除敏感字段加密配置
     *
     * @param ids ID列表
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(List<Long> ids);

    /**
     * 根据ID查询配置
     *
     * @param id 配置ID
     * @return 配置信息
     */
    SysFieldEncryptConfigVo queryById(Long id);
}
