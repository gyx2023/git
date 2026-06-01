package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.SysUserCustNo;
import com.crm.system.mapper.SysUserCustNoMapper;
import com.crm.system.service.ISysUserCustNoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统用户customer_no Service实现
 */
@Service
@Slf4j
public class SysUserCustNoServiceImpl implements ISysUserCustNoService {

    @Resource
    private SysUserCustNoMapper sysUserCustNoMapper;

    @Override
    public List<SysUserCustNo> findListByUserId(Integer userId) {
        LambdaQueryWrapper<SysUserCustNo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SysUserCustNo::getUserId, userId);
        return sysUserCustNoMapper.selectList(lqw);
    }
}
