//package com.crm.system.service;
//
//import com.crm.system.domain.dao.ZqztGrpCustInuseUserlist;
//import com.crm.system.domain.vo.ZqztGrpCustInuseUserlistVo;
//import com.crm.system.domain.bo.ZqztGrpCustInuseUserlistBo;
//import com.crm.common.core.page.TableDataInfo;
//import com.crm.common.core.domain.PageQuery;
//
//import java.util.Collection;
//import java.util.List;
//
///**
// * 政企实际使用客户类型用户清单Service接口
// *
// * @author crm
// * @date 2026-01-20
// */
//public interface IZqztGrpCustInuseUserlistService {
//
//    /**
//     * 查询政企实际使用客户类型用户清单
//     */
//    ZqztGrpCustInuseUserlistVo queryById(String inuseId);
//
//    /**
//     * 查询政企实际使用客户类型用户清单列表
//     */
//    TableDataInfo<ZqztGrpCustInuseUserlistVo> queryPageList(ZqztGrpCustInuseUserlistBo bo, PageQuery pageQuery);
//
//    /**
//     * 查询政企实际使用客户类型用户清单列表
//     */
//    List<ZqztGrpCustInuseUserlistVo> queryList(ZqztGrpCustInuseUserlistBo bo);
//
//    /**
//     * 新增政企实际使用客户类型用户清单
//     */
//    Boolean insertByBo(ZqztGrpCustInuseUserlistBo bo);
//
//    /**
//     * 修改政企实际使用客户类型用户清单
//     */
//    Boolean updateByBo(ZqztGrpCustInuseUserlistBo bo);
//
//    /**
//     * 校验并批量删除政企实际使用客户类型用户清单信息
//     */
//    Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid);
//}
