package com.crm.common.constant;


/**
 * 商机同步集团常量类
 * @author 优化后
 */
public interface OppoSyncConstants {
    // 省分编码
    String PROVINCE_CODE = "36";
    // 默认小数格式化（两位）
    String DECIMAL_FORMAT_TWO = "0.00";
    // 默认整数格式化
    // 日期时间格式化（yyyyMMddHHmmss）
    String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    // HTTP请求超时时间（5秒）
    int HTTP_CONNECT_TIMEOUT = 5000;
    // Content-Type头
    String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    // Accept头
    String ACCEPT_JSON = "application/json";
    // Accept-Encoding头
    String ACCEPT_ENCODING_UTF8 = "utf-8";



    // 商机状态同步编码
    String OPP_STATUS_SAVE = "oppo_save";
    String OPP_STATUS_EVALUATION = "oppo_evaluation";
    String OPP_STATUS_SUPPORT_ORDER_ARRIVED = "oppo_support_officer_order_arrived";
    String OPP_STATUS_SUPPORT_FIRST_DEAL = "oppo_support_officer_first_deal";
    String OPP_STATUS_SUPPORT_COMPLETE = "oppo_support_complete";
    String OPP_STATUS_SIGN = "oppo_sign";
    String OPP_STATUS_ABANDON = "oppo_abandon";

    String SIGN_TYPE_APPLICATION = "受理单";
    String SIGN_TYPE_CONTRACT = "合同";
    String CONTRACT_TYPE_PROJECT = "项目合同";
}
