package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.CustTRosterCustomerProvinceDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 名单客户省份关系 Mapper 接口（用于写入master主库）
 * 使用@DS("master")注解确保操作MySQL主库
 *
 * @author xyj
 * @date 2026-05-13
 */
@Mapper
@DS("master")  // 指定主库数据源
public interface CustTRosterCustomerProvinceWriteMapper extends BaseMapperPlus<CustTRosterCustomerProvinceWriteMapper, CustTRosterCustomerProvinceDo, CustTRosterCustomerProvinceDo> {

    /**
     * 清空主库表数据
     */
    void truncateTable();

    /**
     * 批量插入数据到主库
     *
     * @param list 待插入的数据列表
     */
    void insertBatch(@Param("list") List<CustTRosterCustomerProvinceDo> list);

}
