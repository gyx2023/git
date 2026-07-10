package com.crm.system.domain.dao.zbzq;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VisitAppointmentDo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date visitTime;

    private String customerId;

    private String customName;

    private String userId;

    private String createUser;

    private String eparchyCode;

    private String appointmentId;

    private Date createTime;

    private String executeUserCode;
}
