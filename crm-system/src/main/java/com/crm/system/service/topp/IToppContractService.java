package com.crm.system.service.topp;

import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface IToppContractService {

    /**
     * 查询【请填写功能名称】
     */
    ToppContractVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppContractVo> queryPageList(ToppContractBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppContractVo> queryList(ToppContractBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppContractBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppContractBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<ToppContractVo> queryContractList(List<Long> ids);

    List<ToppContractVo>  findContractDetailById(Long oppoId);

    /**
     * 根据商机ID列表批量查询合同信息 - 优化版
     * 替代N+1查询问题
     * @param opportunityIds 商机ID列表
     * @return 合同信息列表
     */
    List<ToppContractVo> selectByOpportunityIds(List<Long> opportunityIds);

    /**
     * 校验合同金额（对照老系统 ContractService.toValidContract）
     * @param bo 合同信息
     * @param includeTypeMoneyTotal 分项收入总和
     * @return 校验结果，key=是否通过，value=错误信息
     */
    Map<Boolean, String> toValidContract(ToppContractBo bo, String includeTypeMoneyTotal);

    /**
     * 检查合同编号是否唯一（对照老系统 ContractAction.validbind）
     * @param contractNo 合同编号
     * @param oppoId 商机ID（用于排除自身）
     * @return status: 1=当前商机已关联该合同, 2=其他商机已关联该合同, 3=合同编号可用
     */
    Map<String, Object> validContractNo(String contractNo, Long oppoId);
}
