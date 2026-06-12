package com.crm.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo2;
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
     * 查询政企集团客户信息（不脱敏）
     */
    ZqGrpGroupInfo queryByIdWithoutSensitive(Long grpId);

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
    //TableDataInfo<ZqGrpGroupInfoVo> queryPageList(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd);

    /**
     * 查询政企集团客户信息列表（根据sys_param表里的配置决定省市县经理信息是否脱敏）
     */
    TableDataInfo<ZqGrpGroupInfoVo2> queryPageListWithoutSensitive(ZqGrpGroupInfoBo bo, PageQuery pageQuery, String grpCreateTimeBegin, String grpCreateTimeEnd);

    /**
     * 根据areaCode查询政企集团客户信息列表（用于异步任务导出）
     *
     * @param bo 查询条件
     * @param areaCode 区域代码
     * @return 政企集团客户信息列表
     */
    List<ZqGrpGroupInfoVo> exportList(ZqGrpGroupInfoBo bo, String areaCode);

    /**
     * 根据areaCode查询政企集团客户信息列表（用于异步任务导出）
     *
     * @param bo 查询条件
     * @param areaCode 区域代码
     * @return 政企集团客户信息列表
     */
    List<ZqGrpGroupInfoVo> exportListLevel(ZqGrpGroupInfoBo bo, String areaCode);

    /**
     * 根据areaCode分页查询政企集团客户信息列表（用于流式导出）
     *
     * @param bo 查询条件
     * @param page 分页参数
     * @param areaCode 区域代码
     * @return 政企集团客户信息列表
     */
    List<ZqGrpGroupInfoVo2> exportPageList(ZqGrpGroupInfoBo bo, Page page, String areaCode);

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

    String getUserByStt();
}
