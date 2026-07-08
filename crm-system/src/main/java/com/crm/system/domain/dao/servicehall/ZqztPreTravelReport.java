package com.crm.system.domain.dao.servicehall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("zqzt_pre_travel_report")
public class ZqztPreTravelReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String reportId;

    private String customerId;

    private String customerName;

    private String creator;

    private String oaAccount;

    private String taskStatus;

    private String taskStatusDesc;

    private String apiRequest;

    private String apiResponse;

    private String mdContent;

    private String pdfPath;

    private Long orderId;

    private Integer retryCount;

    private Date createTime;

    private Date updateTime;

    private String remark;

    private Long appointmentId;

    private String dataSource;
}
