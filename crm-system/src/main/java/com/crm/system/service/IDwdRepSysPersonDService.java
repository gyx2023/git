package com.crm.system.service;

import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface IDwdRepSysPersonDService {

    /**
     * 查询【请填写功能名称】
     */
    DwdRepSysPersonDVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<DwdRepSysPersonDVo> queryPageList(DwdRepSysPersonDBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<DwdRepSysPersonDVo> queryList(DwdRepSysPersonDBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(DwdRepSysPersonDBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(DwdRepSysPersonDBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据县市编码和拓展客户类型查询客户经理信息
     * @param cityCode 县市编码
     * @param expandCustType 拓展客户类型(01:省级,02:市级,03:县级)
     * @return 客户经理信息列表
     */
    List<Map<String, Object>> queryManagersByCityCodeAndType(String cityCode,@RequestParam(required = false) String expandCustType,@RequestParam(required = false) String grpCustType);

    /**
     * 批量根据姓名和电话查询人员地区信息
     * @param personInfos 人员信息列表 [{fullName: "张三", mobile: "13800138000"}, ...]
     * @return 人员地区信息列表
     */
    List<DwdRepSysPersonDVo> selectAreaAndCounty(List<Map<String, String>> personInfos);
}
