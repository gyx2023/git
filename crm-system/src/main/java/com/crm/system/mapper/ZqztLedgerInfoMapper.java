package com.crm.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.annotation.DataColumn;
import com.crm.common.annotation.DataPermission;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 台账数据主表 Mapper
 * 类级 @DataPermission：对 dataScope=6(客户经理) 的角色自动追加 created_by = 当前登录username
 */
@Mapper
@DataPermission({
    @DataColumn(key = "custManagerOauser", value = "created_by")
})
public interface ZqztLedgerInfoMapper extends BaseMapperPlus<ZqztLedgerInfoMapper, ZqztLedgerInfo, ZqztLedgerInfoVo> {

    /** 定时导出专用：忽略页面数据权限，查询台账数据主表总数。 */
    @InterceptorIgnore(dataPermission = "true")
    Long selectAllForExportCount();

    /** 定时导出专用：查询最小主键。 */
    @InterceptorIgnore(dataPermission = "true")
    Long selectAllForExportMinId();

    /** 定时导出专用：查询最大主键。 */
    @InterceptorIgnore(dataPermission = "true")
    Long selectAllForExportMaxId();

    /** 定时导出专用：按主键范围查询全字段数据。 */
    @InterceptorIgnore(dataPermission = "true")
    List<ZqztLedgerInfo> selectAllForExportByRange(@Param("startId") long startId,
                                                   @Param("endId") long endId);

    /**
     * 分页查询（数据权限 + 包装器条件在 XML 的 ${ew.getCustomSqlSegment} 中合并）
     */
    Page<ZqztLedgerInfoVo> selectPageList(@Param("page") Page<ZqztLedgerInfoVo> page,
        @Param(Constants.WRAPPER) Wrapper<ZqztLedgerInfo> queryWrapper);
}
