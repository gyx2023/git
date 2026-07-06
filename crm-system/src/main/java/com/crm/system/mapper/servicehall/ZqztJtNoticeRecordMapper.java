package com.crm.system.mapper.servicehall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.jt.ZqztJtNoticeRecordVo;

import java.util.List;

/**
 * 集团任务前置通知接口记录表 Mapper接口
 *
 * @author crm
 * @date 2025-12-31
 */
public interface ZqztJtNoticeRecordMapper extends BaseMapperPlus<ZqztJtNoticeRecordMapper, ZqztJtNoticeRecordDo, ZqztJtNoticeRecordVo> {

    /**
     * 查询所有待重试的通知记录（deal_result_code='1'）
     * 限制返回数量，避免全表扫描
     */
    default List<ZqztJtNoticeRecordDo> selectPendingRetryRecords() {
        return selectList(new LambdaQueryWrapper<ZqztJtNoticeRecordDo>()
            .eq(ZqztJtNoticeRecordDo::getDealResultCode, "1")
            .last("LIMIT 500"));
    }

}
