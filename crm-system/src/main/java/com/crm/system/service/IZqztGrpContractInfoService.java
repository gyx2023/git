package com.crm.system.service;

import com.crm.system.domain.ZqztGrpContractInfo;
import com.crm.system.domain.vo.ZqztGrpContractInfoVo;
import com.crm.system.domain.bo.ZqztGrpContractInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 集团合同信息Service接口
 *
 * @author 万超凡
 * @date 2026-01-15
 */
public interface IZqztGrpContractInfoService {

    /**
     * 查询集团合同信息
     */
    ZqztGrpContractInfoVo queryById(String contractId);

    /**
     * 查询集团合同信息列表
     */
    TableDataInfo<ZqztGrpContractInfoVo> queryPageList(ZqztGrpContractInfoBo bo, PageQuery pageQuery);

    /**
     * 查询集团合同信息列表
     */
    List<ZqztGrpContractInfoVo> queryList(ZqztGrpContractInfoBo bo);

    /**
     * 新增集团合同信息
     */
    Boolean insertByBo(ZqztGrpContractInfoBo bo);

    /**
     * 修改集团合同信息
     */
    Boolean updateByBo(ZqztGrpContractInfoBo bo);

    /**
     * 校验并批量删除集团合同信息信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);

    /**
     * 全量导入集团合同信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    int importAllGroupContractInfo(List<ZqztGrpContractInfo> dataList);

    /**
     * 查询所有集团合同信息
     *
     * @return 集团合同信息列表
     */
    List<ZqztGrpContractInfo> selectAllZqztGrpContractInfo();

    /**
     * 判断合同状态是否是履行中9或办结5
     *
     * @param contractId 合同ID
     * @return 是否为履行中或办结合同
     */
    Boolean isContractInExecutionOrCompleted(String contractId);

    /**
     * 判断合同失效时间是否小于当前时间
     *
     * @param contractId 合同ID
     * @return 合同是否已失效
     */
    Boolean isContractExpired(String contractId);
}
