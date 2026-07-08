package com.crm.system.service;

import com.crm.common.annotation.DataColumn;
import com.crm.common.annotation.DataPermission;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.bo.SpecialListBo;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListVo;

import java.util.Collection;
import java.util.List;

/**
 * 特殊名单服务接口
 *
 * @author crm
 * @date 2026-06-16
 */
public interface ISpecialListService {

    /**
     * 分页查询特殊名单列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    @DataPermission({
        @DataColumn(key = "createBy", value = "SPECIAL_LIST")
    })
    TableDataInfo<SpecialListVo> queryPageList(SpecialListBo bo, PageQuery pageQuery);

    /**
     * 新增特殊名单
     *
     * @param bo 业务对象
     * @return 结果
     */
    Boolean insertByBo(SpecialListBo bo);

    /**
     * 修改特殊名单
     *
     * @param bo 业务对象
     * @return 结果
     */
    Boolean updateByBo(SpecialListBo bo);

    /**
     * 批量删除特殊名单（逻辑删除，状态置为0）
     *
     * @param ids 需要删除的主键集合
     * @return 结果
     */
    Boolean deleteWithValidByIds(Collection<Integer> ids);

    /**
     * 检查是否重复
     *
     * @param sceneCode   场景编码
     * @param targetValue 目标值
     * @param excludeId   排除的主键ID
     * @return true=重复
     */
    boolean checkDuplicate(String sceneCode, String targetValue, Integer excludeId);


    /**
     * 批量插入
     *
     * @param list 插入列表
     */
    void batchInsert(List<SpecialListDo> list);

    /**
     * 批量更新
     *
     * @param list 更新列表
     */
    void batchUpdate(List<SpecialListDo> list);

}
