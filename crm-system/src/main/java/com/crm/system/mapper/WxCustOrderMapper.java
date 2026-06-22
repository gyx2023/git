package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WxCustOrderMapper {
    Map<String, Object> selectOrderInfo(@Param("orderId") Integer orderId);
    List<Map<String, Object>> selectProcessHistory(@Param("piId") String piId);
}
