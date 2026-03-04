package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.crm.common.annotation.DataColumn;
import com.crm.common.annotation.DataPermission;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门管理 数据层
 *
 * @author Lion Li
 */
public interface SysDeptMapper extends BaseMapperPlus<SysDeptMapper, SysDept, SysDept> {

    /**
     * 查询部门管理数据
     *
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
//    @DataPermission({
//        @DataColumn(key = "deptName", value = "dept_id")
//    })
    List<SysDept> selectDeptList(@Param(Constants.WRAPPER) Wrapper<SysDept> queryWrapper);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    /**
     * 清空zqzt_sys_dept表（使用TRUNCATE）
     * 用于数据同步前的表清空操作
     */
    @org.apache.ibatis.annotations.Select("TRUNCATE TABLE zqzt_sys_dept")
    void executeTruncate();

    /**
     * 清空ancestors字段
     */
    void updateAncestorsToEmpty();

    /**
     * 更新根节点的ancestors（parent_id为0或NULL或等于自身dept_id的节点）
     */
    void updateRootAncestors();

    /**
     * 更新子节点的ancestors
     */
    int updateChildAncestors();

    /**
     * 计算待更新ancestors的节点数量
     */
    int countPendingAncestorsUpdate();

    /**
     * 更新部门的level字段
     */
    void updateDeptLevel();

    List<SysDept> selectDqDeptList(@Param("deptId") Long deptId);

    List<SysDept> selectDeptQdList(@Param("deptId") Long deptId);

}
