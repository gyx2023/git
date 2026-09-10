package com.crm.system.utils;

import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;

import java.util.Date;

/**
 * 台账工单 OB 源表两行数据合并工具
 * 合并规则：同一工单只落一张表（sl08md 或 04049f），字段均按表1优先择一取值；
 * 宽带账号在此基础上按"利旧口径优先于新装口径"择一，并据此带出是否利旧
 */
public final class ZqztLedgerObMergeUtil {

    /** 宽带是否利旧：利旧 */
    private static final String REUSE_OLD = "OLD";
    /** 宽带是否利旧：新装 */
    private static final String REUSE_NEW = "NEW";

    private ZqztLedgerObMergeUtil() {
    }

    public static ZqztLedgerInfoVo merge(ZqztOrderInfoObVo t1, ZqztOrderInfoObVo t2) {
        ZqztLedgerInfoVo vo = new ZqztLedgerInfoVo();
        ZqztOrderInfoObVo r1 = safe(t1);
        ZqztOrderInfoObVo r2 = safe(t2);
        vo.setCompanyName(firstNonBlank(r1.getCompanyName(), r2.getCompanyName()));
        vo.setCreditCode(firstNonBlank(r1.getCreditCode(), r2.getCreditCode()));
        vo.setRegAddress(firstNonBlank(r1.getRegAddress(), r2.getRegAddress()));
        vo.setActualAddress(firstNonBlank(r1.getActualAddress(), r2.getActualAddress()));
        vo.setCompanyPrincipal(firstNonBlank(r1.getContactName(), r2.getContactName()));
        vo.setPrincipalPhone(firstNonBlank(r1.getContactPhone(), r2.getContactPhone()));
        vo.setSafetyOfficer1(firstNonBlank(r1.getSafetyOfficerName1(), r2.getSafetyOfficerName2()));
        vo.setSafetyOfficer1Phone(firstNonBlank(r1.getSafetyOfficerPhone1(), r2.getSafetyOfficerPhone2()));
        vo.setUnicomManager(firstNonBlank(r1.getDevelopName(), r2.getDevelopName()));
        vo.setUnicomManagerPhone(firstNonBlank(r1.getDevelopPhone(), r2.getDevelopPhone()));
        vo.setSignRoutes(firstNonBlankInt(r1.getSignRoutes1(), r2.getSignRoutes2()));
        Date orderCreateTime = r1.getOrderCreateTime() != null ? r1.getOrderCreateTime() : r2.getOrderCreateTime();
        vo.setOrderCreateTime(orderCreateTime);
        vo.setCloudVisionPackageId(firstNonBlank(r1.getCloudVisionPackageId1(), r2.getCloudVisionPackageId2()));
        mergeBroadband(vo, r1, r2);
        vo.setCloudAccount(firstNonBlank(r1.getCloudAccount1(), r2.getCloudAccount2()));
        vo.setCameraAccount(firstNonBlank(r1.getCameraAccount1(), r2.getCameraAccount2()));
        return vo;
    }

    /**
     * 宽带账号与是否利旧取值：
     * 1. 利旧口径(textField_ahdM4O0A)有值 → 默认利旧，账号取该值
     * 2. 否则新装口径(sl08md.textField_90QNngmC 或 textField_YkrgCq0G / 04049f.textField_GYjOc1Vy 或 textField_IIPTA5sx)有值 → 默认新装，账号取该值
     * 3. 两者均无值 → 是否利旧与账号均留空，由用户手工填写
     */
    private static void mergeBroadband(ZqztLedgerInfoVo vo, ZqztOrderInfoObVo r1, ZqztOrderInfoObVo r2) {
        String reuseAccount = firstNonBlank(r1.getBroadbandAccountOld1(), r2.getBroadbandAccountOld2());
        if (isNotBlank(reuseAccount)) {
            vo.setBroadbandIsReuse(REUSE_OLD);
            vo.setBroadbandAccount(reuseAccount.trim());
            return;
        }
        String newAccount = firstNonBlank(r1.getBroadbandAccountNew1(), r2.getBroadbandAccountNew2());
        if (isNotBlank(newAccount)) {
            vo.setBroadbandIsReuse(REUSE_NEW);
            vo.setBroadbandAccount(newAccount.trim());
        }
    }

    private static ZqztOrderInfoObVo safe(ZqztOrderInfoObVo row) {
        return row == null ? new ZqztOrderInfoObVo() : row;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }

    private static Integer firstNonBlankInt(String a, String b) {
        String v = firstNonBlank(a, b);
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException ignored) {
            // 非法数字视为无值
            return null;
        }
    }
}
