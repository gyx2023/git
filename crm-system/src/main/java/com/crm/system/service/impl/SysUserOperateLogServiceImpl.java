package com.crm.system.service.impl;

import com.crm.common.utils.StringUtils;
import com.crm.system.domain.SysUserOperateLog;
import com.crm.system.domain.SysUsers;
import com.crm.system.mapper.SysUserOperateLogMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysUsersService;
import com.crm.system.service.SysUserOperateLogService;
import com.crm.system.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * 系统用户操作日志Service实现
 */
@Slf4j
@Service
public class SysUserOperateLogServiceImpl implements SysUserOperateLogService {

    @Resource
    private SysUserOperateLogMapper sysUserOperateLogMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private ISysUsersService sysUsersService;

    @Override
    public void synUserOperate(SysUsers user, String operateType) {
        try {
            SysUsers operator = null;
            // 打个补丁，防止非登录状态下的账号操作获取不到登录人信息，
            // 此时统一给到管理员
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    HttpSession session = request.getSession();
                    operator = (SysUsers) session.getAttribute("currentUser");
                }
                if (operator == null) {
                    // 如果获取不到当前用户，使用默认管理员
                    operator = sysUsersMapper.selectById(2L);
                }
            } catch (Exception e) {
                operator = sysUsersMapper.selectById(2L);
            }

            SysUserOperateLog operateLog = new SysUserOperateLog();
            // 复制用户信息到日志
            BeanUtils.copyProperties(user, operateLog);

            // 设置操作相关信息
            operateLog.setOperateType(operateType);
            operateLog.setOperateTime(new Date());
            operateLog.setOperator(operator != null && operator.getId() != null ? operator.getId().intValue() : 2);
            operateLog.setInfoId((long) (user.getId() != null ? user.getId().intValue() : 0));

            String userRoleYx = user.getUserRoleYx();
            // 保存日志
            sysUserOperateLogMapper.insert(operateLog);

            // 如果userRoleYx不为空且不等于"0"且操作类型为"del"，需要同步亚信
            if (StringUtils.isNotEmpty(userRoleYx) && !"0".equals(userRoleYx) && "del".equals(operateType)) {
                try {
                    sysUsersService.doPostForYxUser(user, operateType);
                } catch (Exception e) {
                    log.error("同步亚信用户信息失败", e);
                }
            }
        } catch (Exception e) {
            log.error("保存用户操作日志失败", e);
        }
    }
}
