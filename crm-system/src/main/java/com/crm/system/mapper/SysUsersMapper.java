package com.crm.system.mapper;

import com.crm.system.domain.dao.SysUsers;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.SysUsersVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户(客户的员工)Mapper接口
 *
 * @author crm
 * @date 2025-12-23
 */
@Mapper
public interface SysUsersMapper extends BaseMapperPlus<SysUsersMapper, SysUsers, SysUsersVo> {

    /**
     * 超时未登录用户
     *
     * @param failureDays 超时时长
     * @return 超时未登录用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
        "LEFT JOIN (SELECT user_id, MAX(login_time) AS login_time FROM sys_login_log GROUP BY user_id) l ON u.id_ = l.user_id " +
        "WHERE u.is_valid = '1' AND NOT EXISTS (SELECT 1 FROM sys_user_not_failure n WHERE u.id_ = n.user_id) " +
        "  AND (l.login_time <= DATE_SUB(NOW(), INTERVAL #{failureDays} DAY) OR l.login_time IS NULL) " +
        "  AND u.create_time <= DATE_SUB(NOW(), INTERVAL #{failureDays} DAY) ")
    List<SysUsers> selectFailureList(@Param("failureDays") Integer failureDays);

    /**
     * 查询长期未登录用户（按老框架逻辑：LEFT JOIN ON条件中过滤近期登录，WHERE中判断无登录记录）
     *
     * @param failureDays 超时时长
     * @return 长期未登录用户列表
     */
    @Select("SELECT a.* FROM sys_user a " +
        "LEFT JOIN sys_login_log b ON a.id_ = b.user_id " +
        "  AND b.login_time > DATE_SUB(CURDATE(), INTERVAL #{failureDays} DAY) " +
        "WHERE a.oa_account IS NOT NULL AND a.oa_account != '' AND b.user_id IS NULL")
    List<SysUsers> selectLongTimeNoLoginByOa(@Param("failureDays") Integer failureDays);

    /**
     * 根据渠道总部编码查询单个用户
     * @param channelHqCode 渠道总部编码
     * @return 单个用户对象，未查询到则返回 null
     */
    @Select("SELECT a.* FROM sys_user a " +
        "INNER JOIN sys_developer_income_day b ON a.oa_account = b.user_name " +
        "WHERE a.is_valid = '1' " +
        "AND b.user_name != '' " +
        "AND b.up_channel_hq_code = #{channelHqCode} " +
        "LIMIT 1")  // 只查1条数据
    SysUsers selectUserByChannelHqCode(@Param("channelHqCode") String channelHqCode);

    /**
     * 根据 OA 账号查询用户（大小写不敏感，对齐旧系统 SysUserService.findByOaId）
     */
    @Select("SELECT * FROM sys_user WHERE LOWER(oa_account) = LOWER(#{oaAccount}) AND is_valid = '1' LIMIT 1")
    SysUsers findByOaAccount(@Param("oaAccount") String oaAccount);

    /**
     * 按 OA 账号查询用户信息（返回 Map，供预警策略使用）
     * 大小写不敏感，对齐旧系统 SysUserService.findByOaId
     * @param oaId OA 账号
     * @return user_id, dept_id, nick_name, phonenumber
     */
    @Select("SELECT id_ as user_id, org_id as dept_id, name_ch as nick_name, relation_tel as phonenumber "
            + "FROM sys_user WHERE LOWER(oa_account) = LOWER(#{oaId}) AND is_valid = '1' LIMIT 1")
    Map<String, Object> selectUserByOaId(@Param("oaId") String oaId);

    /**
     * 按区域范围分页查询有效客户经理（cust_manager），支持OA账号/姓名模糊搜索。
     * 对齐旧系统 SysPositionService.findCustManagerForMNP 逻辑。
     * @param areaIds 区域ID列表，为null或空时不限制区域
     */
    List<SysUsers> selectByAreaScope(@Param("areaIds") List<Long> areaIds, @Param("keyword") String keyword,
                                     @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 按区域范围统计有效客户经理数
     */
    long countByAreaScope(@Param("areaIds") List<Long> areaIds, @Param("keyword") String keyword);

}
