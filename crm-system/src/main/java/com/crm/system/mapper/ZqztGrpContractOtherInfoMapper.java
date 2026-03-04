package com.crm.system.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;
/**
 * 集团合同扩展信息Mapper接口
 *
 * @author 万超凡
 * @date 2026-01-13
 */
public interface ZqztGrpContractOtherInfoMapper extends BaseMapperPlus<ZqztGrpContractOtherInfoMapper, ZqztGrpContractOtherInfo, ZqztGrpContractOtherInfoVo> {

    /**
     * 分页查询合同关联用户信息（LEFT JOIN方式）
     *
     * @param page 分页对象
     * @param contractId 合同ID
     * @param infoVal2 服务号码（可选）
     * @return 分页结果
     */
    Page<ZqztGrpContractOtherInfoVo> selectContractRelatedUserInfoPage(Page<ZqztGrpContractOtherInfoVo> page,
                                                                       @Param("contractId") String contractId,
                                                                       @Param("infoVal2") String infoVal2);

}
