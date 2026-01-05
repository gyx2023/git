package com.crm.system.utils;


public class OppClueCodeUtil {
    private static final String XS_SEQ_NAME = "XS";
    private static final String SJ_SEQ_NAME = "SJ";
    private static final String D_SEQ_NAME = "D";
    private static final String KSJ_SEQ_NAME = "KSJ";

    /**
     * 获取线索编码
     * @return 线索编码
     */
    public static String getClueCode() {
        return XS_SEQ_NAME;
    }

    /**
     * 获取商机编码
     * @return 商机编码
     */
    public static String getOppCode() {
        return SJ_SEQ_NAME;
    }

    /**
     * 获取订单编码
     * @return 订单编码
     */
    public static String getOrderCode() {
        return D_SEQ_NAME;
    }

    /**
     * 获取快速商机编码
     * @return 快速商机编码
     */
    public static String getKsjOppCode() {
        return KSJ_SEQ_NAME;
    }

}
