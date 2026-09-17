package com.crm.system.mapper.rpa;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.crm.system.domain.dao.rpa.RpaCommonNoticeLogDo;
import com.crm.system.domain.dao.rpa.RpaSmsPendingPushDo;
import com.crm.system.domain.dao.rpa.RpaSmsPendingPushParamDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RPA 预警短信 Mapper。
 */
public interface RpaWarningSmsMapper {

    /**
     * 调用存储过程，生成短信待推送记录及模板参数。
     */
    @InterceptorIgnore(dataPermission = "true")
    void callPreSendRecord();

    /**
     * 查询并锁定所有待处理记录。
     */
    List<Long> selectUnprocessedIdsForUpdate();

    /**
     * 将选中的待处理记录标记为处理中。
     */
    int updateToProcessing(@Param("ids") List<Long> ids);

    RpaSmsPendingPushDo selectPendingPushById(@Param("id") Long id);

    List<RpaSmsPendingPushParamDo> selectParamsByMsgUnionid(@Param("msgUnionid") String msgUnionid);

    int updatePushResult(@Param("id") Long id,
                         @Param("pushStatus") String pushStatus,
                         @Param("pushResultRemark") String pushResultRemark);

    int insertCommonNoticeLog(RpaCommonNoticeLogDo noticeLog);

    int resetTimeoutLockedSmsData();
}
