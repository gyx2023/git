package com.crm.system.domain.dao.zbzq;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VisitRecordDo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String prov;
    private String city;
    private String district;
    private String natureCustName;
    private String natureCustId;
    private String userName;
    private String oaAccount;
    private String phone;
    private String visitTitle;
    private String visitType;
    private Date visitTime;
    private String isInsertDiff;
    private String intervieweeName;
    private String intervieweeJob;
    private String interviewContent;
    private String visitSummary;
    private String businessCode;
    private String businessName;
    private String importProduct;
    private String isSupport;
    private String fromSource;
}
