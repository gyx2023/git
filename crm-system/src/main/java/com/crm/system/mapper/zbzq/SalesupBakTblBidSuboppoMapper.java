package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.zbzq.SalesupBakTblBidSuboppoVo;
import com.crm.system.domain.zbzq.SalesupBakTblBidSuboppoDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("slave")
public interface SalesupBakTblBidSuboppoMapper extends BaseMapperPlus<SalesupBakTblBidSuboppoMapper, SalesupBakTblBidSuboppoDo, SalesupBakTblBidSuboppoVo> {

    /**
     * 查询已签约商机ID列表
     * @param ids 商机ID列表
     * @return 包含商机ID的VO列表（或直接返回List<String>/List<Long>，按需选择）
     */
    @Select("SELECT a.oppo_id " +
        "FROM salesup_bak_tbl_bid_suboppo a " +
        "LEFT JOIN salesup_bak_tbl_bid_task b ON a.info_id = b.info_id " +
        "WHERE a.status = 1 " +
        "  AND a.deleted = 0 " +
        "  AND a.oppo_id IN (#{ids})") // 批量参数正确写法
    List<Long> selectSignedOppoData(@Param("ids") List<Long> ids);
}
