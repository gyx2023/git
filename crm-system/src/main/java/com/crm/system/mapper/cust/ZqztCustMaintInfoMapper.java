package com.crm.system.mapper.cust;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.cust.ZqztCustMaintInfo;
import com.crm.system.domain.vo.cust.ZqztCustMaintInfoVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2026-02-12
 */
public interface ZqztCustMaintInfoMapper extends BaseMapperPlus<ZqztCustMaintInfoMapper, ZqztCustMaintInfo, ZqztCustMaintInfoVo> {
    /**
     * 查询未签约用户列表
     *
     * @param hqGrpCode 总部集团编码
     * @param contractId 合同ID
     * @return 未签约用户列表
     */
    Page<ZqztCustMaintInfoVo> selectUncontractedUsers(Page<ZqztCustMaintInfoVo> page, @Param("hqGrpCode") String hqGrpCode, @Param("contractId") String contractId);

    List<ZqztCustMaintInfoVo> selectUncontractedUsersNoPage(@Param("hqGrpCode") String hqGrpCode, @Param("contractId") String contractId, List<String> svcNums);
}
