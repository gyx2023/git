package com.crm.job.mapper.warning;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息发送记录 Mapper（表 message_record）
 * <p>
 * 迁移自旧系统 {@code com.hollycrm.service.send.MessageRecordService} 的
 * {@code findByRecord} / {@code saveRecord}，用于营销线索预警的重复发送校验：
 * 同一个人、同一条线索（或线索编号）、同一种预警类型只提醒一次。
 *
 * @author crm
 */
@Mapper
public interface MessageRecordMapper {

    /**
     * 查询是否已发送过
     *
     * @param userId  接收人ID（sys_user.id_）
     * @param otherId 业务标识（旧系统存线索编号 o.number_ 或线索订单ID）
     * @param type    预警类型
     */
    @Select("select count(1) from message_record where user_id = #{userId} and other_id = #{otherId} and type_ = #{type}")
    int countByRecord(@Param("userId") Long userId, @Param("otherId") String otherId, @Param("type") String type);

    /**
     * 保存发送记录
     */
    @Insert("insert into message_record(user_id, other_id, type_, create_time) "
            + "values(#{userId}, #{otherId}, #{type}, now())")
    int insertRecord(@Param("userId") Long userId, @Param("otherId") String otherId, @Param("type") String type);
}
