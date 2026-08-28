package com.crm.system.mapper;

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

/**
 * 台账数据主表 Mapper
 * 类级 @DataPermission：对 dataScope=6(客户经理) 的角色自动追加 created_by = 当前登录username
 */
@Mapper
@DataPermission({
    @DataColumn(key = "custManagerOauser", value = "created_by")
})
public interface ZqztLedgerInfoMapper extends BaseMapperPlus<ZqztLedgerInfoMapper, ZqztLedgerInfo, ZqztLedgerInfoVo> {

    /**
     * 分页查询（数据权限 + 包装器条件在 XML 的 ${ew.getCustomSqlSegment} 中合并）
     */
    Page<ZqztLedgerInfoVo> selectPageList(@Param("page") Page<ZqztLedgerInfoVo> page,
        @Param(Constants.WRAPPER) Wrapper<ZqztLedgerInfo> queryWrapper);
}
