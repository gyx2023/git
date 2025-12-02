package com.crm.system.service;

import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDictData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 政企集团客户信息Service接口
 *
 * @author crm
 * @date 2025-11-20
 */
public interface IZqGrpGroupInfoService {

    /**
     * 查询政企集团客户信息
     */
    ZqGrpGroupInfoVo queryById(Long grpId);

    /**
     * 根据总部集团编码查询政企集团客户信息
     *
     * @param hqGrpCode 总部集团编码
     * @return 政企集团客户信息
     */
    ZqGrpGroupInfo selectByHqGrpCode(String hqGrpCode);

    /**
     * 查询政企集团客户信息列表
     */
    TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery);
    TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd);

    /**
     * 查询政企集团客户信息列表
     */
    List<ZqGrpGroupInfoVo> queryList(ZqGrpGroupInfoBo bo);

    /**
     * 新增政企集团客户信息
     */
    Boolean insertByBo(ZqGrpGroupInfoBo bo);

    /**
     * 修改政企集团客户信息
     */
    Boolean updateByBo(ZqGrpGroupInfoBo bo);

    /**
     * 修改政企集团客户信息
     */
    Boolean updateByBo1(ZqGrpGroupInfoBo bo);
    /**
     * 校验并批量删除政企集团客户信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 获取政企集团客户信息相关字典数据
     */
    Map<String, List<SysDictData>> getDictData();
    
    /**
     * 客户经理级别枚举
     */
    enum ManagerLevel {
        /**
         * 省级
         */
        PROVINCIAL,

        /**
         * 市级
         */
        CITY,

        /**
         * 县级
         */
        COUNTY
    }
    
    /**
     * 根据countyId和countyName判断客户经理级别
     *
     * @param countyId   县ID
     * @param countyName 县名
     * @return 客户经理级别
     */
    ManagerLevel determineManagerLevel(Long countyId, String countyName);
    
    /**
     * 根据用户的OA账号确定其客户经理级别，并设置相应的数据过滤条件
     * @param bo 查询条件对象
     * @param currentUserOaAccount 当前用户的OA账号
     */
    void applyDataManagerPermission(ZqGrpGroupInfoBo bo, String currentUserOaAccount);
}