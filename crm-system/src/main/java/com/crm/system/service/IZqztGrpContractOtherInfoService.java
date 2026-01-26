package com.crm.system.service;

import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

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
}
