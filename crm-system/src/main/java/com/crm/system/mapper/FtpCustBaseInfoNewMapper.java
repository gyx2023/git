package com.crm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.system.domain.FtpCustBaseInfoNew;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * FTP客户基础信息新表Mapper接口
 */
@Mapper
public interface FtpCustBaseInfoNewMapper extends BaseMapper<FtpCustBaseInfoNew> {

    FtpCustBaseInfoNew selectByCondition1(@Param("grpName") String grpName,
                                          @Param("areaId") String areaId,
                                          @Param("countyId") String countyId,
                                          @Param("grpCode") String grpCode);

    FtpCustBaseInfoNew selectByCondition2(@Param("grpName") String grpName,
                                          @Param("areaId") String areaId,
                                          @Param("countyId") String countyId,
                                          @Param("custType") String custType,
                                          @Param("grpCode") String grpCode,
                                          @Param("grpDeptType") String grpDeptType);
}
