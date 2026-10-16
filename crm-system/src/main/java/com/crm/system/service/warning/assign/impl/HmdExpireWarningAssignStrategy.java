package com.crm.system.service.warning.assign.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.dao.FtpCustBaseInfoDo;
import com.crm.system.domain.dao.FtpTcUserCustWarningDo;
import com.crm.system.domain.dao.FtpTcUserCommonWarningDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.cust.FtpCustBaseInfoMapper;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.IFtpTcUserCommonWarningService;
import com.crm.system.service.IFtpTcUserCustWarningService;
import com.crm.system.service.warning.assign.IWarningAssignStrategy;
import com.crm.system.service.warning.assign.WarningAssignContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 红名单到期预警未派发指派策略。
 * <p>
 * 手动指派（老框架"维系预警未派发工单清单"批量转派）时按 maintenance_detail.detail_id 反查原始预警行
 * （新版客户级 ftp_tc_user_cust_warning，warning_type=000030；历史用户级 ftp_tc_user_common_warning 兜底），
 * 使用前端指定的客户经理覆盖原 oa_id_dev，并组装建单上下文。
 * </p>
 * <p>
 * 与 job 策略 {@code HmdExpireWarningStrategy} 的关键差异：
 * <ul>
 *   <li>处理人取前端指定目标人（不取预警原 oa_id_dev）；</li>
 *   <li>流程 key 为红名单双节点流程 {@code hmd_warn_approval}（客户经理处理 → 领导审核），
 *       而非指派接口默认的 {@code single_approval}；</li>
 *   <li>处理页为 {@code /userCustWarning/dealByHmd}（对应前端 views/workflow/hmdWarning/deal）。</li>
 * </ul>
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HmdExpireWarningAssignStrategy implements IWarningAssignStrategy {

    /** 红名单到期的预警类型编码（ftp_tc_user_cust_warning.warning_type） */
    private static final String WARNING_TYPE = "000030";

    private final IFtpTcUserCustWarningService ftpTcUserCustWarningService;
    private final IFtpTcUserCommonWarningService ftpTcUserCommonWarningService;
    private final SysUsersMapper sysUsersMapper;
    private final TcommCustomerMapper tcommCustomerMapper;
    private final FtpCustBaseInfoMapper ftpCustBaseInfoMapper;

    @Override
    public String getOrderType() {
        return "hmd_warn";
    }

    @Override
    public String getOrderTypeName() {
        return "红名单到期预警";
    }

    /**
     * 红名单为专属双节点流程，不用接口默认的 single_approval。
     */
    @Override
    public String getProcessKey() {
        return "hmd_warn_approval";
    }

    @Override
    public WarningAssignContext buildContext(Long detailId, Long targetUserId) {
        if (detailId == null || targetUserId == null) {
            return null;
        }
        // 新版客户级预警行：detail_id 指向 ftp_tc_user_cust_warning.id_（限定期望的 warning_type）
        FtpTcUserCustWarningDo custRow = ftpTcUserCustWarningService.getByIdAndWarningType(detailId, WARNING_TYPE);
        // 历史数据兜底：detail_id 仍指向用户级表 ftp_tc_user_common_warning
        FtpTcUserCommonWarningDo commonRow = null;
        if (custRow == null) {
            commonRow = ftpTcUserCommonWarningService.getById(detailId.intValue());
        }
        if (custRow == null && commonRow == null) {
            log.warn("【红名单指派】未找到原始预警行, detailId={}", detailId);
            return null;
        }

        String grpName = custRow != null ? custRow.getGrpName() : commonRow.getGrpName();
        String grpCode = custRow != null ? custRow.getGrpCode() : commonRow.getGrpCode();
        Integer custGrpIdObj = custRow != null ? custRow.getCustGrpId() : commonRow.getCustGrpId();
        String custGrpId = custGrpIdObj == null ? null : String.valueOf(custGrpIdObj);
        String areaName = custRow != null ? custRow.getAreaName() : commonRow.getAreaName();
        // 客户级表有 county_name；用户级表无 county_name，只有 county_id 编码
        String countyName = custRow != null ? custRow.getCountyName() : null;
        if (StringUtils.isBlank(countyName)) {
            countyName = custRow != null ? null : commonRow.getCountyId();
        }
        String oaIdDev = custRow != null ? custRow.getOaIdDev() : commonRow.getOaIdDev();
        String oaNameDev = custRow != null ? custRow.getOaNameDev() : commonRow.getOaNameDev();

        SysUsers targetUser = sysUsersMapper.selectById(targetUserId);
        if (targetUser == null) {
            log.warn("【红名单指派】目标客户经理不存在, userId={}", targetUserId);
            return null;
        }

        WarningAssignContext ctx = new WarningAssignContext();
        ctx.setOrderType(getOrderType());
        ctx.setOrderName(DateUtils.dateTime() + "_" + (StringUtils.isNotBlank(grpName) ? grpName + "_" : "") + "红名单到期预警");
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

        // 扩展字段：与 job 策略 HmdExpireWarningStrategy 对齐，供建单/流程变量/维系明细使用
        ctx.setExtraVariable("infoId", detailId);
        ctx.setExtraVariable("_detailId", detailId);
        ctx.setExtraVariable("orgId", targetUser.getOrgId());
        ctx.setExtraVariable("nickName", targetUser.getNameCh());
        ctx.setExtraVariable("phone", targetUser.getRelationTel());
        ctx.setExtraVariable("_needOrderNumber", true);
        ctx.setExtraVariable("orderCategory", "C");
        ctx.setExtraVariable("areaName", areaName);
        ctx.setExtraVariable("countyName", countyName);
        ctx.setExtraVariable("grpCode", grpCode);
        ctx.setExtraVariable("custGrpId", custGrpId);
        // 被指派的处理人 OA 信息（供 assignSuccess 反查旧 sys_user 写 manager_id/name）
        ctx.setExtraVariable("oaIdDev", oaIdDev);
        ctx.setExtraVariable("oaNameDev", oaNameDev);

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
                log.debug("【红名单指派】查 ftp_cust_base_info businessDep 失败: custGrpId={}", custGrpId, e);
            }
        }
        return ctx;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealByHmd?orderId=" + orderId;
    }
}
