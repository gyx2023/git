package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.annotation.DataColumn;
import com.crm.common.annotation.DataPermission;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 特殊名单Mapper接口
 *
 * @author crm
 * @date 2026-06-16
 */
@Mapper
public interface SpecialListMapper extends BaseMapperPlus<SpecialListMapper, SpecialListDo, SpecialListVo> {

    @DataPermission({
        @DataColumn(key = "createBy", value = "create_by")
    })
    Page<SpecialListVo> selectPageList(@Param("page") Page<SpecialListVo> page, @Param(Constants.WRAPPER) Wrapper<SpecialListDo> queryWrapper);

}
