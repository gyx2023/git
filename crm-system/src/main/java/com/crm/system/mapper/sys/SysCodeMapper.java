package com.crm.system.mapper.sys;

import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一编码Mapper接口
 *
 * @author crm
 * @date 2025-12-15
 */
@Mapper
public interface SysCodeMapper extends BaseMapperPlus<SysCodeMapper, SysCode, SysCodeVo> {
    /**
     * 根据类型删除统一编码
     * @param type 类型
     * @return 影响行数
     */
    int deleteByType(@Param("type") String type);

    /**
     * 批量插入 cust_sale_product 类型的编码数据
     * @return 插入的行数
     */
    int insertCustSaleProduct();
}
