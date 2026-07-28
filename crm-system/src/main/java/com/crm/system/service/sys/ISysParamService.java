package com.crm.system.service.sys;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dao.SysParamDo;

import java.util.List;
import java.util.Map;

public interface ISysParamService {
    Map<String, String> findValueMapsByType(String type);

    SysParamDo findByCode(String code);

    /**
     * 根据参数编码获取参数值
     * @param code 参数编码
     * @return 参数值，不存在返回null
     */
    String findValueByCode(String code);

    SysParamDo findValueByTypeCode(String type, String code);

    /**
     * 分页查询配置列表
     */
    TableDataInfo<SysParamDo> selectPageParamList(SysParamDo param, PageQuery pageQuery);

    /**
     * 查询配置列表
     */
    List<SysParamDo> selectParamList(SysParamDo param);

    /**
     * 根据ID查询配置
     */
    SysParamDo selectParamById(Long id);

    /**
     * 新增配置
     */
    int insertParam(SysParamDo param);

    /**
     * 修改配置
     */
    int updateParam(SysParamDo param);

    /**
     * 批量删除配置
     */
    void deleteParamByIds(Long[] ids);

    /**
     * 获取所有不重复的配置类型
     */
    List<String> selectDistinctTypes();

    /**
     * 重置配置缓存
     */
    void resetParamCache();
}
