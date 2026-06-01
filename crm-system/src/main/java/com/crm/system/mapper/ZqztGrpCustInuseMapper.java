package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.ZqztGrpCustInuseUserListRespDTO;
import com.crm.system.domain.dto.ZqztGrpCustReqDTO;
import com.crm.system.domain.dto.ZqztGrpCustInuseRespDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.domain.vo.ZqztGrpCustInuseVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 合同和关联客户IDMapper接口
 *
 * @author crm
 * @date 2026-01-19
 */
public interface ZqztGrpCustInuseMapper extends BaseMapperPlus<ZqztGrpCustInuseMapper, ZqztGrpCustInuse, ZqztGrpCustInuseVo> {

    List<ZqztGrpCustInuseRespDTO> queryZqztGrpCustInuseList(ZqztGrpCustReqDTO reqDTO);

    Boolean updateUserByInuseId(@Param("ids") String[] ids, @Param("loginUser") Map<String,String> loginUser);

    List<ZqztGrpCustInuseUserListRespDTO> queryPageUserList(ZqztGrpCustReqDTO reqDTO);

    int updateBySubscrbid(@Param("subscrbids") String[] subscrbids, @Param("loginUser") Map<String,String> loginUser);

    List<ZqztGrpCustInuseRespDTO> queryPageGrpList(ZqztGrpCustReqDTO reqDTO);

    int addInuse(@Param("reqDTO") ZqztGrpCustReqDTO reqDTO, @Param("loginUser") Map<String,String> loginUser);

    List<String> queryUserIdArray(ZqztGrpCustReqDTO reqDTO);

    @DS("slave_2")
    List<ZqztGrpCustInuseUserListRespDTO> queryPageAddUserList(@Param("inuseId") String inuseId, @Param("custId") String custId, @Param("userIds") List<String> userIds);

    String getCustId(ZqztGrpCustReqDTO reqDTO);

    @DS("slave_2")
    List<ZqztGrpCustInuseUserListRespDTO> queryPageAddUserData(@Param("inuseId") String inuseId, @Param("addInuseUserList") List<Map<String, String>> addInuseUserList);

    int addUserList(@Param("userList") List<ZqztGrpCustInuseUserListRespDTO> userList, @Param("loginUser") Map<String,String> loginUser);

    int queryZqztGrpCustInuseListTotal(ZqztGrpCustReqDTO reqDTO);

    int queryZqztGrpGroupInfoCount(ZqztGrpCustReqDTO reqDTO);

    int insertInuse(@Param("reqDTO") ZqztGrpCustReqDTO reqDTO, @Param("loginUser") Map<String, String> loginUser);

    Boolean updateUserlistByInuseId(@Param("ids") String[] ids, @Param("loginUser") Map<String,String> loginUser);
}
