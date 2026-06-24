package com.crm.system.mapper;

import com.crm.system.domain.dto.WxCustUserListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.List;
import java.util.Map;

@Mapper
public interface WxCustUserListMapper {
    List<Map<String, Object>> queryList(WxCustUserListDTO dto);
    Long queryCount(WxCustUserListDTO dto);

    /** 查用户org→area权限ID列表 */
    List<String> queryUserAreaIds(Long userId);
}
