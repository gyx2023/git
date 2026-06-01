package com.crm.common.constant;

/**
 * Redis Key常量类
 */
public interface RedisKeyConstants {
    /**
     * 锁持有最大时间（秒）
     */
    int MAX_LOCK_TIME = 3600;
    /**
     * 日任务锁持有最大时间（秒）
     */
    int DAY_MAX_LOCK_TIME = 24 * 3600;

    String PREFIX = "zqzt:backend:";


    default String format(String redisKey, Object... params) {
        return String.format(redisKey, params);
    }

    /**
     * 办单申请补偿mq定时任务
     */
    String EXECUTE_IDENTIFY_SCORE_TASK = PREFIX + "identify:score";

    String CUST_TROSTER_NATURE_RELATION_TASK= PREFIX + "cust:roster:nature:relation:task";

    String WZ_TOPP_OPPORTUNITY_PUT_TASK = PREFIX + "wz:topp:opportunity:put:task";
}
