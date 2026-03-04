package com.crm.system.service.cust;

import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.cust.ZqztCustMaintInfo;
import com.crm.system.domain.vo.cust.ZqztCustMaintInfoVo;
import com.crm.system.domain.bo.cust.ZqztCustMaintInfoBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2026-02-12
 */
public interface IZqztCustMaintInfoService {

    /**
     * 查询【请填写功能名称】
     */
    ZqztCustMaintInfoVo queryById(String statCycle);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ZqztCustMaintInfoVo> queryPageList(ZqztCustMaintInfoBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ZqztCustMaintInfoVo> queryList(ZqztCustMaintInfoBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ZqztCustMaintInfoBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ZqztCustMaintInfoBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);

    /**
     * 批量插入数据到 zqzt_cust_main_info 表
     * @param dataList 待插入的数据列表
     */
    boolean batchInsert(List<CustMaintInfo> dataList);

    List<ZqztCustMaintInfo> selectExistingUserIds(List<String> userIds);

    TableDataInfo<ZqztCustMaintInfoVo> selectUncontractedUsers(String hqGrpCode, String contractId, PageQuery pageQuery);

    List<ZqztCustMaintInfoVo> selectUncontractedUsersNoPage(String hqGrpCode, String contractId, List<String> svcNums);

    /**
     * 根据 user_id 批量删除客户维护信息
     * @param userIds user_id 列表
     * @return 删除是否成功
     */
    Boolean deleteByUserIds(List<String> userIds);

}
