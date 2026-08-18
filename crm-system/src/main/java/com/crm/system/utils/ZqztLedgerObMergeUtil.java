package com.crm.system.utils;

import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.domain.vo.ZqztOrderInfoObVo;

import java.util.Date;

/**
 * 台账工单 OB 源表两行数据合并工具
 * 合并规则：字段任一非空优先 t1；签约路数/通道数求和；宽带/云智眼账号/摄像头账号拼接
 */
public final class ZqztLedgerObMergeUtil {

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
        vo.setUnicomManager(firstNonBlank(r1.getDevelopName(), r2.getDevelopName()));
        vo.setUnicomManagerPhone(firstNonBlank(r1.getDevelopPhone(), r2.getDevelopPhone()));
        Integer routes = sumInt(r1.getSignRoutes1(), r2.getSignRoutes2());
        vo.setSignRoutes(routes);
        Date orderCreateTime = r1.getOrderCreateTime() != null ? r1.getOrderCreateTime() : r2.getOrderCreateTime();
        vo.setOrderCreateTime(orderCreateTime);
        vo.setCloudVisionPackageId(firstNonBlank(r1.getCloudVisionPackageId1(), r2.getCloudVisionPackageId2()));
        vo.setBroadbandAccount(concat(r1.getBroadbandAccount1(), r2.getBroadbandAccount2()));
        vo.setCloudAccount(concat(r1.getCloudAccount1(), r2.getCloudAccount2()));
        vo.setCameraAccount(concat(r1.getCameraAccount1(), r2.getCameraAccount2()));
        return vo;
    }

    private static ZqztOrderInfoObVo safe(ZqztOrderInfoObVo row) {
        return row == null ? new ZqztOrderInfoObVo() : row;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b;
    }

    private static String concat(String a, String b) {
        StringBuilder sb = new StringBuilder();
        if (a != null && !a.trim().isEmpty()) {
            sb.append(a.trim());
        }
        if (b != null && !b.trim().isEmpty()) {
            sb.append(b.trim());
        }
        return sb.toString();
    }

    private static Integer sumInt(String a, String b) {
        int sum = 0;
        if (a != null && !a.trim().isEmpty()) {
            try {
                sum += Integer.parseInt(a.trim());
            } catch (NumberFormatException ignored) {
                // 忽略非法数字
            }
        }
        if (b != null && !b.trim().isEmpty()) {
            try {
                sum += Integer.parseInt(b.trim());
            } catch (NumberFormatException ignored) {
                // 忽略非法数字
            }
        }
        return sum;
    }
}
