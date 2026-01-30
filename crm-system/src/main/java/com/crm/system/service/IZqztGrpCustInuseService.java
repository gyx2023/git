package com.crm.system.service;

import com.crm.common.core.domain.R;
import com.crm.common.core.domain.dto.ZqztGrpCustAddUserListReqDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseRespDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustInuseUserListRespDTO;
import com.crm.common.core.domain.dto.ZqztGrpCustReqDTO;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.domain.vo.ZqztGrpCustInuseVo;
import com.crm.system.domain.bo.ZqztGrpCustInuseBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 政企主体-实际使用客户（zqzt_grp_cust_inuse）Service接口
 *
 * @author crm
 * @date 2026-01-20
 */
public interface IZqztGrpCustInuseService {

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    ZqztGrpCustInuseVo queryById(String inuseId);

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    TableDataInfo<ZqztGrpCustInuseVo> queryPageList(ZqztGrpCustInuseBo bo, PageQuery pageQuery);

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    List<ZqztGrpCustInuseVo> queryList(ZqztGrpCustInuseBo bo);

    /**
     * 新增政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    Boolean insertByBo(ZqztGrpCustInuseBo bo);

    /**
     * 修改政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    Boolean updateByBo(ZqztGrpCustInuseBo bo);

    /**
     * 校验并批量删除政企主体-实际使用客户（zqzt_grp_cust_inuse）信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
    /**
     * 查询合同和关联客户ID列表
     */
    TableDataInfo<ZqztGrpCustInuseRespDTO> queryPageList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery);

    /**
     * 校验并批量删除合同和关联客户ID信息
     */
    Boolean deleteWithValidByIds(String[] ids, Boolean isValid);

    /**
     * 查询关联客户用户列表
     */
    TableDataInfo<ZqztGrpCustInuseUserListRespDTO> queryPageUserList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery);

    /**
     * 删除关联客户用户
     */
    int deleteUserListWithValidByIds(String[] subscrbids, Boolean isValid);

    /**
     * 查询关联客户列表
     */
    TableDataInfo<ZqztGrpCustInuseRespDTO> queryPageGrpList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery);

    /**
     * 更新关联客户
     */
    int addInuse(ZqztGrpCustReqDTO reqDTO);

    /**
     * 查询待添加关联客户用户列表
     */
    TableDataInfo<ZqztGrpCustInuseUserListRespDTO> getAddUserList(ZqztGrpCustReqDTO reqDTO, PageQuery pageQuery);

    /**
     * 添加关联客户用户
     */
    int addUserList(ZqztGrpCustAddUserListReqDTO reqDTO);

}
