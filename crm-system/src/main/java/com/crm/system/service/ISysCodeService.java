package com.crm.system.service;

import com.crm.system.domain.SysCode;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.domain.bo.SysCodeBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 统一编码Service接口
 *
 * @author crm
 * @date 2025-12-15
 */
public interface ISysCodeService {

    /**
     * 查询统一编码
     */
    SysCodeVo queryById(Long id);

    /**
     * 查询统一编码列表
     */
    TableDataInfo<SysCodeVo> queryPageList(SysCodeBo bo, PageQuery pageQuery);

    /**
     * 查询统一编码列表
     */
    List<SysCodeVo> queryList(SysCodeBo bo);

    /**
     * 新增统一编码
     */
    Boolean insertByBo(SysCodeBo bo);

    /**
     * 修改统一编码
     */
    Boolean updateByBo(SysCodeBo bo);

    /**
     * 校验并批量删除统一编码信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    SysCodeVo findCode(String type, String code);

    String findNameByCodes(String type, String codes);

    /**
     * 根据类型查询编码列表并转换为LinkedHashMap
     * @param type 编码类型
     * @return LinkedHashMap，key为code，value为name
     */
    LinkedHashMap<String, String> getCodeMapByType(String type);

    /**
     * 根据类型查询编码的settingStr1映射
     * @param type 编码类型
     * @return HashMap，key为code，value为settingStr1
     */
    HashMap<String, String> getCodeSettingStr1MapByType(String type);
}
