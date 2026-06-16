package com.crm.system.domain.topp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商机统计结果对象 topp_oppotunity_statistics
 *
 * @author crm
 */
@Data
@TableName("topp_oppotunity_statistics")
//@TableName("topp_oppotunity_statistics")
public class ToppOppoStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;

    /**
     * 统计类型 1-储备商机 2-本月新增 3-签约商机 4-商机分类 5-签约按月
     */
    @TableField("type")
    private String type;

    /**
     * 统计数据(JSON格式)
     */
    @TableField("data")
    private String data;

    /**
     * 时间限制(月数)
     */
    @TableField("time_limite")
    private String timeLimite;

    /**
     * 统计日期
     */
    @TableField("statistics_date")
    private Date statisticsDate;

    /**
     * 区域类型 1-全省/全市 2-省本/市本/县分 其他-具体地市前缀
     */
    @TableField("area_type")
    private String areaType;
}
