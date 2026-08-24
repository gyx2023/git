package com.crm.system.service;

/**
 * 台账 用户信息查询（总部数字化能力开放平台，查入网时间）
 */
public interface IZqztLedgerUserInfoService {

    /**
     * 根据号码查询入网时间
     *
     * @param serialNumber 号码（宽带账号/摄像头账号）
     * @return 入网时间字符串（yyyy-MM-dd）；未查到/异常返回 null
     */
    String queryUserInDate(String serialNumber);
}
