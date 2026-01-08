package com.crm.common.enums;


/**
 * 商机同步集团常量类
 * @author 优化后
 */
public class OppoSyncConstants {
    // 省分编码
    public static final String PROVINCE_CODE = "36";
    // 默认小数格式化（两位）
    public static final String DECIMAL_FORMAT_TWO = "0.00";
    // 默认整数格式化
    // 日期时间格式化（yyyyMMddHHmmss）
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    // HTTP请求超时时间（5秒）
    public static final int HTTP_CONNECT_TIMEOUT = 5000;
    // Content-Type头
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    // Accept头
    public static final String ACCEPT_JSON = "application/json";
    // Accept-Encoding头
    public static final String ACCEPT_ENCODING_UTF8 = "utf-8";



    // 商机状态同步编码
    public static final String OPP_STATUS_SAVE = "oppo_save";
    public static final String OPP_STATUS_EVALUATION = "oppo_evaluation";
    public static final String OPP_STATUS_SUPPORT_ORDER_ARRIVED = "oppo_support_officer_order_arrived";
    public static final String OPP_STATUS_SUPPORT_FIRST_DEAL = "oppo_support_officer_first_deal";
    public static final String OPP_STATUS_SUPPORT_COMPLETE = "oppo_support_complete";
    public static final String OPP_STATUS_SIGN = "oppo_sign";
    public static final String OPP_STATUS_ABANDON = "oppo_abandon";

    public static final String SIGN_TYPE_APPLICATION = "受理单";
    public static final String SIGN_TYPE_CONTRACT = "合同";
    public static final String CONTRACT_TYPE_PROJECT = "项目合同";
}
