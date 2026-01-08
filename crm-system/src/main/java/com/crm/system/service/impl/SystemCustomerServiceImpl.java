package com.crm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.SystemCustomerDo;
import com.crm.system.domain.vo.SystemCustomerVo;
import com.crm.system.mapper.SystemCustomerMapper;
import com.crm.system.service.ISystemCustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SystemCustomerServiceImpl implements ISystemCustomerService {

    @Resource
    private SystemCustomerMapper systemCustomerMapper;


    @Override
    public SystemCustomerVo findSysCustByName(String custName) {
        LambdaQueryWrapper<SystemCustomerDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtil.isNotEmpty(custName), SystemCustomerDo::getCustName, custName);
        List<SystemCustomerVo> list = systemCustomerMapper.selectVoList(lqw);
        if (ObjectUtil.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }
}
