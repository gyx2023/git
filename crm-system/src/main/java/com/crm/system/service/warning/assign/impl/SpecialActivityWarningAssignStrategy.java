package com.crm.system.service.warning.assign.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.SpecialActivityWarningAssignMapper;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.warning.assign.IWarningAssignStrategy;
import com.crm.system.service.warning.assign.WarningAssignContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 特殊活动到期预警未派发指派策略。
 * <p>
 * 手动指派时按 maintenance_detail.detail_id 反查 ftp_tc_user_common_warning（warning_type=000032），
 * 使用前端指定的客户经理覆盖原 oa_id_dev，并组装建单上下文。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpecialActivityWarningAssignStrategy implements IWarningAssignStrategy {

    private final SpecialActivityWarningAssignMapper assignMapper;
    private final SysUsersMapper sysUsersMapper;
    private final TcommCustomerMapper tcommCustomerMapper;
    private final FtpCustBaseInfoMapper ftpCustBaseInfoMapper;

    @Override
    public String getOrderType() {
        return "special_activity_warn";
    }

    @Override
    public String getOrderTypeName() {
        return "特殊活动到期预警";
    }

    @Override
    public WarningAssignContext buildContext(Long detailId, Long targetUserId) {
        if (detailId == null || targetUserId == null) {
            return null;
        }
        Map<String, Object> row = assignMapper.selectSpecialActivityById(detailId);
        if (row == null || row.isEmpty()) {
            log.warn("【特殊活动指派】未找到原始预警行, detailId={}", detailId);
            return null;
        }

        SysUsers targetUser = sysUsersMapper.selectById(targetUserId);
        if (targetUser == null) {
            log.warn("【特殊活动指派】目标客户经理不存在, userId={}", targetUserId);
            return null;
        }

        String grpName = str(row.get("grp_name"));
        String grpCode = str(row.get("grp_code"));
        String custGrpId = str(row.get("cust_grp_id"));

        WarningAssignContext ctx = new WarningAssignContext();
        ctx.setOrderType(getOrderType());
        ctx.setOrderName(DateUtils.dateTime() + "_" + (StringUtils.isNotBlank(grpName) ? grpName + "_" : "") + "特殊活动到期预警");
        ctx.setCustName(grpName);
        ctx.setCustManagerId(targetUserId);
        ctx.setSysUser(targetUser);

        // 客户ID：按 grp_code 查 tcomm_customer
        if (StringUtils.isNotBlank(grpCode) && tcommCustomerMapper != null) {
            TcommCustomer customer = tcommCustomerMapper.selectOne(
                    new LambdaQueryWrapper<TcommCustomer>().eq(TcommCustomer::getCustNo, grpCode).last("LIMIT 1"));
            if (customer != null) {
                ctx.setCustId(customer.getId());
            }
        }

        // 扩展字段：与 job 策略对齐，供 writeSuccess / 流程变量使用
        ctx.setExtraVariable("infoId", detailId);
        ctx.setExtraVariable("_detailId", detailId);
        ctx.setExtraVariable("orgId", targetUser.getOrgId());
        ctx.setExtraVariable("nickName", targetUser.getNameCh());
        ctx.setExtraVariable("phone", targetUser.getRelationTel());
        ctx.setExtraVariable("_needOrderNumber", true);
        ctx.setExtraVariable("orderCategory", "C");
        ctx.setExtraVariable("areaName", str(row.get("area_name")));
        ctx.setExtraVariable("countyName", str(row.get("county_id")));
        ctx.setExtraVariable("grpCode", grpCode);
        ctx.setExtraVariable("custGrpId", custGrpId);
        // 被指派的处理人 OA 信息（供 assignSuccess 反查旧 sys_user 写 manager_id/name）
        ctx.setExtraVariable("oaIdDev", str(row.get("oa_id_dev")));
        ctx.setExtraVariable("oaNameDev", str(row.get("oa_name_dev")));

        // businessDep：对齐 job 策略用 custGrpId 查 ftp_cust_base_info.grp_busi_dept_type
        if (StringUtils.isNotBlank(custGrpId) && ftpCustBaseInfoMapper != null) {
            try {
                FtpCustBaseInfoDo custInfo = ftpCustBaseInfoMapper.selectOne(
                        new LambdaQueryWrapper<FtpCustBaseInfoDo>()
                                .eq(FtpCustBaseInfoDo::getCustGrpId, Integer.parseInt(custGrpId))
                                .last("LIMIT 1"));
                if (custInfo != null) {
                    String deptType = custInfo.getGrpBusiDeptType();
                    ctx.setExtraVariable("businessDep",
                            (StringUtils.isBlank(deptType) || "99".equals(deptType)) ? "22" : deptType);
                }
            } catch (Exception e) {
                log.debug("【特殊活动指派】查 ftp_cust_base_info businessDep 失败: custGrpId={}", custGrpId);
            }
        }
        return ctx;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealBySpecialActivity?orderId=" + orderId;
    }

    private String str(Object v) {
        return v == null ? null : v.toString();
    }
}
