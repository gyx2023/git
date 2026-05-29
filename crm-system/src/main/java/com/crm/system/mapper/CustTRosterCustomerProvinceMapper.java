package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.CustTRosterCustomerProvinceDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 名单客户省份关系 Mapper 接口
 * 使用@DS("slave_1")注解确保查询MySQL从库
 *
 * @author xyj
 * @date 2026-04-30
 */
@Mapper
public interface CustTRosterCustomerProvinceMapper extends BaseMapperPlus<CustTRosterCustomerProvinceMapper, CustTRosterCustomerProvinceDo, CustTRosterCustomerProvinceDo> {

    /**
     * 查询名单客户省份关系列表
     *
     * @return 名单客户省份关系列表
     */
    List<CustTRosterCustomerProvinceDo> getCustTCustomerList();

    /**
     * 清空主库表数据
     */
    void truncateTable();

}
