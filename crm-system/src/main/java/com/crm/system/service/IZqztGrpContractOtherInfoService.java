package com.crm.system.service;

import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 集团合同扩展信息Service接口
 *
 * @author 万超凡
 * @date 2026-01-13
 */
public interface IZqztGrpContractOtherInfoService {

    /**
     * 查询集团合同扩展信息
     */
    ZqztGrpContractOtherInfoVo queryById(String contractId);

    /**
     * 查询集团合同扩展信息列表
     */
    TableDataInfo<ZqztGrpContractOtherInfoVo> queryPageList(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery);

    /**
     * 查询集团合同扩展信息列表
     */
    List<ZqztGrpContractOtherInfoVo> queryList(ZqztGrpContractOtherInfoBo bo);

    /**
     * 新增集团合同扩展信息
     */
    Boolean insertByBo(ZqztGrpContractOtherInfoBo bo);

    /**
     * 修改集团合同扩展信息
     */
    Boolean updateByBo(ZqztGrpContractOtherInfoBo bo);

    /**
     * 校验并批量删除集团合同扩展信息信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);

    /**
     * 全量导入集团合同扩展信息数据（包含业务逻辑处理）
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    int importAllGroupContractOtherInfoWithBusinessLogic(List<ZqztGrpContractOtherInfo> dataList);

    /**
     * 全量导入集团合同扩展信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    int importAllGroupContractOtherInfo(List<ZqztGrpContractOtherInfo> dataList);

    /**
     * 查询所有集团合同扩展信息
     *
     * @return 集团合同扩展信息列表
     */
    List<ZqztGrpContractOtherInfo> selectAllZqztGrpContractOtherInfo();

    /**
     * 根据合同ID查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @return 集团合同扩展信息列表
     */
    List<ZqztGrpContractOtherInfoVo> selectByContractId(String contractId);

    /**
     * 根据合同ID和服务号码查询集团合同扩展信息列表
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码
     * @return 集团合同扩展信息列表
     */
    List<ZqztGrpContractOtherInfoVo> selectByContractIdAndInfoVal2(String contractId, String infoVal2);

    /**
     * 根据合同ID查询合同关联的客户信息，将合同扩展信息与客户维护信息关联
     *
     * @param contractId 合同ID
     * @return 关联的客户信息列表
     */
    List<Map<String, Object>> selectContractRelatedUserInfo(String contractId);

    /**
     * 根据合同ID和服务号码查询合同关联的客户信息
     *
     * @param contractId 合同ID
     * @param infoVal2 服务号码
     * @return 关联的客户信息列表
     */
    List<Map<String, Object>> selectContractRelatedUserInfoByContractIdAndInfoVal2(String contractId, String infoVal2);

    /**
     * 批量使合同关联用户信息失效
     *
     * @param ids 合同关联信息ID列表
     * @return 操作结果
     */
    Boolean batchInvalidateContractUserRelation(List<Long> ids);
}
